package com.kevin.buslines;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import io.github.cdimascio.dotenv.Dotenv;


@SpringBootApplication
public class BusLinesApplication {

Dotenv dotenv = Dotenv.load();
String apiKey = dotenv.get("API_STOPSTWO");

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public static void main(String[] args) {
        SpringApplication.run(BusLinesApplication.class, args);
    }
}
