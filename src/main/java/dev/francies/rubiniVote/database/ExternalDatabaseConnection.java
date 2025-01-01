package dev.francies.rubiniVote.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.francies.rubiniVote.RubiniVote;

import java.sql.Connection;
import java.sql.SQLException;

public class ExternalDatabaseConnection {

    private static HikariDataSource dataSource;

    public static void connect() {
        HikariConfig config = new HikariConfig();
        String host = RubiniVote.getInstance().getConfig().getString("external_database.host");
        String port = RubiniVote.getInstance().getConfig().getString("external_database.port");
        String database = RubiniVote.getInstance().getConfig().getString("external_database.name");
        String username = RubiniVote.getInstance().getConfig().getString("external_database.username");
        String password = RubiniVote.getInstance().getConfig().getString("external_database.password");

        String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&autoReconnect=true";
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);

        config.setMaximumPoolSize(10);
        config.setMinimumIdle(1);
        config.setIdleTimeout(30000);
        config.setMaxLifetime(60000);
        config.setConnectionTimeout(30000);

        dataSource = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new IllegalStateException("DataSource non inizializzato.");
        }
        return dataSource.getConnection();
    }

    public static void disconnect() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
