package com.nsw.im.service.conversation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nsw.im.codec.pack.conversation.DeleteConversationPack;
import com.nsw.im.codec.pack.conversation.UpdateConversationPack;
import com.nsw.im.common.ResponseVO;
import com.nsw.im.common.config.AppConfig;
import com.nsw.im.common.constant.Constants;
import com.nsw.im.common.enums.ConversationErrorCode;
import com.nsw.im.common.enums.ConversationTypeEnum;
import com.nsw.im.common.enums.command.ConversationEventCommand;
import com.nsw.im.common.model.ClientInfo;
import com.nsw.im.common.model.message.MessageReadedContent;
import com.nsw.im.service.conversation.dao.ImConversationSetEntity;
import com.nsw.im.service.conversation.dao.mapper.ImConversationSetMapper;
import com.nsw.im.service.conversation.model.DeleteConversationReq;
import com.nsw.im.service.conversation.model.UpdateConversationReq;
import com.nsw.im.service.seq.RedisSeq;
import com.nsw.im.service.utils.MessageProducer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
            //long seq = redisSeq.deGetSeq(messageReadedContent.getAppId() + ":" + Constants.SeqConstants.Conversation);
            imConversationSetEntity.setConversationId(conversationId);
            BeanUtils.copyProperties(messageReadedContent, imConversationSetEntity);
            imConversationSetEntity.setReadedSequence(messageReadedContent.getMessageSeqence());

            imConversationSetMapper.insert(imConversationSetEntity);
        } else {
            //
            imConversationSetEntity.setSequence(messageReadedContent.getMessageSeqence());
            imConversationSetMapper.readMark(imConversationSetEntity);
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
            if (req.getIsMute() != null) {
                imConversationSetEntity.setIsMute(req.getIsMute());
            }
            if (req.getIsTop() != null) {
                imConversationSetEntity.setIsTop(req.getIsTop());
            }

            imConversationSetMapper.update(imConversationSetEntity, queryWrapper);
            // 更新完成后，同步其他端
            UpdateConversationPack pack = new UpdateConversationPack();
            pack.setConversationId(imConversationSetEntity.getConversationId());
            pack.setIsMute(imConversationSetEntity.getIsMute());
            pack.setIsTop(imConversationSetEntity.getIsTop());
            pack.setConversationType(imConversationSetEntity.getConversationType());
            messageProducer.sendToUserExceptClient(req.getFromId(),
                    ConversationEventCommand.CONVERSATION_UPDATE,
                    pack, new ClientInfo(req.getAppId(),req.getClientType(),
                            req.getImei()));
        }

        return ResponseVO.successResponse();
    }

}
