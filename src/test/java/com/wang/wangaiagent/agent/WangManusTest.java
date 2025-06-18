package com.wang.wangaiagent.agent;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author: Shajia Wang
 * @createTime: 2025/6/7---11:22
 * @description:
 */

@SpringBootTest
class WangManusTest {

    @Resource
    private WangManus wangManus;

    @Test
    void run() {
        String userPrompt = """
                我的另一半比较喜欢吃鸡，参数用中文，使用我提供的MCP服务，推荐菜单给我""";
        String answer = wangManus.run(userPrompt);
        Assertions.assertNotNull(answer);
    }
}

