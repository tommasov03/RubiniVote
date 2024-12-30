package dev.francies.rubiniVote.gui;

import dev.francies.rubiniVote.RubiniVote;
import dev.francies.rubiniVote.database.RubiniManager;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.SlotPos;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RubiniGUI {
    private static final FileConfiguration config = RubiniVote.getInstance().getConfig();

    public static void open(Player player) {
        String title = ChatColor.translateAlternateColorCodes('&',
                config.getString("gui.title", "&aRubini GUI"));

        int rows = config.getInt("gui.rows", 1);
        if (rows < 1 || rows > 6) rows = 1;

        SmartInventory inventory = SmartInventory.builder()
                .id("rubini")
                .manager(RubiniVote.getInstance().getInventoryManager())
                .provider(new InventoryProvider() {
                    @Override
                    public void init(Player player, InventoryContents contents) {
                        List<Map<?, ?>> items = config.getMapList("gui.items");

                        for (Map<?, ?> itemConfig : items) {
                            try {
                                if (!itemConfig.containsKey("slot") || !itemConfig.containsKey("material") ||
                                        !itemConfig.containsKey("display_name") || !itemConfig.containsKey("lore")) {
                                    player.sendMessage(ChatColor.RED + "Errore nella configurazione dell'oggetto GUI: " + itemConfig);
                                    continue;
                                }

                                int slot = (int) itemConfig.get("slot");
                                int row = slot / 9;
                                int col = slot % 9;

                                Material material = Material.valueOf(((String) itemConfig.get("material")).toUpperCase());
                                String displayName = PlaceholderAPI.setPlaceholders(player, (String) itemConfig.get("display_name"));

                                ItemStack item = new ItemStack(material);
                                ItemMeta meta = item.getItemMeta();

                                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));

                                List<String> loreConfig = (List<String>) itemConfig.get("lore");
                                List<String> lore = new ArrayList<>();

                                for (String line : loreConfig) {
                                    // Sostituzione diretta di %rubini_bal% con il saldo
                                    if (line.contains("%rubini_bal%")) {
                                        int balance;
                                        try {
                                            balance = RubiniManager.getRubini(player.getUniqueId().toString());
                                        } catch (SQLException e) {
                                            balance = 0; // Gestione dell'errore di SQL
                                        }
                                        line = line.replace("%rubini_bal%", String.valueOf(balance));
                                    }
                                    // Usa PlaceholderAPI per altri placeholder
                                    lore.add(ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, line)));
                                }

                                meta.setLore(lore);
                                item.setItemMeta(meta);

                                contents.set(SlotPos.of(row, col), ClickableItem.empty(item));
                            } catch (Exception e) {
                                player.sendMessage(ChatColor.RED + "Errore nella configurazione dell'oggetto GUI: " + itemConfig);
                            }
                        }
                    }

                    @Override
                    public void update(Player player, InventoryContents contents) {

                    }
                })
                .size(rows, 9)
                .title(title)
                .build();

        inventory.open(player);
    }


    private static String replacePlaceholdersPapi(String text, Player player) {
        String result = PlaceholderAPI.setPlaceholders(player, text);
        player.sendMessage("DEBUG: PlaceholderAPI ha restituito: " + result);
        return result;
    }
}
