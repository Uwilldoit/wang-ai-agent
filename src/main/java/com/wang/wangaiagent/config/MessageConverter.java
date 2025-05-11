package com.wang.wangaiagent.config;

import com.wang.wangaiagent.model.entity.ChatMessage;
import org.springframework.ai.chat.messages.*;

import java.util.List;
import java.util.Map;

/**
 * @author: Shajia Wang
 * @createTime: 2025/5/11---17:38
 * @description:
 */

public class MessageConverter {

    /**
     * 将 Message 转换为 ChatMessage
     */
    public static ChatMessage toChatMessage(Message message, String conversationId) {
        return ChatMessage.builder()
                .conversationId(conversationId)
                .messageType(message.getMessageType())
                .content(message.getText())
                .metadata(message.getMetadata())
                .build();
    }

    /**
     * 将 ChatMessage 转换为 Message
     */
    public static Message toMessage(ChatMessage chatMessage) {
        MessageType messageType = chatMessage.getMessageType();
        String text = chatMessage.getContent();
        Map<String, Object> metadata = chatMessage.getMetadata();
        return switch (messageType) {
            case USER -> new UserMessage(text);
            case ASSISTANT -> new AssistantMessage(text, metadata);
            case SYSTEM -> new SystemMessage(text);
            case TOOL -> new ToolResponseMessage(List.of(), metadata);
        };
    }

}
