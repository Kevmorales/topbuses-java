package com.kevin.buslines.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BusLineController {
    
    @GetMapping("/test")
    public String testEndpoint() {
        return "Hello from Spring Boot!";
    }
}
