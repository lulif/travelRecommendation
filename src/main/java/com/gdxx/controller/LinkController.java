package com.gdxx.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/*
 * 链接路由
 */
@Controller
public class LinkController {
    @GetMapping("/index")
    private String index() {
        return "index";
    }

    @GetMapping("/linkToRoute")
    private String linkToRoute() {
        return "route";
    }
}
