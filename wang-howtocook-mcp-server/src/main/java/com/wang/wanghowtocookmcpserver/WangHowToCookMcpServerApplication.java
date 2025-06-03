package com.wang.wanghowtocookmcpserver;

import com.wang.wanghowtocookmcpserver.tools.HowToCookTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class WangHowToCookMcpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(WangHowToCookMcpServerApplication.class, args);
    }

    @Bean
    public ToolCallbackProvider howToCookTools(HowToCookTool howToCookTool){
        return MethodToolCallbackProvider.builder()
                .toolObjects(howToCookTool)
                .build();
    }

}
