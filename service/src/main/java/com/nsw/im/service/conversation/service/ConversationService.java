package com.nsw.im.service.conversation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nsw.im.common.constant.Constants;
import com.nsw.im.common.enums.ConversationTypeEnum;
import com.nsw.im.common.model.message.MessageReadedContent;
import com.nsw.im.service.conversation.dao.ImConversationSetEntity;
import com.nsw.im.service.conversation.dao.mapper.ImConversationSetMapper;
import com.nsw.im.service.seq.RedisSeq;
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

    /**
     * 生成会话id
     * @param type 0单聊 1群聊
     * @param fromId
     * @param toId
     * @return
     */
    private String convertConversationId(Integer type,String fromId,String toId) {
        return type + "_" + fromId + "_" + toId;
    }

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

}
