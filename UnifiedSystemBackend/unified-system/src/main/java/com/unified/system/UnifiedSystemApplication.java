package com.unified.system;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = "com.unified")
@MapperScan({"com.unified.system.mapper", "com.unified.common.workflow.mapper",
        "com.unified.common.sequence",
        "com.unified.production.mapper", "com.unified.logistics.mapper",
        "com.unified.sales.mapper", "com.unified.finance.mapper"})
@EnableAsync
public class UnifiedSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(UnifiedSystemApplication.class, args);
    }
}
