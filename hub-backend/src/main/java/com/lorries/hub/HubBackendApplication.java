package com.lorries.hub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 络绎智慧交通管理系统 - 枢纽端后端启动类
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class HubBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(HubBackendApplication.class, args);
    }
}
