package com.wang.wangaiagent.controller;

import com.wang.wangaiagent.agent.WangManus;
import com.wang.wangaiagent.app.LoveApp;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;

/**
 * @author: Shajia Wang
 * @createTime: 2025/6/7---16:48
 * @description:
 */

@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private LoveApp loveApp;

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel dashscopeChatModel;


    @GetMapping("/love_app/chat/sync")
    public String doChatWithLoveAppSync(String message,String chatId){
        return loveApp.doChat(message, chatId);
    }

    /**
     * 基于SSE的流式输出接口 ，方法一：返回FLux响应式对象，并且添加SSE对应的MediaType
     * 这里选择第一种
     * @param message 用户信息
     * @param chatId  会话ID
     * @return 输出内容
     */
    @GetMapping(value = "/love_app/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithLoveAppSse(String message,String chatId){
        return loveApp.doChatByStream(message, chatId);
    }

    /**
     * 基于SSE的流式输出接口，方法2： 返回Flux对象，并且设置泛型为ServerSentEvent
     * @param message 用户信息
     * @param chatId 会话ID
     * @return  输出内容
     */
    @Deprecated
    @GetMapping("/love_app/chat/sse/ServerSentEvent")
    public Flux<ServerSentEvent<String>> doChatWithLoveAppSseWithServerSentEvent(String message, String chatId){
        return loveApp.doChatByStream(message,chatId)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }

    /**
     * 基于SSE的流式输出接口，方法3： 创建SseEmitter对象，并返回
     * @param message 用户信息
     * @param chatId 会话ID
     * @return 输出内容
     */
    @Deprecated
    @GetMapping("/love_app/chat/sse/emitter")
    public SseEmitter doChatWithLoveAppSseEmitter(String message, String chatId) {
        //创建一个超时时间较长的SseEmitter,这里设置为3分钟
        SseEmitter sseEmitter = new SseEmitter(180000L);
        //获取Flux数据流并直接订阅
        loveApp.doChatByStream(message, chatId)
                .subscribe(
                        //处理每条消息
                        chunk -> {
                            try {
                                sseEmitter.send(chunk);
                            }catch (IOException e){
                                sseEmitter.completeWithError(e);
                            }
                        },
                        //处理错误
                        sseEmitter::completeWithError,
                        //处理完成
                        sseEmitter::complete
                );
        return sseEmitter;
    }

    /**
     * 流式调用 Manus 超级智能体
     *
     * @param message 用户信息
     * @return 输出内容
     */
    @GetMapping("/manus/chat")
    public SseEmitter doChatWithManus(String message) {
        WangManus wangManus = new WangManus(allTools, dashscopeChatModel);
        return wangManus.runStream(message);
    }

}
