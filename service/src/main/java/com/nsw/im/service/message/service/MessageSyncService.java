package com.nsw.im.service.message.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.nsw.im.codec.pack.message.MessageReadedPack;
import com.nsw.im.common.ResponseVO;
import com.nsw.im.common.constant.Constants;
import com.nsw.im.common.enums.command.Command;
import com.nsw.im.common.enums.command.GroupEventCommand;
import com.nsw.im.common.enums.command.MessageCommand;
import com.nsw.im.common.model.SyncReq;
import com.nsw.im.common.model.SyncResp;
import com.nsw.im.common.model.message.MessageReadedContent;
import com.nsw.im.common.model.message.MessageReceiveAckContent;
import com.nsw.im.common.model.message.OfflineMessageContent;
import com.nsw.im.service.conversation.service.ConversationService;
import com.nsw.im.service.utils.MessageProducer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 消息同步
 * @author nsw
 * @date 2023/12/7 21:58
 */
@Service
public class MessageSyncService {

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    ConversationService conversationService;

    @Autowired
    RedisTemplate redisTemplate;


    /**
     * 服务端确认接收到消息发送给客户端的ack消息
     * @param messageReceiveAckContent
     */
    public void receiveMark(MessageReceiveAckContent messageReceiveAckContent) {

        messageProducer.sendToUser(messageReceiveAckContent.getToId(),
                MessageCommand.MSG_RECEIVE_ACK, messageReceiveAckContent, messageReceiveAckContent.getAppId());

    }


    /**
     * 消息已读：
     * 1、更新会话seq,
     * 2、通知在线的同步端，发送指定的command，
     * 3、发送已读回执通知对方（消息发起方）我已读
     * @param messageReadedContent
     */
    public void readMark(MessageReadedContent messageReadedContent) {
        // 1、更新会话seq,
        conversationService.messageMarkRead(messageReadedContent);

        // 2、通知在线的同步端，发送指定的command，
        MessageReadedPack messageReadedPack = new MessageReadedPack();
        BeanUtils.copyProperties(messageReadedContent, messageReadedPack);
        syncToSender(messageReadedPack, messageReadedContent, MessageCommand.MSG_READED_NOTIFY);

        // 3、发送给对方 已读
        messageProducer.sendToUser(messageReadedContent.getToId(),
                MessageCommand.MSG_READED_RECEIPT, messageReadedPack, messageReadedContent.getAppId());


    }

    /**
     * 发送同步端已读通知
     * @param pack
     * @param content
     */
    private void syncToSender(MessageReadedPack pack, MessageReadedContent content, Command command) {
        // 发送给自己的其他端
        messageProducer.sendToUserExceptClient(pack.getFromId(),
                command, pack, content);

    }


    /**
     * 群聊消息已读
     * @param readedContent
     */
    public void groupReadMark(MessageReadedContent readedContent) {
        // 1、更新会话seq,
        conversationService.messageMarkRead(readedContent);

        // 2、通知在线的同步端，发送指定的command，
        MessageReadedPack messageReadedPack = new MessageReadedPack();
        BeanUtils.copyProperties(readedContent, messageReadedPack);
        syncToSender(messageReadedPack, readedContent, GroupEventCommand.MSG_GROUP_READED_NOTIFY);

        // 3、发送给对方 已读
        messageProducer.sendToUser(readedContent.getToId(),
                GroupEventCommand.MSG_GROUP_READED_RECEIPT, messageReadedPack, readedContent.getAppId());

    }

    /**
     * 拉取离线消息
     * @param req
     * @return
     */
    public ResponseVO syncOfflineMessage(SyncReq req) {

        SyncResp<OfflineMessageContent> resp = new SyncResp<>();

        String key  = req.getAppId() + ":" + Constants.RedisConstants.OfflineMessage + ":" + req.getOperater();
        // 1、获取离线消息中最大的seq
        Long maxSeq = 0L;
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        Set set = zSetOperations.reverseRangeWithScores(key, 0, 0);// 获取zSet中最大的值
        if (!CollectionUtils.isEmpty(set)) {
            List list = new ArrayList(set);
            DefaultTypedTuple o = (DefaultTypedTuple) list.get(0);
            maxSeq = o.getScore().longValue();
        }
        resp.setMaxSequence(maxSeq);

        // 2、查询离线消息数据
        List<OfflineMessageContent> respList = new ArrayList<>();
        Set<ZSetOperations.TypedTuple> querySet = zSetOperations.rangeByScoreWithScores(key, req.getLastSequence(), maxSeq, 0, req.getMaxLimit());
        for (ZSetOperations.TypedTuple<String> typedTuple : querySet) {
            String value = typedTuple.getValue();
            OfflineMessageContent offlineMessageContent = JSONObject.parseObject(value, OfflineMessageContent.class);
            respList.add(offlineMessageContent);
        }
        resp.setDataList(respList);

        // 3、判断是否查询完毕
        if (!CollectionUtils.isEmpty(respList)) {
            OfflineMessageContent offlineMessageContent = respList.get(respList.size() - 1);
            resp.setCompleted(maxSeq <= offlineMessageContent.getMessageKey());
        }

        return ResponseVO.successResponse(resp);
    }
}
