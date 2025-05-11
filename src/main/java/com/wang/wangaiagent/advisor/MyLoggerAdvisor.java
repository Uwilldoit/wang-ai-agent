package com.wang.wangaiagent.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.model.MessageAggregator;
import reactor.core.publisher.Flux;

/**
 * @author: Shajia Wang
 * @createTime: 2025/5/11---00:48
 * @description: 自定义日志 Advisor
 *  * 打印 info 级别日志、只输出单次用户提示词和 AI 回复的文本
 */

@Slf4j
public class MyLoggerAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

    /**
     * 获取名字
     * @return
     */
    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * 环绕通知
     * @param advisedRequest 调用before进行前置处理
     * @param chain 执行后续拦截器
     * @return advisedResponse
     */
    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        this.before(advisedRequest);
        AdvisedResponse advisedResponse = chain.nextAroundCall(advisedRequest);
        this.observeAfter(advisedResponse);
        return advisedResponse;
    }

    /**
     * 响应式流式调用的增强
     * @param advisedRequest 执行before进行前置处理
     * @param chain 调用责任链中的下一个节点处理并获取响应流
     * @return 使用MessageAggregator对响应流进行聚合，并在聚合完成后执行observeAfter方法进行后置处理
     */
    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        this.before(advisedRequest);
        Flux<AdvisedResponse> advisedResponseFlux = chain.nextAroundStream(advisedRequest);
        return (new MessageAggregator()).aggregateAdvisedResponse(advisedResponseFlux,this::observeAfter);
    }

    /**
     * 前置通知
     * @param request ,记录用户输入的文本日志
     */

    private  AdvisedRequest before(AdvisedRequest request){
        log.info("AI Request: {}",request.userText());
        return request;
    }

    /**
     * 后置通知
     * @param response 提取AI输出文本
     */

    private  void observeAfter (AdvisedResponse  response ){
        log.info("AI response: {}",response.response().getResult().getOutput().getText());
    }



    @Override
    public int getOrder() {
        return 0;
    }
}
