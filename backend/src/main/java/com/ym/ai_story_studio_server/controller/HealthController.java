package com.ym.ai_story_studio_server.controller;

import com.ym.ai_story_studio_server.annotation.NoAuth;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查Controller
 */
@RestController
public class HealthController {

    /**
     * 健康检查接口（免认证）
     */
    @NoAuth
    @GetMapping("/api/health")
    public String health(){
        return "ok";
    }
}
