package dev.francies.rubiniVote.database;

import dev.francies.rubiniVote.RubiniVote;
import dev.francies.rubiniVote.cache.RubiniCache;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class RubiniManager {

    public static CompletableFuture<Void> addRubini(String uuid, String nickname, int amount) {
        return CompletableFuture.runAsync(() -> {
            try {
                ensurePlayerExistsSync(uuid, nickname);
                try (Connection connection = DatabaseConnection.getConnection();
                     PreparedStatement statement = connection.prepareStatement(
                             "UPDATE rubini SET balance = balance + ? WHERE uuid = ?"
                     )) {
                    statement.setInt(1, amount);
                    statement.setString(2, uuid);
                    statement.executeUpdate();
                    
                    // Invalida la cache per aggiornamento immediato
                    RubiniCache.invalidateAndUpdate(uuid);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static CompletableFuture<Void> setRubini(String uuid, String nickname, int amount) {
        return CompletableFuture.runAsync(() -> {
            try {
                ensurePlayerExistsSync(uuid, nickname);
                try (Connection connection = DatabaseConnection.getConnection();
                     PreparedStatement statement = connection.prepareStatement(
                             "UPDATE rubini SET balance = ? WHERE uuid = ?"
                     )) {
                    statement.setInt(1, amount);
                    statement.setString(2, uuid);
                    statement.executeUpdate();
                    
                    // Invalida la cache per aggiornamento immediato
                    RubiniCache.invalidateAndUpdate(uuid);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static CompletableFuture<Void> takeRubini(String uuid, String nickname, int amount) {
        return CompletableFuture.runAsync(() -> {
            try {
                ensurePlayerExistsSync(uuid, nickname);
                try (Connection connection = DatabaseConnection.getConnection();
                     PreparedStatement statement = connection.prepareStatement(
                             "UPDATE rubini SET balance = balance - ? WHERE uuid = ? AND balance >= ?"
                     )) {
                    statement.setInt(1, amount);
                    statement.setString(2, uuid);
                    statement.setInt(3, amount);
                    statement.executeUpdate();
                    
                    // Invalida la cache per aggiornamento immediato
                    RubiniCache.invalidateAndUpdate(uuid);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static void ensurePlayerExistsSync(String uuid, String nickname) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement checkStatement = connection.prepareStatement(
                     "SELECT 1 FROM rubini WHERE uuid = ?"
             )) {
            checkStatement.setString(1, uuid);
            try (ResultSet resultSet = checkStatement.executeQuery()) {
                if (!resultSet.next()) {
                    addOrUpdatePlayerSync(uuid, nickname, 0);
                }
            }
        }
    }

    public static CompletableFuture<String> getPlayerAtPosition(int position) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT nickname FROM rubini ORDER BY balance DESC LIMIT 1 OFFSET ?"
                 )) {
                statement.setInt(1, position - 1);
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next() ? resultSet.getString("nickname") : "N/A";
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static CompletableFuture<Integer> getRubiniAtPosition(int position) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT balance FROM rubini ORDER BY balance DESC LIMIT 1 OFFSET ?"
                 )) {
                statement.setInt(1, position - 1);
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next() ? resultSet.getInt("balance") : 0;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static CompletableFuture<Void> addOrUpdatePlayer(String uuid, String nickname, int initialBalance) {
        return CompletableFuture.runAsync(() -> {
            try {
                addOrUpdatePlayerSync(uuid, nickname, initialBalance);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static void addOrUpdatePlayerSync(String uuid, String nickname, int initialBalance) throws SQLException {
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

    public static CompletableFuture<Integer> getRubini(String uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT balance FROM rubini WHERE uuid = ?"
                 )) {
                statement.setString(1, uuid);
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next() ? resultSet.getInt("balance") : 0;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    // Metodi sincroni per retrocompatibilità (deprecati)
    @Deprecated
    public static void addRubiniSync(String uuid, String nickname, int amount) throws SQLException {
        ensurePlayerExistsSync(uuid, nickname);
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE rubini SET balance = balance + ? WHERE uuid = ?"
             )) {
            statement.setInt(1, amount);
            statement.setString(2, uuid);
            statement.executeUpdate();
        }
    }

    @Deprecated
    public static int getRubiniSync(String uuid) throws SQLException {
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
