package com.wang.wangaiagent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wang.wangaiagent.model.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author: Shajia Wang
 * @createTime: 2025/5/11---17:40
 * @description:
 */

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
}

