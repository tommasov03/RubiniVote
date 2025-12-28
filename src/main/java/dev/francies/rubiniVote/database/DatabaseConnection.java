package dev.francies.rubiniVote.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseConnection {
    private static HikariDataSource dataSource;

    public static void connect(String dbHost, String dbPort, String dbName, String dbUsername, String dbPassword, FileConfiguration config) {
        HikariConfig hikariConfig = new HikariConfig();

        String jdbcUrl = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName + "?useSSL=false&autoReconnect=true";
        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setUsername(dbUsername);
        hikariConfig.setPassword(dbPassword);

        // Configurazione HikariCP dal config.yml
        hikariConfig.setMaximumPoolSize(config.getInt("database.pool-settings.maximum-pool-size", 10));
        hikariConfig.setMinimumIdle(config.getInt("database.pool-settings.minimum-idle", 10));
        hikariConfig.setMaxLifetime(config.getLong("database.pool-settings.maximum-lifetime", 1800000L));
        hikariConfig.setConnectionTimeout(config.getLong("database.pool-settings.connection-timeout", 5000L));
        hikariConfig.setIdleTimeout(config.getLong("database.pool-settings.idle-timeout", 600000L));
        hikariConfig.setPoolName(config.getString("database.pool-settings.pool-name", "RubiniVote-HikariPool"));

        // Inizializza il data source
        dataSource = new HikariDataSource(hikariConfig);

        // Creazione della tabella se non esiste
        try (Connection connection = getConnection()) {
            createTableIfNotExists(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new IllegalStateException("DataSource non inizializzato. Chiama il metodo connect prima di ottenere una connessione.");
        }
        return dataSource.getConnection();
    }

    public static void disconnect() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    private static void createTableIfNotExists(Connection connection) throws SQLException {
        String createTableSQL = """
                CREATE TABLE IF NOT EXISTS rubini (
                    uuid VARCHAR(36) NOT NULL PRIMARY KEY,
                    nickname VARCHAR(16) NOT NULL,
                    balance INT NOT NULL DEFAULT 0
                )
                """;

        try (PreparedStatement statement = connection.prepareStatement(createTableSQL)) {
            statement.executeUpdate();
        }
    }
}
