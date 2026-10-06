package com.medical;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SmartMedicalApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartMedicalApplication.class, args);
        System.out.println("========================================");
        System.out.println("智慧医疗助手系统启动成功！");
        System.out.println("访问地址: http://localhost:8082");
        System.out.println("========================================");
    }
}
