package me.gauravbuilds.runeforgedvote.items.mining;

import me.gauravbuilds.runeforgedvote.RuneForgedVote;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.*;

public class AetherMagnet implements Listener {
    private final RuneForgedVote plugin;
    public static final int ID = 2006;
    private final Map<UUID, Long> activeMagnets = new HashMap<>();

    public AetherMagnet(RuneForgedVote p) { this.plugin = p; }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        NamespacedKey key = new NamespacedKey(plugin, "aether_magnet");
        plugin.getServer().removeRecipe(key); // FIX

        ShapedRecipe r = new ShapedRecipe(key, getItem());
        r.shape(" A ", "AIA", " A ");
        r.setIngredient('I', Material.IRON_INGOT);
        r.setIngredient('A', Material.AMETHYST_SHARD);
        plugin.getServer().addRecipe(r);
    }

    public static ItemStack getItem() {
        ItemStack i = new ItemStack(Material.COMPASS);
        ItemMeta m = i.getItemMeta();
        m.setDisplayName(ChatColor.LIGHT_PURPLE + "🧲 Aether Magnet");
        m.setCustomModelData(ID);
        m.setLore(Arrays.asList(ChatColor.GRAY + "Pulls the void towards you.", ChatColor.GOLD + "Ability: Void Pull", ChatColor.YELLOW + "Right-click to activate.", ChatColor.YELLOW + "Mined items fly to you for 5m.", ChatColor.RED + "Consumable."));
        i.setItemMeta(m);
        return i;
    }

    @EventHandler
    public void onUse(PlayerInteractEvent e) {
        if (e.getItem() != null && isItem(e.getItem())) {
            e.getItem().setAmount(e.getItem().getAmount() - 1);
            activeMagnets.put(e.getPlayer().getUniqueId(), System.currentTimeMillis() + (5 * 60 * 1000));
            e.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE + "⚡ Aether Magnet activated for 5 minutes!");
            e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 2f);
        }
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent e) {
        Item item = e.getEntity();
        Player nearest = null;
        double dist = 10.0;

        for (Player p : item.getWorld().getPlayers()) {
            if (activeMagnets.containsKey(p.getUniqueId())) {
                if (System.currentTimeMillis() > activeMagnets.get(p.getUniqueId())) {
                    activeMagnets.remove(p.getUniqueId());
                    p.sendMessage(ChatColor.RED + "Your Aether Magnet has expired.");
                    continue;
                }
                double d = p.getLocation().distance(item.getLocation());
                if (d < dist) {
                    dist = d;
                    nearest = p;
                }
            }
        }

        if (nearest != null) {
            item.teleport(nearest.getLocation());
            item.setPickupDelay(0);
        }
    }
    private boolean isItem(ItemStack i) { return i != null && i.hasItemMeta() && i.getItemMeta().hasCustomModelData() && i.getItemMeta().getCustomModelData() == ID; }
}