package com.wang.wangaiagent.agent;


import com.wang.wangaiagent.agent.model.AgentState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.internal.StringUtil;
import org.springframework.ai.chat.client.ChatClient;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author: Shajia Wang
 * @createTime: 2025/6/4---15:58
 * @description: 抽象基础代理类，用于管理代理状态和执行流程。
 * 提供状态转换、内存管理和基于步骤的执行循环的基础功能。
 * 子类必须实现step方法。
 */

@Data
@Slf4j
public abstract class BaseAgent {

    /**
     * 核心属性
     */
    private String name;

    /**
     * 提示
     */
    private String systemPrompt;
    private String nextStepPrompt;

    /**
     * 用于控制智能体的执行流程，记录状态，默认初始为空闲
     */
    private AgentState state = AgentState.IDLE;

    /**
     * 执行控制，用于记录最大步骤数和当前步骤数
     */
    private int maxSteps = 10;
    private int currentStep = 0;
    private int duplicateThreshold = 2;

    /**
     * LLM，可由调用方传入具体调用大模型的对象，更加灵活
     */
    private ChatClient chatClient;


    /**
     * Memory（需要自主维护会话上下文）
     */
    private List<Message> messageList = new ArrayList<>();

    /**
     * 运行代理
     *
     * @param userPrompt 用户提示词
     * @return 执行结果
     */
    public String run(String userPrompt) {
        if (this.state != AgentState.IDLE) {
            throw new RuntimeException("Cannot run agent from state: " + this.state);
        }
        if (StringUtil.isBlank(userPrompt)) {
            throw new RuntimeException("对话输入不能为空");
        }
        // 更改状态
        state = AgentState.RUNNING;
        // 记录消息上下文
        messageList.add(new UserMessage(userPrompt));
        // 保存结果列表
        List<String> results = new ArrayList<>();
        try {
            for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                int stepNumber = i + 1;
                currentStep = stepNumber;
                log.info("Executing step {}/{}", stepNumber, maxSteps);
                // 单步执行
                String stepResult = step();
                // 每一步 step 执行完都要检查是否陷入循环
                if (isStuck()) {
                    handleStuckState();
                }
                String result = "Step " + stepNumber + ": " + stepResult;
                results.add(result);
            }
            // 检查是否超出步骤限制
            if (currentStep >= maxSteps) {
                state = AgentState.FINISHED;
                results.add("Terminated: 达到最大步数 (" + maxSteps + ")");
            }
            return String.join("\n", results);
        } catch (Exception e) {
            state = AgentState.ERROR;
            log.error("Error executing agent", e);
            return "执行错误" + e.getMessage();
        } finally {
            // 清理资源
            this.cleanup();
        }
    }

    /**
     * 运行代理（流式输出）
     *
     * @param userPrompt 用户提示词
     * @return SseEmitter实例
     */
    public SseEmitter runStream(String userPrompt) {
        // 创建SseEmitter，设置较长的超时时间,这里设置为5分钟超时
        SseEmitter emitter = new SseEmitter(300000L);

        // 使用线程异步处理，避免阻塞主线程
        CompletableFuture.runAsync(() -> {
            try {
                if (this.state != AgentState.IDLE) {
                    emitter.send("错误：无法从状态运行代理: " + this.state);
                    emitter.complete();
                    return;
                }
                if (StringUtil.isBlank(userPrompt)) {
                    emitter.send("错误：不能使用空提示词运行代理");
                    emitter.complete();
                    return;
                }
                // 更改状态
                state = AgentState.RUNNING;
                // 记录消息上下文
                messageList.add(new UserMessage(userPrompt));

                try {
                    for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                        int stepNumber = i + 1;
                        currentStep = stepNumber;
                        log.info("Executing step " + stepNumber + "/" + maxSteps);

                        // 单步执行
                        String stepResult = step();
                        // 每一步 step 执行完都要检查是否陷入循环
                        if (isStuck()) {
                            handleStuckState();
                        }
                        String result = "Step " + stepNumber + ": " + stepResult;
                        // 发送每一步的结果
                        emitter.send(result);
                    }
                    // 检查是否超出步骤限制
                    if (currentStep >= maxSteps) {
                        state = AgentState.FINISHED;
                        emitter.send("执行结束: 达到最大步骤 (" + maxSteps + ")");
                    }
                    // 正常完成
                    emitter.complete();
                } catch (Exception e) {
                    state = AgentState.ERROR;
                    log.error("执行智能体失败", e);
                    try {
                        emitter.send("执行错误: " + e.getMessage());
                        emitter.complete();
                    } catch (Exception ex) {
                        emitter.completeWithError(ex);
                    }
                } finally {
                    // 清理资源
                    this.cleanup();
                }
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        // 设置超时和完成回调
        emitter.onTimeout(() -> {
            this.state = AgentState.ERROR;
            this.cleanup();
            log.warn("SSE connection timed out");
        });

        emitter.onCompletion(() -> {
            if (this.state == AgentState.RUNNING) {
                this.state = AgentState.FINISHED;
            }
            this.cleanup();
            log.info("SSE connection completed");
        });

        return emitter;
    }


    /**
     * 处理陷入循环的状态
     */
    protected void handleStuckState() {
        String stuckPrompt = "观察到重复响应。考虑新策略，避免重复已尝试过的无效路径。";
        this.nextStepPrompt = stuckPrompt + "\n" + (this.nextStepPrompt != null ? this.nextStepPrompt : "");
        System.out.println("Agent detected stuck state. Added prompt: " + stuckPrompt);
    }

/**
 * 判断对话是否陷入循环无法进行下去
 * 此方法通过检查消息列表中是否存在超过阈值的重复回复来确定对话是否“卡住”
 *
 * @return 如果对话卡住，则返回true；否则返回false
 */
private boolean isStuck() {
    // 如果消息数量少于2条，则不足以判断是否卡住
    if (messageList.size() < 2) return false;

    // 获取最后一条消息
    Message lastMessage = messageList.get(messageList.size() - 1);
    // 如果最后一条消息或其文本为空，则不足以判断
    if (lastMessage == null || lastMessage.getText() == null) return false;

    // 初始化重复计数
    int duplicateCount = 0;
    // 从倒数第二条消息开始向前遍历，寻找与最后一条消息重复的助手消息
    for (int i = messageList.size() - 2; i >= 0; i--) {
        Message msg = messageList.get(i);
        // 如果找到消息类型为ASSISTANT且文本内容与最后一条消息相同的，则增加重复计数
        if (MessageType.ASSISTANT.equals(msg.getMessageType()) &&
                lastMessage.getText().equals(msg.getText())) {
            duplicateCount++;
        }
        // 如果重复次数达到或超过阈值，则认为对话卡住
        if (duplicateCount >= duplicateThreshold) return true;
    }

    // 如果没有找到足够的重复消息，则认为对话没有卡住
    return false;
}



    /**
     * 执行单个步骤
     *
     * @return 步骤执行结果
     */
    public abstract String step();

    /**
     * 清理资源
     */
    protected void cleanup() {
        // 子类可以重写此方法来清理资源
    }
}

