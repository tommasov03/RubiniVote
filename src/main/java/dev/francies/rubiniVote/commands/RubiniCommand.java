package dev.francies.rubiniVote.commands;

import dev.francies.rubiniVote.RubiniVote;
import dev.francies.rubiniVote.database.RubiniManager;
import dev.francies.rubiniVote.gui.RubiniGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.sql.SQLException;


public class RubiniCommand implements CommandExecutor {

    private final FileConfiguration config = RubiniVote.getInstance().getConfig();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player) {
                if (!sender.hasPermission("rubini.use")) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            config.getString("messages.no_permission", "&cNon hai il permesso di usare questo comando.")));
                    return true;
                }
                Player player = (Player) sender;
                RubiniGUI.open(player);
                return true;
            }else{
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    config.getString("messages.only_players", "&cQuesto comando può essere usato solo da un giocatore.")));}
            return true;
        }

        if (!sender.hasPermission("rubini.admin")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    config.getString("messages.no_permission", "&cNon hai il permesso di usare questo comando.")));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    config.getString("messages.usage", "&cUtilizzo: /rubini [give|set|take|balance] [player] [amount]")));
            return true;
        }

        String action = args[0].toLowerCase();
        String targetPlayerName = args[1];

        // Azioni che non richiedono amount
        if (action.equals("balance") || action.equals("bal")) {
            handleBalance(sender, targetPlayerName);
            return true;
        }

        int amount;

        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    config.getString("messages.invalid_amount", "&cL'importo deve essere un numero valido.")));
            return true;
        }

        UUIDFetcher.getUUIDFromDatabaseAsync(targetPlayerName).thenAccept(targetUUID -> {
            if (targetUUID == null) {
                sender.sendMessage(ChatColor.RED + config.getString("messages.uuid_not_found", "Giocatore non trovato nel database esterno."));
                return;
            }

            Bukkit.getScheduler().runTaskAsynchronously(RubiniVote.getInstance(), () -> {
                try {
                    switch (action) {
                        case "balance", "bal" -> sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.show_rubini")
                                .replace("%balance%", String.valueOf(RubiniManager.getRubini(targetUUID.toString())))
                                .replace("%player%", targetPlayerName)));
                        case "give" -> {
                            RubiniManager.addRubini(targetUUID.toString(), targetPlayerName, amount);
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                    config.getString("messages.give_success", "&aHai aggiunto &e%amount% &arubini a &e%player%.")
                                            .replace("%amount%", String.valueOf(amount))
                                            .replace("%player%", targetPlayerName)));
                        }
                        case "set" -> {
                            RubiniManager.setRubini(targetUUID.toString(), targetPlayerName, amount);
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                    config.getString("messages.set_success", "&aHai impostato i rubini di &e%player% &aa &e%amount%.")
                                            .replace("%amount%", String.valueOf(amount))
                                            .replace("%player%", targetPlayerName)));
                        }
                        case "take" -> {
                            RubiniManager.takeRubini(targetUUID.toString(), targetPlayerName, amount);
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                    config.getString("messages.take_success", "&aHai rimosso &e%amount% &arubini a &e%player%.")
                                            .replace("%amount%", String.valueOf(amount))
                                            .replace("%player%", targetPlayerName)));
                        }
                        default -> sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                config.getString("messages.invalid_action", "&cAzione non valida. Usa /rubini [give|set|take|balance|bal].")));
                    }
                } catch (SQLException e) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            config.getString("messages.db_error", "&cErrore durante l'accesso al database: %error%.")
                                    .replace("%error%", e.getMessage())));
                }
            });
        });



        return true;
    }
    private void handleBalance(CommandSender sender, String playerName) {
        UUIDFetcher.getUUIDFromDatabaseAsync(playerName).thenAccept(targetUUID -> {
            if (targetUUID == null) {
                sender.sendMessage(ChatColor.RED + config.getString("messages.uuid_not_found", "Giocatore non trovato nel database esterno."));
                return;
            }

            Bukkit.getScheduler().runTaskAsynchronously(RubiniVote.getInstance(), () -> {
                try {
                    int balance = RubiniManager.getRubini(targetUUID.toString());
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            config.getString("messages.show_rubini", "&e%player% ha &a%balance% rubini.")
                                    .replace("%player%", playerName)
                                    .replace("%balance%", String.valueOf(balance))));
                } catch (SQLException e) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            config.getString("messages.db_error", "&cErrore durante l'accesso al database: %error%.")
                                    .replace("%error%", e.getMessage())));
                }
            });
        });
    }
}
