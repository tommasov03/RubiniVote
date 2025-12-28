package dev.francies.rubiniVote;

import dev.francies.rubiniVote.cache.RubiniCache;
import dev.francies.rubiniVote.commands.RubiniCommand;
import dev.francies.rubiniVote.database.DatabaseConnection;
import dev.francies.rubiniVote.database.ExternalDatabaseConnection;
import dev.francies.rubiniVote.database.RubiniManager;
import dev.francies.rubiniVote.listeners.PlayerCacheListener;
import dev.francies.rubiniVote.papi.PlaceholderHook;
import fr.minuskube.inv.InventoryManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public final class RubiniVote extends JavaPlugin {

    private static RubiniVote instance;
    private InventoryManager inventoryManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        String host = getConfig().getString("database.host");
            String port = getConfig().getString("database.port");
            String database = getConfig().getString("database.name");
            String username = getConfig().getString("database.username");
            String password = getConfig().getString("database.password");

            DatabaseConnection.connect(host, port, database, username, password, getConfig());
            getLogger().info("Connessione al database stabilita con successo!");
        try {
            ExternalDatabaseConnection.connect();
        } catch (Exception e) {
            getLogger().severe("Errore durante la connessione al database esterno: " + e.getMessage());
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        startPlayerDataUpdater();
        getCommand("rubini").setExecutor(new RubiniCommand());

        // Registra listener per gestire la cache
        getServer().getPluginManager().registerEvents(new PlayerCacheListener(), this);

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceholderHook().register();
            getLogger().info("PlaceholderAPI rilevata! Placeholder registrati.");
        } else {
            getLogger().warning("PlaceholderAPI non trovata. I placeholder non funzioneranno.");
        }

        // Configura SmartInvs
        inventoryManager = new InventoryManager(this);
        inventoryManager.init();

        // Avvia cache rubini se abilitata
        if (getConfig().getBoolean("cache.enabled", true)) {
            int updateInterval = getConfig().getInt("cache.update-interval", 10);
            RubiniCache.start(updateInterval);
        } else {
            getLogger().info("Cache rubini disabilitata nella configurazione");
        }

        getLogger().info("RubiniVote è stato caricato correttamente!");
    }

    @Override
    public void onDisable() {
            RubiniCache.stop();
            DatabaseConnection.disconnect();
        getLogger().info("RubiniVote è stato disabilitato.");
    }

    public static RubiniVote getInstance() {
        return instance;
    }

    public InventoryManager getInventoryManager() {
        return inventoryManager;
    }

private void startPlayerDataUpdater() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            Bukkit.getOnlinePlayers().forEach(player -> {
                RubiniManager.addOrUpdatePlayer(
                        player.getUniqueId().toString(),
                        player.getName(),
                        0
                ).exceptionally(throwable -> {
                    getLogger().severe("Errore durante l'aggiornamento dei dati per il giocatore: " + player.getName());
                    throwable.printStackTrace();
                    return null;
                });
            });
        }, 0L, 100L);
    }

}
