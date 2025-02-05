package com.example.demo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api")
public class TestController {
    @GetMapping("get")
    public String getTest() {
        return "GET 요청 응답";
    }

    @PostMapping("/post")
    public String postTest() {
        return "POST 요청 응답";
    }
}
