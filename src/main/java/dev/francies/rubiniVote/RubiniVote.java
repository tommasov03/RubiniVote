package dev.francies.rubiniVote;

import dev.francies.rubiniVote.commands.RubiniCommand;
import dev.francies.rubiniVote.database.DatabaseConnection;
import dev.francies.rubiniVote.database.RubiniManager;
import dev.francies.rubiniVote.papi.PlaceholderHook;
import fr.minuskube.inv.InventoryManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public final class RubiniVote extends JavaPlugin {

    private static RubiniVote instance;
    private InventoryManager inventoryManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        if (!isLiteBansInstalled()) {
            getLogger().severe("LiteBans non è installato! Disabilitazione del plugin...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        String host = getConfig().getString("database.host");
            String port = getConfig().getString("database.port");
            String database = getConfig().getString("database.name");
            String username = getConfig().getString("database.username");
            String password = getConfig().getString("database.password");

            DatabaseConnection.connect(host, port, database, username, password);
            getLogger().info("Connessione al database stabilita con successo!");

        startPlayerDataUpdater();
        getCommand("rubini").setExecutor(new RubiniCommand());

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceholderHook().register();
            getLogger().info("PlaceholderAPI rilevata! Placeholder registrati.");
        } else {
            getLogger().warning("PlaceholderAPI non trovata. I placeholder non funzioneranno.");
        }

        // Configura SmartInvs
        inventoryManager = new InventoryManager(this);
        inventoryManager.init();

        getLogger().info("RubiniVote è stato caricato correttamente!");
    }
    private boolean isLiteBansInstalled() {
        Plugin liteBans = Bukkit.getPluginManager().getPlugin("LiteBans");
        return liteBans != null && liteBans.isEnabled();
    }

    @Override
    public void onDisable() {
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
                try {
                    RubiniManager.addOrUpdatePlayer(
                            player.getUniqueId().toString(),
                            player.getName(),
                            0
                    );
                } catch (SQLException e) {
                    getLogger().severe("Errore durante l'aggiornamento dei dati per il giocatore: " + player.getName());
                    e.printStackTrace();
                }
            });
        }, 0L, 100L);
    }

}
