package com.unified.report.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * JimuReport 报表引擎配置
 * 报表数据源、异步生成、权限控制
 */
@Configuration
public class JimuReportConfig {

    @Bean
    @ConfigurationProperties(prefix = "jimureport")
    public JimuReportProperties jimuReportProperties() {
        return new JimuReportProperties();
    }

    public static class JimuReportProperties {
        private String dataSource = "system";
        private Boolean async = true;
        private Integer timeout = 120;
        private String exportDir = "./reports";
        private Long maxRows = 50000L;
        private String[] allowedRoles = { "admin", "boss", "finance_staff", "finance_manager" };

        public String getDataSource() { return dataSource; }
        public void setDataSource(String dataSource) { this.dataSource = dataSource; }
        public Boolean getAsync() { return async; }
        public void setAsync(Boolean async) { this.async = async; }
        public Integer getTimeout() { return timeout; }
        public void setTimeout(Integer timeout) { this.timeout = timeout; }
        public String getExportDir() { return exportDir; }
        public void setExportDir(String exportDir) { this.exportDir = exportDir; }
        public Long getMaxRows() { return maxRows; }
        public void setMaxRows(Long maxRows) { this.maxRows = maxRows; }
        public String[] getAllowedRoles() { return allowedRoles; }
        public void setAllowedRoles(String[] allowedRoles) { this.allowedRoles = allowedRoles; }
    }
}
