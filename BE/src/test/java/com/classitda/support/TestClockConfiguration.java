package com.classitda.support;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

public final class TestClockConfiguration {

    private static final ZoneId SERVICE_ZONE_ID = ZoneId.of("Asia/Seoul");

    private TestClockConfiguration() {
    }

    private static Clock fixedAt(int day, int hour, int minute) {
        LocalDateTime dateTime = LocalDateTime.of(2026, 8, day, hour, minute);
        return Clock.fixed(dateTime.atZone(SERVICE_ZONE_ID).toInstant(), SERVICE_ZONE_ID);
    }

    @TestConfiguration(proxyBeanMethods = false)
    public static class August17AtTen {

        @Primary
        @Bean
        Clock fixedClock() {
            return fixedAt(17, 10, 0);
        }
    }

    @TestConfiguration(proxyBeanMethods = false)
    public static class August17AtNoon {

        @Primary
        @Bean
        Clock fixedClock() {
            return fixedAt(17, 12, 0);
        }
    }

    @TestConfiguration(proxyBeanMethods = false)
    public static class August24AtFifteenThirty {

        @Primary
        @Bean
        Clock fixedClock() {
            return fixedAt(24, 15, 30);
        }
    }

    @TestConfiguration(proxyBeanMethods = false)
    public static class August31AtFifteenThirty {

        @Primary
        @Bean
        Clock fixedClock() {
            return fixedAt(31, 15, 30);
        }
    }
}
