package com.wang.wangaiagent.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author: Shajia Wang
 * @createTime: 2025/5/22---15:42
 * @description:
 */

@SpringBootTest
class LoveAppDocumentLoaderTest {

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    @Test
    void load(){
        loveAppDocumentLoader.loadMarkdowns();
    }
}