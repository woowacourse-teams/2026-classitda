package com.classitda;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.classitda.support.MySqlRepositoryTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

@MySqlRepositoryTest
class EntitySchemaValidationTest {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    EntitySchemaValidationTest(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Test
    void 모든_엔티티가_schema_sql과_일치한다() {
        // 컨텍스트 초기화 시 Hibernate ddl-auto=validate가 스키마 정합성을 검증한다.
    }

    @Test
    void 정리되지_않은_회원은_전화번호가_필수다() {
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO member (name, phone_number, cleaned_up_at, created_at)
                VALUES ('회원', NULL, NULL, CURRENT_TIMESTAMP(6))
                """))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("chk_member_phone_by_cleanup");
    }

    @Test
    void 정리된_회원은_전화번호를_보관할_수_없다() {
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO member (name, phone_number, cleaned_up_at, created_at)
                VALUES ('탈퇴한 회원', '01012345678', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6))
                """))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("chk_member_phone_by_cleanup");
    }
}
