package com.classitda;

import static org.assertj.core.api.Assertions.assertThat;

import com.classitda.support.SharedTestContainers;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

class SeedScriptValidationTest {

    @Test
    void 로컬_시드가_현재_스키마에서_실행된다() {
        // given
        Flyway flyway = 시드_마이그레이션("classpath:db/local");

        // when
        MigrateResult result = flyway.migrate();

        // then
        assertThat(result.migrationsExecuted).isPositive();
    }

    @Test
    void 개발_시드가_현재_스키마에서_실행된다() {
        // given
        Flyway flyway = 시드_마이그레이션("classpath:db/dev");

        // when
        MigrateResult result = flyway.migrate();

        // then
        assertThat(result.migrationsExecuted).isPositive();
    }

    private Flyway 시드_마이그레이션(String seedLocation) {
        SharedTestContainers.MySqlDatabase database = SharedTestContainers.createMySqlDatabase();
        DataSource dataSource = new DriverManagerDataSource(
                database.jdbcUrl(), database.username(), database.password());

        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration", seedLocation)
                .load();
    }
}
