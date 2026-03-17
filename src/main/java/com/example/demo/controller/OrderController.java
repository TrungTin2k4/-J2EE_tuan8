package com.example.demo.controller;

import java.security.Principal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {

    @GetMapping("/order")
    public String order(Principal principal) {
        return "Order page for " + principal.getName();
    }
}
