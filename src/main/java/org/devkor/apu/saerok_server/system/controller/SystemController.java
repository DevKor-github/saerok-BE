package org.devkor.apu.saerok_server.system.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/system")
public class SystemController {

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @GetMapping("/ping")
    public String ping() {
        return "pong from saerok-server";
    }

    @GetMapping("/profile")
    public String getActiveProfile() {
        return "현재 활성화된 프로필은: " + activeProfile + "입니다!";
    }
}
