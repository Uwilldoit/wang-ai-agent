package com.wang.wangaiagent.rag;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;

/**
 * @author: Shajia Wang
 * @createTime: 2025/5/28---12:09
 * @description: 创建上下文查询增强器的工厂
 */

public class LoveAppContextualQueryAugmenterFactory {
    public static ContextualQueryAugmenter createInstance(){
        PromptTemplate emptyPromptTemplate = new PromptTemplate("抱歉，我只能回答恋爱相关的问题，无法帮到其他的，有问题可以联系" +
                "企鹅1243189230");
        return ContextualQueryAugmenter.builder()
                .allowEmptyContext(false)
                .emptyContextPromptTemplate(emptyPromptTemplate)
                .build();
    }
}
