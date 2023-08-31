package com.kevin.buslines.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping; // definera REST API endpoints.
import org.springframework.web.bind.annotation.PathVariable;  // definera REST API endpoints.
import org.springframework.web.bind.annotation.RequestMapping;  // definera REST API endpoints.
import org.springframework.web.bind.annotation.RestController;  // definera REST API endpoints.
import org.springframework.web.client.RestTemplate; //HTTP-begäran.
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.CrossOrigin;


import java.util.*;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api") //Grund-URL
public class BusLineController {

    @Autowired //instans av RestTemplate
    private RestTemplate restTemplate;

    @Value("${API_STOPSTWO:DEFAULT_VALUE}") // från .env filen
    private String API_STOPSTWO;

    private static final String EXTERNAL_API_URL = "https://api.sl.se/api2/LineData.json?model=JourneyPatternPointOnLine&key=%s&DefaultTransportModeCode=bus";

    @GetMapping("/top-bus-lines")
    public ResponseEntity<?> getTopBusLines() {
        try {
            String stopsUrl = String.format(EXTERNAL_API_URL, API_STOPSTWO);
            System.out.println("Requesting URL: " + stopsUrl);
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(stopsUrl, HttpMethod.GET, null,
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    });

            if (response.getStatusCode() != HttpStatus.OK) {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }

            Map<String, Object> body = response.getBody();

            if (body == null || !body.containsKey("ResponseData") || !(body.get("ResponseData") instanceof Map)) {
                return new ResponseEntity<>("Unexpected data format from API", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            Map<String, Object> responseData = (Map<String, Object>) body.get("ResponseData");
            if (!responseData.containsKey("Result") || !(responseData.get("Result") instanceof List)) {
                return new ResponseEntity<>("Unexpected data format in ResponseData", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            List<Map<String, Object>> allStops = (List<Map<String, Object>>) responseData.get("Result");

            Map<String, Integer> lineCounts = new HashMap<>();

            for (Map<String, Object> stop : allStops) {
                String lineNumber = stop.getOrDefault("LineNumber", "undefined").toString();
                String directionCode = stop.getOrDefault("DirectionCode", "undefined").toString();

                String key = lineNumber + "-" + directionCode;
                String lineKey = lineNumber;

                lineCounts.put(key, lineCounts.getOrDefault(key, 0) + 1);
                if (!lineCounts.containsKey(lineKey) || lineCounts.get(key) > lineCounts.get(lineKey)) {
                    lineCounts.put(lineKey, lineCounts.get(key));
                }
            }

            List<Map.Entry<String, Integer>> sortedLines = lineCounts.entrySet().stream()
                    .filter(entry -> !entry.getKey().contains("-"))
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(10)
                    .collect(Collectors.toList());

            return new ResponseEntity<>(sortedLines, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace(); 
            return new ResponseEntity<>("Internal Server Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private static final String STOP_NAMES_API_URL = "https://api.sl.se/api2/LineData.json?model=stop&key=%s&DefaultTransportModeCode=bus";

    @GetMapping("/bus-line-stops/{lineNumber}/{direction}")
    public ResponseEntity<?> getBusLineStops(@PathVariable String lineNumber, @PathVariable String direction) {
        String stopsUrl = String.format(EXTERNAL_API_URL, API_STOPSTWO);
        String stopNamesUrl = String.format(STOP_NAMES_API_URL, API_STOPSTWO);

        try {
            ResponseEntity<Map<String, Object>> stopsResponse = restTemplate.exchange(stopsUrl, HttpMethod.GET, null,
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    });

            ResponseEntity<Map<String, Object>> stopNamesResponse = restTemplate.exchange(stopNamesUrl, HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    });

            // Checking the validity of the responses
            if (stopsResponse.getStatusCode() != HttpStatus.OK || stopNamesResponse.getStatusCode() != HttpStatus.OK) {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }

            // Extracting relevant data from responses
            Map<String, Object> stopsBody = stopsResponse.getBody();
            Map<String, Object> stopNamesBody = stopNamesResponse.getBody();

            if (!isResponseDataValid(stopsBody) || !isResponseDataValid(stopNamesBody)) {
                return new ResponseEntity<>("Unexpected data format received from API",
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }

            // Extract "ResponseData" as a Map
            Map<String, Object> responseDataStops = (Map<String, Object>) stopsBody.get("ResponseData");
            Map<String, Object> responseDataStopNames = (Map<String, Object>) stopNamesBody.get("ResponseData");

            // Now, extract "Result" as a List of Maps
            List<Map<String, Object>> allStops = (List<Map<String, Object>>) responseDataStops.get("Result");
            List<Map<String, Object>> allStopNames = (List<Map<String, Object>>) responseDataStopNames.get("Result");

            List<Map<String, Object>> relevantStops = allStops.stream()
                    .filter(stop -> lineNumber.equals(stop.get("LineNumber"))
                            && direction.equals(stop.get("DirectionCode")))
                    .collect(Collectors.toList());

            List<String> stopsWithNames = relevantStops.stream().map(stop -> {
                Map<String, Object> foundStop = allStopNames.stream()
                        .filter(nameStop -> stop.get("JourneyPatternPointNumber")
                                .equals(nameStop.get("StopPointNumber")))
                        .findFirst()
                        .orElse(null);
                return foundStop != null ? foundStop.get("StopPointName").toString() : null;
            }).collect(Collectors.toList());

            return new ResponseEntity<>(stopsWithNames, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error fetching bus line stops", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean isResponseDataValid(Map<String, Object> body) {
        return body != null && body.containsKey("ResponseData") && body.get("ResponseData") instanceof Map
                && ((Map) body.get("ResponseData")).containsKey("Result")
                && ((Map) body.get("ResponseData")).get("Result") instanceof List;
    }
}