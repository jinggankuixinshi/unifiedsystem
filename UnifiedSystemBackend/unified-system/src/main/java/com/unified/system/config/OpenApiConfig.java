package com.unified.system.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI unifiedSystemOpenApi() {
        return new OpenAPI().info(new Info()
                .title("UnifiedSystem 企业管理系统 API")
                .version("1.2.0")
                .description("RBAC / 审批工作流（分支·层级·委托）/ 生产·物流·销售·财务 / 人事账号操作"));
    }
}
