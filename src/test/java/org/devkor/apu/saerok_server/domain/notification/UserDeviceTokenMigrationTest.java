package org.devkor.apu.saerok_server.domain.notification;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserDeviceTokenMigrationTest {

    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
            DockerImageName.parse("postgis/postgis:16-3.5-alpine")
                    .asCompatibleSubstituteFor("postgres")
    );

    @BeforeAll
    static void startPostgres() {
        POSTGRES.start();
    }

    @AfterAll
    static void stopPostgres() {
        POSTGRES.stop();
    }

    @Test
    @DisplayName("V93은 중복/빈 token만 비활성화하고 디바이스와 알림 설정을 보존한다")
    void migration_preservesRowsAndSettingsWhileNormalizingTokens() throws Exception {
        migrateToVersion92();

        try (Connection connection = connection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    INSERT INTO users (id, joined_at) VALUES
                        (100001, now()),
                        (100002, now())
                    """);
            statement.executeUpdate("""
                    INSERT INTO user_device
                        (id, user_id, device_id, token, platform, created_at, updated_at)
                    VALUES
                        (900001, 100001, 'device-old', 'duplicate-token', 'IOS', now(), '2026-01-01T00:00:00Z'),
                        (900002, 100002, 'device-new', 'duplicate-token', 'IOS', now(), '2026-02-01T00:00:00Z'),
                        (900003, 100001, 'device-blank', '   ', 'IOS', now(), '2026-03-01T00:00:00Z')
                    """);
            statement.executeUpdate("""
                    INSERT INTO notification_setting
                        (id, user_device_id, type, enabled, created_at, updated_at)
                    VALUES
                        (910001, 900001, 'LIKED_ON_COLLECTION', FALSE, now(), now())
                    """);
        }

        migrateToLatest();

        try (Connection connection = connection(); Statement statement = connection.createStatement()) {
            assertThat(queryLong(statement, "SELECT COUNT(*) FROM user_device")).isEqualTo(3L);
            assertThat(queryString(statement, "SELECT token FROM user_device WHERE id = 900001")).isNull();
            assertThat(queryString(statement, "SELECT token FROM user_device WHERE id = 900002"))
                    .isEqualTo("duplicate-token");
            assertThat(queryString(statement, "SELECT token FROM user_device WHERE id = 900003")).isNull();
            assertThat(queryLong(statement, "SELECT COUNT(*) FROM notification_setting WHERE id = 910001"))
                    .isEqualTo(1L);
            assertThat(queryBoolean(statement, "SELECT enabled FROM notification_setting WHERE id = 910001"))
                    .isFalse();
            assertThat(queryBoolean(statement, """
                    SELECT is_nullable = 'YES'
                    FROM information_schema.columns
                    WHERE table_name = 'user_device' AND column_name = 'token'
                    """)).isTrue();

            statement.executeUpdate("INSERT INTO users (id, joined_at) VALUES (100003, now())");
            assertThatThrownBy(() -> statement.executeUpdate("""
                    INSERT INTO user_device
                        (id, user_id, device_id, token, platform, created_at, updated_at)
                    VALUES
                        (900004, 100003, 'device-duplicate', 'duplicate-token', 'IOS', now(), now())
                    """))
                    .isInstanceOf(SQLException.class);
        }
    }

    private void migrateToVersion92() {
        Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .locations("classpath:db/migration")
                .target(MigrationVersion.fromVersion("92"))
                .load()
                .migrate();
    }

    private void migrateToLatest() {
        Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .locations("classpath:db/migration")
                .load()
                .migrate();
    }

    private Connection connection() throws SQLException {
        return DriverManager.getConnection(
                POSTGRES.getJdbcUrl(),
                POSTGRES.getUsername(),
                POSTGRES.getPassword()
        );
    }

    private long queryLong(Statement statement, String sql) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery(sql)) {
            resultSet.next();
            return resultSet.getLong(1);
        }
    }

    private String queryString(Statement statement, String sql) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery(sql)) {
            resultSet.next();
            return resultSet.getString(1);
        }
    }

    private boolean queryBoolean(Statement statement, String sql) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery(sql)) {
            resultSet.next();
            return resultSet.getBoolean(1);
        }
    }
}
