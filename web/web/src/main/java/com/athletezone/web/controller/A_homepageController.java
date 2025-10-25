package com.athletezone.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class A_homepageController {
    @GetMapping("A_homepage")
    public String homepage() {
        return "A_homepage";
    }
}
