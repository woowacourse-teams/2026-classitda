package com.classitda;

import com.classitda.support.MySqlRepositoryTest;
import org.junit.jupiter.api.Test;

@MySqlRepositoryTest
class EntitySchemaValidationTest {

    @Test
    void 모든_엔티티가_schema_sql과_일치한다() {
        // 컨텍스트 초기화 시 Hibernate ddl-auto=validate가 스키마 정합성을 검증한다.
    }
}
