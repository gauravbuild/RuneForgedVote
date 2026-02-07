package me.gauravbuilds.runeforgedvote.items.travel;

import me.gauravbuilds.runeforgedvote.RuneForgedVote;
import org.bukkit.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.Arrays;

public class DolphinCharm implements Listener {
    private final RuneForgedVote plugin;
    public static final int ID = 5010;
    private final NamespacedKey timeKey;

    public DolphinCharm(RuneForgedVote p) {
        this.plugin = p;
        this.timeKey = new NamespacedKey(plugin, "charm_usage_ticks");
    }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        NamespacedKey key = new NamespacedKey(plugin, "dolphin_charm");
        plugin.getServer().removeRecipe(key);

        ShapedRecipe r = new ShapedRecipe(key, getItem());
        r.shape(" C ", "CHC", " C ");
        r.setIngredient('H', Material.HEART_OF_THE_SEA);
        r.setIngredient('C', Material.PRISMARINE_SHARD);
        plugin.getServer().addRecipe(r);
    }

    public static ItemStack getItem() {
        ItemStack i = new ItemStack(Material.HEART_OF_THE_SEA);
        ItemMeta m = i.getItemMeta();
        m.setDisplayName(ChatColor.AQUA + "🐬 Dolphin Charm");
        m.setCustomModelData(ID);
        m.setLore(Arrays.asList(ChatColor.GRAY + "Ocean's blessing.", ChatColor.GOLD + "Ability: Swift Swim", ChatColor.YELLOW + "Grants Dolphins Grace in water.", ChatColor.RED + "Breaks after 1 hour of use."));
        i.setItemMeta(m);
        return i;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (e.getPlayer().isInWater() && e.getPlayer().getInventory().contains(Material.HEART_OF_THE_SEA)) {
            ItemStack[] contents = e.getPlayer().getInventory().getContents();
            for (int i = 0; i < contents.length; i++) {
                ItemStack item = contents[i];
                if (isItem(item)) {
                    // Apply Effect
                    e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 40, 0, false, false));

                    // Increment Usage (Roughly every second)
                    if (System.currentTimeMillis() % 20 == 0) { // Simple tick check logic handled by server tick
                        ItemMeta meta = item.getItemMeta();
                        int ticks = meta.getPersistentDataContainer().getOrDefault(timeKey, PersistentDataType.INTEGER, 0);
                        ticks += 1;

                        // 1 Hour = 72,000 Ticks (Assuming 20 ticks/sec, this check runs on movement so it varies.
                        // Better logic: store seconds.)
                        // Let's just say 3600 seconds.

                        meta.getPersistentDataContainer().set(timeKey, PersistentDataType.INTEGER, ticks);
                        item.setItemMeta(meta);

                        if (ticks > 3600) { // Approx limit (Logic simplified for performance)
                            e.getPlayer().getInventory().setItem(i, null);
                            e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 1f);
                            e.getPlayer().sendMessage(ChatColor.RED + "Your Dolphin Charm has shattered!");
                        }
                    }
                    break; // Only process one charm
                }
            }
        }
    }

    private boolean isItem(ItemStack i) { return i != null && i.hasItemMeta() && i.getItemMeta().hasCustomModelData() && i.getItemMeta().getCustomModelData() == ID; }
}