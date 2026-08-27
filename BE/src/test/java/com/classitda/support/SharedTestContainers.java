package com.classitda.support;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicInteger;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

public final class SharedTestContainers {

    private static final AtomicInteger DATABASE_SEQUENCE = new AtomicInteger();

    private SharedTestContainers() {
    }

    public static MySqlDatabase createMySqlDatabase() {
        MySQLContainer mysql = MySqlHolder.INSTANCE;
        String databaseName = "classitda_test_%d".formatted(DATABASE_SEQUENCE.incrementAndGet());

        try (Connection connection = DriverManager.getConnection(
                mysql.getJdbcUrl(),
                "root",
                mysql.getPassword()
        ); Statement statement = connection.createStatement()) {
            statement.execute("CREATE DATABASE `%s`".formatted(databaseName));
            statement.execute("GRANT ALL PRIVILEGES ON `%s`.* TO '%s'@'%%'"
                    .formatted(databaseName, mysql.getUsername()));
        } catch (SQLException exception) {
            throw new IllegalStateException("테스트 데이터베이스를 생성할 수 없습니다.", exception);
        }

        String jdbcUrl = mysql.getJdbcUrl().replace(
                "/" + mysql.getDatabaseName(),
                "/" + databaseName
        );
        return new MySqlDatabase(jdbcUrl, mysql.getUsername(), mysql.getPassword());
    }

    public static GenericContainer<?> redis() {
        return RedisHolder.INSTANCE;
    }

    public record MySqlDatabase(String jdbcUrl, String username, String password) {
    }

    private static final class MySqlHolder {

        private static final MySQLContainer INSTANCE = start();

        private static MySQLContainer start() {
            MySQLContainer container = new MySQLContainer("mysql:8.4");
            container.start();
            return container;
        }
    }

    private static final class RedisHolder {

        private static final GenericContainer<?> INSTANCE = start();

        private static GenericContainer<?> start() {
            GenericContainer<?> container = new GenericContainer<>(
                    DockerImageName.parse("redis:7.4-alpine")
            ).withExposedPorts(6379);
            container.start();
            return container;
        }
    }
}
