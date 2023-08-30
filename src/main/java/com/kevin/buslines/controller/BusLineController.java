package com.kevin.buslines.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class BusLineController {

    @GetMapping("/top-bus-lines")
    public String getTopBusLines() {
        // placeholder
        return "Top Bus Lines Data Here";
    }

    // endpoints here ...
}
