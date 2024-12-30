package dev.francies.rubiniVote.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RubiniManager {

    public static void addRubini(String uuid, String nickname, int amount) throws SQLException {
        ensurePlayerExists(uuid, nickname);
        Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(
                "UPDATE rubini SET balance = balance + ? WHERE uuid = ?"
        );
        statement.setInt(1, amount);
        statement.setString(2, uuid); // Assicuriamoci che uuid sia passato come Stringa
        statement.executeUpdate();
    }

    public static void setRubini(String uuid, String nickname, int amount) throws SQLException {
        ensurePlayerExists(uuid, nickname);
        Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(
                "UPDATE rubini SET balance = ? WHERE uuid = ?"
        );
        statement.setInt(1, amount);
        statement.setString(2, uuid); // Assicuriamoci che uuid sia passato come Stringa
        statement.executeUpdate();
    }

    public static void takeRubini(String uuid, String nickname, int amount) throws SQLException {
        ensurePlayerExists(uuid, nickname);
        Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(
                "UPDATE rubini SET balance = balance - ? WHERE uuid = ? AND balance >= ?"
        );
        statement.setInt(1, amount);
        statement.setString(2, uuid); // Assicuriamoci che uuid sia passato come Stringa
        statement.setInt(3, amount);
        statement.executeUpdate();
    }

    private static void ensurePlayerExists(String uuid, String nickname) throws SQLException {
        Connection connection = DatabaseConnection.getConnection();
        PreparedStatement checkStatement = connection.prepareStatement(
                "SELECT 1 FROM rubini WHERE uuid = ?"
        );
        checkStatement.setString(1, uuid); // uuid deve essere una Stringa
        ResultSet resultSet = checkStatement.executeQuery();

        if (!resultSet.next()) {
            // Il giocatore non esiste, quindi lo aggiungiamo
            addOrUpdatePlayer(uuid, nickname, 0);
        }
    }



    public static String getPlayerAtPosition(int position) throws SQLException {
        Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(
                "SELECT nickname FROM rubini ORDER BY balance DESC LIMIT 1 OFFSET ?"
        );
        statement.setInt(1, position - 1); // position è un intero, quindi usiamo setInt
        ResultSet resultSet = statement.executeQuery();
        return resultSet.next() ? resultSet.getString("nickname") : "N/A";
    }

    public static int getRubiniAtPosition(int position) throws SQLException {
        Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(
                "SELECT balance FROM rubini ORDER BY balance DESC LIMIT 1 OFFSET ?"
        );
        statement.setInt(1, position - 1); // position è un intero, quindi usiamo setInt
        ResultSet resultSet = statement.executeQuery();
        return resultSet.next() ? resultSet.getInt("balance") : 0;
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
            statement.setString(1, uuid); // uuid è una Stringa
            statement.setString(2, nickname); // nickname è una Stringa
            statement.setInt(3, initialBalance); // balance è un intero
            statement.executeUpdate();
        }
    }

    public static int getRubini(String playerName) throws SQLException {
        Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(
                "SELECT balance FROM rubini WHERE uuid = ?"
        );
        statement.setString(1, playerName);
        ResultSet resultSet = statement.executeQuery();
        return resultSet.next() ? resultSet.getInt("balance") : 0;
    }

}
