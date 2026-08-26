package com.classitda;

import static org.assertj.core.api.Assertions.assertThatCode;

import com.classitda.support.MySqlRepositoryTest;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

@MySqlRepositoryTest
class SeedScriptValidationTest {

    private final DataSource dataSource;

    @Autowired
    SeedScriptValidationTest(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Test
    void 로컬_시드가_현재_스키마에서_실행된다() {
        // given
        ResourceDatabasePopulator populator = 시드를_초기화하는_스크립트("local-data.sql");

        // when / then
        assertThatCode(() -> populator.execute(dataSource)).doesNotThrowAnyException();
    }

    @Test
    void 개발_시드가_현재_스키마에서_실행된다() {
        // given
        ResourceDatabasePopulator populator = 시드를_초기화하는_스크립트("dev-data.sql");

        // when / then
        assertThatCode(() -> populator.execute(dataSource)).doesNotThrowAnyException();
    }

    private ResourceDatabasePopulator 시드를_초기화하는_스크립트(String dataScript) {
        return new ResourceDatabasePopulator(
                new ClassPathResource("reset-schema.sql"),
                new ClassPathResource("schema.sql"),
                new ClassPathResource(dataScript)
        );
    }
}
