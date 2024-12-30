package dev.francies.rubiniVote.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseConnection {
    private static Connection connection;
    private static String host, port, database, username, password;

    public static void connect(String dbHost, String dbPort, String dbName, String dbUsername, String dbPassword) throws SQLException {

        host = dbHost;
        port = dbPort;
        database = dbName;
        username = dbUsername;
        password = dbPassword;

        reconnect();
    }

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed() || !connection.isValid(2)) {
                reconnect();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                reconnect();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return connection;
    }


    public static void disconnect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    private static void reconnect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }

        String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&autoReconnect=true";
        connection = DriverManager.getConnection(url, username, password);

        createTableIfNotExists();
    }

    private static void createTableIfNotExists() throws SQLException {
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
