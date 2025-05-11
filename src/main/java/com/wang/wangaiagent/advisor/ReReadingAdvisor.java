package com.wang.wangaiagent.advisor;

import org.springframework.ai.chat.client.advisor.api.*;
import reactor.core.publisher.Flux;

import java.util.HashMap;

/**
 * @author: Shajia Wang
 * @createTime: 2025/5/11---16:00
 * @description: 自定义Re2 Advisor
 * 可提高大型语言模型的推理能力
 */

public class ReReadingAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {
    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        return chain.nextAroundCall(this.before(advisedRequest));
    }

    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        return chain.nextAroundStream(this.before(advisedRequest));
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * 对请求进行预处理，将用户输入文本存储到参数中，并修改用户输入文本为提示语句，要求模型再次阅读问题
     * @param advisedRequest 请求
     * @return 处理后的请求
     */
    private AdvisedRequest before(AdvisedRequest advisedRequest){
        HashMap<String, Object> advisedUserParams = new HashMap<>(advisedRequest.userParams());
        advisedUserParams.put("re2_input_query", advisedRequest.userText());

        return AdvisedRequest.from(advisedRequest).
                userText("""
                        {re2_input_query}
                        Read the question again: {re2_input_query}
                        """).userParams(advisedUserParams).build();
    }
}
