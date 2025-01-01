package dev.francies.rubiniVote.commands;

import dev.francies.rubiniVote.RubiniVote;
import dev.francies.rubiniVote.database.ExternalDatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class UUIDFetcher {

    public static CompletableFuture<UUID> getUUIDFromDatabaseAsync(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = ExternalDatabaseConnection.getConnection()) {
                String queryTemplate = RubiniVote.getInstance().getConfig().getString("external_database.uuid_query");
                String query = queryTemplate.replace("%player%", playerName);

                try (PreparedStatement statement = connection.prepareStatement(query);
                     ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        String uuidString = resultSet.getString("uniqueId");
                        if (uuidString != null) {
                            return UUID.fromString(uuidString);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        });
    }
}
