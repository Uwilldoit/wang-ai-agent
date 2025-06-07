package com.wang.wangaiagent.agent.model;

/**
 * @author: Shajia Wang
 * @createTime: 2025/6/4---15:53
 * @description: 代理执行状态的枚举类
 */

public enum AgentState {

    /**
     * 空闲状态
     */
    IDLE,

    /**
     * 运行中状态
     */
    RUNNING,

    /**
     * 已完成状态
     */
    FINISHED,

    /**
     * 错误状态
     */
    ERROR
}

