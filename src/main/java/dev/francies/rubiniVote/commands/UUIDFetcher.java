package dev.francies.rubiniVote.commands;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import org.bukkit.Bukkit;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class UUIDFetcher {

    /**
     * Restituisce un CompletableFuture contenente l'UUID di un giocatore
     * utilizzando l'API di EssentialsX.
     *
     * @param playerName Nome del giocatore di cui ottenere l'UUID
     * @return CompletableFuture contenente l'UUID del giocatore o null se non trovato
     */
    public static CompletableFuture<UUID> getUUIDFromEssentialsAsync(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Essentials essentials = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");

                if (essentials == null) {
                    throw new IllegalStateException("EssentialsX non è installato!");
                }

                // Ottieni il giocatore tramite Essentials
                User user = essentials.getUser(playerName);
                if (user != null) {
                    return user.getUUID();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null; // Restituisce null se il giocatore non è trovato
        });
    }
}
