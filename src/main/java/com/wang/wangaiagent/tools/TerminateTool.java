package com.wang.wangaiagent.tools;

import org.springframework.ai.tool.annotation.Tool;

/**
 * @author: Shajia Wang
 * @createTime: 2025/6/7---11:16
 * @description:
 */

public class TerminateTool {

    @Tool(description = """  
            Terminate the interaction when the request is met OR if the assistant cannot proceed further with the task.  
            "When you have finished all the tasks, call this tool to end the work.  
            """)
    public String doTerminate() {
        return "任务结束";
    }
}

