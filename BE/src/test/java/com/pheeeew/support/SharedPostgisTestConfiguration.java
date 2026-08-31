package com.pheeeew.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.postgresql.PostgreSQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class SharedPostgisTestConfiguration {

    @Bean
    DynamicPropertyRegistrar postgisProperties() {
        PostgreSQLContainer postgis = SharedTestContainers.postgis();
        return registry -> {
            registry.add("spring.datasource.url", postgis::getJdbcUrl);
            registry.add("spring.datasource.username", postgis::getUsername);
            registry.add("spring.datasource.password", postgis::getPassword);
        };
    }
}
