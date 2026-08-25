package com.classitda.support;

import com.classitda.common.image.ImageProperties;
import java.time.Duration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration(proxyBeanMethods = false)
public class ImageTestConfiguration {

    public static final String BASE_URL = "https://images.test";

    @Bean
    ImageProperties imageProperties() {
        return new ImageProperties(
                "techcourse-project-2026",
                "classitda",
                BASE_URL,
                "ap-northeast-2",
                Duration.ofMinutes(5)
        );
    }
}
