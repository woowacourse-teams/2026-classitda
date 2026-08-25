package com.classitda.common.image;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@EnableConfigurationProperties(ImageProperties.class)
@Configuration
public class ImageConfig {

    @Bean
    public S3Presigner s3Presigner(ImageProperties imageProperties) {
        return S3Presigner.builder()
                .region(Region.of(imageProperties.region()))
                .build();
    }
}
