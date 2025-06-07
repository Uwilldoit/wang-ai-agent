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
                我的另一半是广东人，居住在上海静安区，请帮我找到 3 公里内合适的买菜地点，
                并结合菜谱，制定一份详细的做菜攻略
                并以 PDF 格式输出""";
        String answer = wangManus.run(userPrompt);
        Assertions.assertNotNull(answer);
    }
}

