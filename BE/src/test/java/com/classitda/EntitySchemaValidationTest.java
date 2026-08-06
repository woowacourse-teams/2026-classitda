package com.classitda;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;

@Testcontainers
@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.sql.init.mode=always"
})
class EntitySchemaValidationTest {

    @Container
    @ServiceConnection
    static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.4");

    @Test
    void 모든_엔티티가_schema_sql과_일치한다() {
        // given / when / then
        // 컨텍스트 로딩 시 schema.sql 적용 후 Hibernate가 ddl-auto=validate 로 매핑을 대조한다.
        // 불일치가 있으면 컨텍스트 로딩 자체가 실패한다.
    }
}
