package com.wang.wangaiagent.manager.mysql;

import com.baomidou.mybatisplus.extension.repository.CrudRepository;
import com.wang.wangaiagent.mapper.ChatMessageMapper;
import com.wang.wangaiagent.model.entity.ChatMessage;
import org.springframework.stereotype.Component;

/**
 * @author: Shajia Wang
 * @createTime: 2025/5/11---17:52
 * @description:
 */

@Component
public class ChatMessageRepository extends CrudRepository<ChatMessageMapper, ChatMessage> {
}