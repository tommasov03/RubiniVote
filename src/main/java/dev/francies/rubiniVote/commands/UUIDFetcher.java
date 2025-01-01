package dev.francies.rubiniVote.commands;

import litebans.api.Database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class UUIDFetcher {

    /**
     * Restituisce un CompletableFuture che fornisce l'UUID dell'ultimo record trovato
     * in litebans_history per il nome specificato. Restituisce null se non trovato.
     *
     * @param playerName Nome del giocatore di cui ottenere l'UUID
     * @return CompletableFuture contenente l'UUID del giocatore o null se non trovato
     */
    public static CompletableFuture<UUID> getUUIDFromLiteBansAsync(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            String query = "SELECT uuid FROM litebans_history "
                    + "WHERE LOWER(name) = LOWER(?) "
                    + "ORDER BY date DESC LIMIT 1";

            try {
                PreparedStatement statement = Database.get().prepareStatement(query);
                statement.setString(1, playerName);

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        String uuidString = rs.getString("uuid");
                        if (uuidString == null) {
                            return null;
                        }

                        if (uuidString.contains("-")) {
                            return UUID.fromString(uuidString);
                        } else {
                            String uuidWithDashes = uuidString.replaceFirst(
                                    "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{12})",
                                    "$1-$2-$3-$4-$5"
                            );
                            return UUID.fromString(uuidWithDashes);
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
