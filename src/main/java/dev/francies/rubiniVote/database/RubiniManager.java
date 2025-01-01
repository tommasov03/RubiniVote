package dev.francies.rubiniVote.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RubiniManager {

    public static void addRubini(String uuid, String nickname, int amount) throws SQLException {
        ensurePlayerExists(uuid, nickname);
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE rubini SET balance = balance + ? WHERE uuid = ?"
             )) {
            statement.setInt(1, amount);
            statement.setString(2, uuid);
            statement.executeUpdate();
        }
    }

    public static void setRubini(String uuid, String nickname, int amount) throws SQLException {
        ensurePlayerExists(uuid, nickname);
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE rubini SET balance = ? WHERE uuid = ?"
             )) {
            statement.setInt(1, amount);
            statement.setString(2, uuid);
            statement.executeUpdate();
        }
    }

    public static void takeRubini(String uuid, String nickname, int amount) throws SQLException {
        ensurePlayerExists(uuid, nickname);
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE rubini SET balance = balance - ? WHERE uuid = ? AND balance >= ?"
             )) {
            statement.setInt(1, amount);
            statement.setString(2, uuid);
            statement.setInt(3, amount);
            statement.executeUpdate();
        }
    }

    private static void ensurePlayerExists(String uuid, String nickname) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement checkStatement = connection.prepareStatement(
                     "SELECT 1 FROM rubini WHERE uuid = ?"
             )) {
            checkStatement.setString(1, uuid);
            try (ResultSet resultSet = checkStatement.executeQuery()) {
                if (!resultSet.next()) {
                    addOrUpdatePlayer(uuid, nickname, 0);
                }
            }
        }
    }

    public static String getPlayerAtPosition(int position) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT nickname FROM rubini ORDER BY balance DESC LIMIT 1 OFFSET ?"
             )) {
            statement.setInt(1, position - 1);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getString("nickname") : "N/A";
            }
        }
    }

    public static int getRubiniAtPosition(int position) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT balance FROM rubini ORDER BY balance DESC LIMIT 1 OFFSET ?"
             )) {
            statement.setInt(1, position - 1);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt("balance") : 0;
            }
        }
    }

    public static void addOrUpdatePlayer(String uuid, String nickname, int initialBalance) throws SQLException {
        String upsertSQL = """
                INSERT INTO rubini (uuid, nickname, balance)
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE
                nickname = VALUES(nickname)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(upsertSQL)) {
            statement.setString(1, uuid);
            statement.setString(2, nickname);
            statement.setInt(3, initialBalance);
            statement.executeUpdate();
        }
    }

    public static int getRubini(String uuid) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT balance FROM rubini WHERE uuid = ?"
             )) {
            statement.setString(1, uuid);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt("balance") : 0;
            }
        }
    }
}
