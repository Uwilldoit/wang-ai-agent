package com.wang.wanghowtocookmcpserver.tools;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;



/**
 * @author: Shajia Wang
 * @createTime: 2025/6/3---16:09
 * @description:
 */
@SpringBootTest
class HowToCookToolTest {

    @Resource
    private HowToCookTool howToCookTool;

    @Test
    void searchRecipes(){
        List<HowToCookTool.Recipe> recipes = howToCookTool.searchRecipes("鸡");

        Assertions.assertNotNull(recipes);
    }
}