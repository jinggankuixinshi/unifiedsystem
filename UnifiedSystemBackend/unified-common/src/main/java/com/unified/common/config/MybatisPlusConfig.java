package com.unified.common.config;

import com.unified.common.security.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;

import java.time.LocalDateTime;

@Slf4j
@Configuration
public class MybatisPlusConfig {

    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                LocalDateTime now = LocalDateTime.now();
                this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
                this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
                try {
                    UserContext ctx = UserContext.get();
                    if (ctx != null && ctx.getUserId() != null) {
                        this.strictInsertFill(metaObject, "createBy", Long.class, ctx.getUserId());
                        this.strictInsertFill(metaObject, "updateBy", Long.class, ctx.getUserId());
                    }
                } catch (Exception ignored) {
                }
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
                try {
                    UserContext ctx = UserContext.get();
                    if (ctx != null && ctx.getUserId() != null) {
                        this.strictUpdateFill(metaObject, "updateBy", Long.class, ctx.getUserId());
                    }
                } catch (Exception ignored) {
                }
            }
        };
    }
}
