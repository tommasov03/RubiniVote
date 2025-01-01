package dev.francies.rubiniVote.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseConnection {
    private static HikariDataSource dataSource;

    public static void connect(String dbHost, String dbPort, String dbName, String dbUsername, String dbPassword) {
        HikariConfig config = new HikariConfig();

        String jdbcUrl = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName + "?useSSL=false&autoReconnect=true";
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(dbUsername);
        config.setPassword(dbPassword);

        // Opzioni di configurazione HikariCP
        config.setMaximumPoolSize(10); // Numero massimo di connessioni nel pool
        config.setMinimumIdle(2); // Numero minimo di connessioni inattive
        config.setIdleTimeout(30000); // Timeout per connessioni inattive (ms)
        config.setMaxLifetime(1800000); // Tempo massimo di vita delle connessioni (ms)
        config.setConnectionTimeout(30000); // Timeout per ottenere una connessione (ms)

        // Inizializza il data source
        dataSource = new HikariDataSource(config);

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
