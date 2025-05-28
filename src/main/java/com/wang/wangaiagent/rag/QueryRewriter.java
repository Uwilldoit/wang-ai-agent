package com.wang.wangaiagent.rag;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.stereotype.Component;

/**
 * @author: Shajia Wang
 * @createTime: 2025/5/28---11:26
 * @description: 查询重写
 */

@Component
public class QueryRewriter {

    private final QueryTransformer queryTransformer;

    public QueryRewriter(ChatModel dashScopeChatModel){
        ChatClient.Builder builder =ChatClient.builder(dashScopeChatModel);
        //创建查询重写转换器
         queryTransformer = RewriteQueryTransformer.builder()
                .chatClientBuilder(builder)
                .build();
    }

    /**
     * 执行查询重写
     * @param prompt 查询内容
     * @return 重写后的查询
     */
    public String  doQueryRewriter(String prompt){
        Query query = new Query(prompt);
        //执行查询重写
        Query transform = queryTransformer.transform(query);
        return transform.text();

    }
}
