package com.wanted.momocity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class LegendMomoCityApplication {

    public static void main(String[] args) {
        SpringApplication.run(LegendMomoCityApplication.class, args);
    }

}
