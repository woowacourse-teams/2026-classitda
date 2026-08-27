package com.classitda.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;

@TestConfiguration(proxyBeanMethods = false)
public class SharedMySqlTestConfiguration {

    @Bean
    DynamicPropertyRegistrar mysqlProperties() {
        SharedTestContainers.MySqlDatabase database = SharedTestContainers.createMySqlDatabase();
        return registry -> {
            registry.add("spring.datasource.url", database::jdbcUrl);
            registry.add("spring.datasource.username", database::username);
            registry.add("spring.datasource.password", database::password);
            registry.add("spring.datasource.hikari.maximum-pool-size", () -> 4);
            registry.add("spring.datasource.hikari.minimum-idle", () -> 0);
        };
    }
}
