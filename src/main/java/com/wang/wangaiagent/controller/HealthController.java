package com.wang.wangaiagent.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wang
 * @createTime:2025/5/7---21:44
 * @description:
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping("ok")
    public String health(){
        return "ok";
    }

}
