package com.decisionhub.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RoleTestController {

    @GetMapping("/api/admin/test")
    public String adminTest() {
        return "Welcome Admin";
    }

    @GetMapping("/api/user/test")
    public String userTest() {
        return "Welcome User";
    }
}