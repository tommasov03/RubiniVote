package dev.francies.rubiniVote.papi;

import dev.francies.rubiniVote.cache.RubiniCache;
import dev.francies.rubiniVote.database.RubiniManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

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
        // Placeholder per nome del giocatore nella classifica (CACHED)
        if (params.startsWith("player_amount_")) {
            try {
                int position = Integer.parseInt(params.split("_")[2]);
                return RubiniCache.getTopPlayerName(position);
            } catch (Exception e) {
                return "N/A";
            }
        }

        // Placeholder per rubini del giocatore nella classifica (CACHED)
        if (params.startsWith("amount_")) {
            try {
                int position = Integer.parseInt(params.split("_")[1]);
                return String.valueOf(RubiniCache.getTopPlayerAmount(position));
            } catch (Exception e) {
                return "0";
            }
        }

        // Placeholder CACHED - ottimizzato per scoreboard/tablist (aggiornamento periodico)
        // Usa: %rubini_bal_cached% o %rubini_cached%
        if (params.equalsIgnoreCase("bal_cached") || params.equalsIgnoreCase("cached")) {
            return String.valueOf(RubiniCache.getCachedRubini(player.getUniqueId()));
        }

        // Placeholder REAL-TIME - accesso diretto al DB (controllo esatto)
        // Usa: %rubini_bal% - ideale per shop, transazioni, controlli precisi
        if (params.equalsIgnoreCase("bal")) {
            try {
                return String.valueOf(RubiniManager.getRubini(player.getUniqueId().toString()).join());
            } catch (Exception e) {
                return "Errore";
            }
        }


        return null; // Placeholder non riconosciuto
    }
}
