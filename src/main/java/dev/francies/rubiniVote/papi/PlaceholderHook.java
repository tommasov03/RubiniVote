package dev.francies.rubiniVote.papi;

import dev.francies.rubiniVote.database.RubiniManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class PlaceholderHook extends PlaceholderExpansion {

    @Override
    public String getIdentifier() {
        return "rubini";
    }

    @Override
    public String getAuthor() {
        return "Francies";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        // Placeholder per nome del giocatore nella classifica
        if (params.startsWith("player_amount_")) {
            try {
                int position = Integer.parseInt(params.split("_")[2]);
                return RubiniManager.getPlayerAtPosition(position);
            } catch (Exception e) {
                return "N/A";
            }
        }

        // Placeholder per rubini del giocatore nella classifica
        if (params.startsWith("amount_")) {
            try {
                int position = Integer.parseInt(params.split("_")[1]);
                return String.valueOf(RubiniManager.getRubiniAtPosition(position));
            } catch (Exception e) {
                return "N/A";
            }
        }

        if (params.equalsIgnoreCase("bal")) {
            try {
                return String.valueOf(RubiniManager.getRubini(player.getUniqueId().toString()));
            } catch (SQLException e) {
                return "Errore";
            }
        }


        return null; // Placeholder non riconosciuto
    }
}
