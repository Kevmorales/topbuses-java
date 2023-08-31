package com.kevin.buslines;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.boot.CommandLineRunner;
import io.github.cdimascio.dotenv.Dotenv;




@SpringBootApplication
public class BusLinesApplication implements CommandLineRunner {

    @Autowired
    private Environment env;

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();
        System.setProperty("API_STOPSTWO", dotenv.get("API_STOPSTWO"));
        SpringApplication.run(BusLinesApplication.class, args);
        System.out.println("API_STOPSTWO value: " + dotenv.get("API_STOPSTWO"));
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Override
    public void run(String... args) throws Exception {
        Dotenv dotenv = Dotenv.load();
        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
        });
    }
}