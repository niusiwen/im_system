package com.nsw.im.service.conversation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.nsw.im.codec.pack.conversation.DeleteConversationPack;
import com.nsw.im.codec.pack.conversation.UpdateConversationPack;
import com.nsw.im.common.ResponseVO;
import com.nsw.im.common.config.AppConfig;
import com.nsw.im.common.constant.Constants;
import com.nsw.im.common.enums.ConversationErrorCode;
import com.nsw.im.common.enums.ConversationTypeEnum;
import com.nsw.im.common.enums.command.ConversationEventCommand;
import com.nsw.im.common.model.ClientInfo;
import com.nsw.im.common.model.SyncReq;
import com.nsw.im.common.model.SyncResp;
import com.nsw.im.common.model.message.MessageReadedContent;
import com.nsw.im.service.conversation.dao.ImConversationSetEntity;
import com.nsw.im.service.conversation.dao.mapper.ImConversationSetMapper;
import com.nsw.im.service.conversation.model.DeleteConversationReq;
import com.nsw.im.service.conversation.model.UpdateConversationReq;
import com.nsw.im.service.seq.RedisSeq;
import com.nsw.im.service.utils.MessageProducer;
import com.nsw.im.service.utils.WriteUserSeq;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author nsw
 * @date 2024/2/4 17:20
 */
@Service
public class ConversationService {

    @Autowired
    ImConversationSetMapper imConversationSetMapper;

    @Autowired
    RedisSeq redisSeq;

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    AppConfig appConfig;

    @Autowired
    WriteUserSeq writeUserSeq;

    /**
     * 生成会话id
     * @param type 0单聊 1群聊
     * @param fromId
     * @param toId
     * @return
     */
    public String convertConversationId(Integer type,String fromId,String toId) {
        return type + "_" + fromId + "_" + toId;
    }

