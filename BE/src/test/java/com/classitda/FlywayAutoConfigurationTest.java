package com.classitda;

import static org.assertj.core.api.Assertions.assertThat;

import com.classitda.support.SharedTestContainers;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

class FlywayAutoConfigurationTest {

    private static final String AUTO_CONFIGURATION_IMPORTS =
            "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports";

    private static final String FLYWAY_AUTO_CONFIGURATION =
            "org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration";

    @Test
    void 플라이웨이_자동설정이_클래스패스에_등록되어_있다() throws IOException {
        // given
        List<String> registered = 등록된_자동설정();

        // when / then
        assertThat(registered).contains(FLYWAY_AUTO_CONFIGURATION);
    }

    @Test
    void 자동설정만으로_빈_데이터베이스에_스키마가_만들어진다() {
        // given
        SharedTestContainers.MySqlDatabase database = SharedTestContainers.createRawMySqlDatabase();
        DataSource dataSource = new DriverManagerDataSource(
                database.jdbcUrl(), database.username(), database.password());

        // when
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(FlywayAutoConfiguration.class))
                .withBean(DataSource.class, () -> dataSource)
                .withPropertyValues(
                        "spring.flyway.baseline-on-migrate=true",
                        "spring.flyway.baseline-version=1",
                        "spring.flyway.locations=classpath:db/migration"
                )
                .run(context -> {
                    // then
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(Flyway.class);

                    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
                    List<String> applied = jdbcTemplate.queryForList(
                            "SELECT version FROM flyway_schema_history WHERE success = 1 AND version IS NOT NULL",
                            String.class);
                    assertThat(applied).contains("1");

                    Integer tables = jdbcTemplate.queryForObject(
                            "SELECT COUNT(*) FROM information_schema.tables "
                                    + "WHERE table_schema = DATABASE() AND table_name = 'auth_account'",
                            Integer.class);
                    assertThat(tables).isEqualTo(1);
                });
    }

    private List<String> 등록된_자동설정() throws IOException {
        List<String> registered = new ArrayList<>();
        Enumeration<URL> resources = getClass().getClassLoader().getResources(AUTO_CONFIGURATION_IMPORTS);
        while (resources.hasMoreElements()) {
            try (InputStream stream = resources.nextElement().openStream()) {
                registered.addAll(new String(stream.readAllBytes(), StandardCharsets.UTF_8).lines().toList());
            }
        }
        return registered;
    }
}
