package dev.francies.rubiniVote.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.francies.rubiniVote.RubiniVote;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.SQLException;

public class ExternalDatabaseConnection {

    private static HikariDataSource dataSource;

    public static void connect() {
        FileConfiguration config = RubiniVote.getInstance().getConfig();
        
        HikariConfig hikariConfig = new HikariConfig();
        String host = config.getString("external_database.host");
        String port = config.getString("external_database.port");
        String database = config.getString("external_database.name");
        String username = config.getString("external_database.username");
        String password = config.getString("external_database.password");

        String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&autoReconnect=true";
        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);

        // Configurazione HikariCP dal config.yml
        hikariConfig.setMaximumPoolSize(config.getInt("external_database.pool-settings.maximum-pool-size", 10));
        hikariConfig.setMinimumIdle(config.getInt("external_database.pool-settings.minimum-idle", 10));
        hikariConfig.setMaxLifetime(config.getLong("external_database.pool-settings.maximum-lifetime", 1800000L));
        hikariConfig.setConnectionTimeout(config.getLong("external_database.pool-settings.connection-timeout", 5000L));
        hikariConfig.setIdleTimeout(config.getLong("external_database.pool-settings.idle-timeout", 600000L));
        hikariConfig.setPoolName(config.getString("external_database.pool-settings.pool-name", "RubiniVote-External-HikariPool"));

        dataSource = new HikariDataSource(hikariConfig);
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
