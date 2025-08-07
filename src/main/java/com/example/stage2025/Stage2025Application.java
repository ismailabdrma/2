package com.example.stage2025;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // Enable scheduling for tasks like product imports
public class Stage2025Application {

    public static void main(String[] args) {
        SpringApplication.run(Stage2025Application.class, args);
    }

}