    /**
     * 消息标记为已读
     * @param messageReadedContent
     */
    public void messageMarkRead(MessageReadedContent messageReadedContent) {

        String toId = messageReadedContent.getToId();
        if (messageReadedContent.getConversationType() == ConversationTypeEnum.GROUP.getCode()) {
            toId = messageReadedContent.getGroupId();
        }
        String conversationId = convertConversationId(messageReadedContent.getConversationType(), messageReadedContent.getFromId(),
                toId);

        LambdaQueryWrapper<ImConversationSetEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ImConversationSetEntity::getConversationId, conversationId);
        queryWrapper.eq(ImConversationSetEntity::getAppId, messageReadedContent.getAppId());
        ImConversationSetEntity imConversationSetEntity = imConversationSetMapper.selectOne(queryWrapper);
        if (imConversationSetEntity == null) {
            imConversationSetEntity = new ImConversationSetEntity();
            long seq = redisSeq.doGetSeq(messageReadedContent.getAppId() + ":" + Constants.SeqConstants.Conversation);
            imConversationSetEntity.setConversationId(conversationId);
            BeanUtils.copyProperties(messageReadedContent, imConversationSetEntity);
            imConversationSetEntity.setReadedSequence(messageReadedContent.getMessageSequence());
            imConversationSetEntity.setToId(toId); //这里需要再次赋值一下toId，
            imConversationSetEntity.setSequence(seq);
            imConversationSetMapper.insert(imConversationSetEntity);
            writeUserSeq.writeUserSeq(messageReadedContent.getAppId(), messageReadedContent.getFromId(),
                    Constants.SeqConstants.Conversation, seq);
        } else {
            //
            long seq = redisSeq.doGetSeq(messageReadedContent.getAppId() + ":" + Constants.SeqConstants.Conversation);
            imConversationSetEntity.setSequence(seq);
            imConversationSetEntity.setReadedSequence(messageReadedContent.getMessageSequence());
            imConversationSetMapper.readMark(imConversationSetEntity);
            writeUserSeq.writeUserSeq(messageReadedContent.getAppId(), messageReadedContent.getFromId(),
                    Constants.SeqConstants.Conversation, seq);
        }

    }

    /**
     * 删除会话
     * @param req
     * @return
     */
    public ResponseVO deleteConversation(DeleteConversationReq req) {

        // 置顶 免打扰 置为默认值--》根据不通的需求而定
//        LambdaQueryWrapper<ImConversationSetEntity> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(ImConversationSetEntity::getAppId, req.getAppId());
//        queryWrapper.eq(ImConversationSetEntity::getConversationId, req.getConversationId());
//        ImConversationSetEntity imConversationSetEntity = imConversationSetMapper.selectOne(queryWrapper);
//        if (imConversationSetEntity != null) {
//            imConversationSetEntity.setIsMute(0);
//            imConversationSetEntity.setIsTop(0);
//            imConversationSetMapper.update(imConversationSetEntity, queryWrapper);
//        }

        // 是否同步所有端
        if (appConfig.getDeleteConversationSyncMode() == 1) {
            DeleteConversationPack pack = new DeleteConversationPack();
            pack.setConversationId(req.getConversationId());
            messageProducer.sendToUserExceptClient(req.getFromId(),
                    ConversationEventCommand.CONVERSATION_DELETE,
                    pack, new ClientInfo(req.getAppId(),req.getClientType(),
                            req.getImei()));

        }

        return ResponseVO.successResponse();
    }

    /**
     * 更新会话 --> 置顶 or 免打扰
     * @param req
     * @return
     */
    public ResponseVO updateConversation(UpdateConversationReq req) {

        if (req.getIsTop() == null && req.getIsMute() == null) {
            return ResponseVO.errorResponse(ConversationErrorCode.CONVERSATION_UPDATE_PARAM_ERROR);
        }

        LambdaQueryWrapper<ImConversationSetEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ImConversationSetEntity::getAppId, req.getAppId());
        queryWrapper.eq(ImConversationSetEntity::getConversationId, req.getConversationId());
        ImConversationSetEntity imConversationSetEntity = imConversationSetMapper.selectOne(queryWrapper);
        if (imConversationSetEntity != null) {
            long seq = redisSeq.doGetSeq(req.getAppId() + ":" + Constants.SeqConstants.Conversation);
            if (req.getIsMute() != null) {
                imConversationSetEntity.setIsMute(req.getIsMute());
            }
            if (req.getIsTop() != null) {
                imConversationSetEntity.setIsTop(req.getIsTop());
            }
            imConversationSetEntity.setSequence(seq);
            imConversationSetMapper.update(imConversationSetEntity, queryWrapper);
            // 更新完成后把的seq写入到redis
            writeUserSeq.writeUserSeq(req.getAppId(), req.getFromId(),
                    Constants.SeqConstants.Conversation, seq);

            // 更新完成后，同步其他端
            UpdateConversationPack pack = new UpdateConversationPack();
            pack.setConversationId(imConversationSetEntity.getConversationId());
            pack.setIsMute(imConversationSetEntity.getIsMute());
            pack.setIsTop(imConversationSetEntity.getIsTop());
            pack.setSequence(seq);
            pack.setConversationType(imConversationSetEntity.getConversationType());
            messageProducer.sendToUserExceptClient(req.getFromId(),
                    ConversationEventCommand.CONVERSATION_UPDATE,
                    pack, new ClientInfo(req.getAppId(),req.getClientType(),
                            req.getImei()));
        }

        return ResponseVO.successResponse();
    }

    public ResponseVO syncConversationSet(SyncReq req) {
        if(req.getMaxLimit() > 100){
            req.setMaxLimit(100);
        }

        SyncResp<ImConversationSetEntity> resp = new SyncResp<>();
        //seq > req.getseq limit maxLimit
        QueryWrapper<ImConversationSetEntity> queryWrapper =
                new QueryWrapper<>();
        queryWrapper.eq("from_id",req.getOperater());
        queryWrapper.gt("sequence",req.getLastSequence());
        queryWrapper.eq("app_id",req.getAppId());
        queryWrapper.last(" limit " + req.getMaxLimit());
        queryWrapper.orderByAsc("sequence");
        List<ImConversationSetEntity> list = imConversationSetMapper
                .selectList(queryWrapper);

        if(!CollectionUtils.isEmpty(list)){
            ImConversationSetEntity maxSeqEntity = list.get(list.size() - 1);
            resp.setDataList(list);
            //设置最大seq
            Long friendShipMaxSeq = imConversationSetMapper.geConversationSetMaxSeq(req.getAppId(), req.getOperater());
            resp.setMaxSequence(friendShipMaxSeq);
            //设置是否拉取完毕
            resp.setCompleted(maxSeqEntity.getSequence() >= friendShipMaxSeq);
            return ResponseVO.successResponse(resp);
        }

        resp.setCompleted(true);
        return ResponseVO.successResponse(resp);

    }
}
