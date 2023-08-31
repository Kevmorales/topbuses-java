package com.kevin.buslines.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/api")
public class BusLineController {

    @Autowired
    private RestTemplate restTemplate;

    @Value("#{systemEnvironment['API_STOPSTWO'] ?: 'DEFAULT_VALUE'}")
    private String API_STOPSTWO;

    private static final String EXTERNAL_API_URL = "https://api.sl.se/api2/LineData.json?model=JourneyPatternPointOnLine&key=%s&DefaultTransportModeCode=bus";

    @GetMapping("/top-bus-lines")
    public List<Map.Entry<String, Integer>> getTopBusLines() {
        String stopsUrl = String.format(EXTERNAL_API_URL, API_STOPSTWO);
        String response = restTemplate.getForObject(stopsUrl, String.class);

        // logiken här
        //placeholder
        return new ArrayList<>();
    }
}
