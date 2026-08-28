package com.classitda.authentication.infra.sms;

import com.classitda.authentication.application.phone.SmsSender;
import java.time.Clock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestClient;

@Import(SmsConfig.SensConfig.class)
@Configuration(proxyBeanMethods = false)
public class SmsConfig {

    @Profile("local")
    @ConditionalOnProperty(name = "auth.sms.provider", havingValue = "local")
    @Bean
    public SmsSender localSmsSender() {
        return new LocalNoopSmsSender();
    }

    @ConditionalOnProperty(name = "auth.sms.provider", havingValue = "sens")
    @EnableConfigurationProperties(NcpSensProperties.class)
    @Configuration(proxyBeanMethods = false)
    static class SensConfig {

        @Bean
        public NcpApiSignatureGenerator ncpApiSignatureGenerator() {
            return new NcpApiSignatureGenerator();
        }

        @Bean
        public NcpSensSmsSender ncpSensSmsSender(
                RestClient.Builder restClientBuilder,
                NcpSensProperties properties,
                NcpApiSignatureGenerator signatureGenerator,
                Clock clock
        ) {
            RestClient restClient = restClientBuilder
                    .baseUrl(NcpSensSmsSender.BASE_URL)
                    .build();
            return new NcpSensSmsSender(restClient, properties, signatureGenerator, clock);
        }
    }
}
