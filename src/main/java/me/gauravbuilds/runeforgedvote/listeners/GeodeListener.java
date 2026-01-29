package me.gauravbuilds.runeforgedvote.listeners;

import me.gauravbuilds.runeforgedvote.RuneForgedVote;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GeodeListener implements Listener {

    private final RuneForgedVote plugin;
    private final Random random = new Random();

    public GeodeListener(RuneForgedVote plugin) {
        this.plugin = plugin;
    }

    // HIGHEST PRIORITY + IGNORE CANCELLED = FALSE
    // This allows us to UN-CANCEL the event if WorldGuard blocked it.
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBreak(BlockBreakEvent event) {

        // 1. Is it a Geode Core?
        if (plugin.getGeodeManager().isGeodeCore(event.getBlock().getLocation())) {
            // FORCE ALLOW BREAKING (Bypass protection)
            event.setCancelled(false);
            event.setDropItems(false); // Don't drop raw block

            handleLoot(event.getPlayer(), event.getBlock().getType());
            plugin.getGeodeManager().removeSingleGeode(event.getBlock().getLocation());
            return;
        }

        // 2. Is it Geode Crust?
        if (plugin.getGeodeManager().isGeodeCrust(event.getBlock().getLocation())) {
            // FORCE ALLOW BREAKING
            event.setCancelled(false);
            event.setDropItems(false); // Don't drop crust
            event.setExpToDrop(0);
        }
    }

    private void handleLoot(Player player, Material type) {
        // Visuals
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 2f);
        player.spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation(), 20, 0.5, 0.5, 0.5, 0.2);
        player.sendMessage(ChatColor.GOLD + "★ You harvested the Star Core!");

        // --- DROP CUSTOM SHARD ---
        ItemStack shard = getCustomShard(type);
        player.getWorld().dropItemNaturally(player.getLocation(), shard);

        // --- STANDARD REWARDS ---
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "rune givecatalyst " + player.getName() + " 1");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "eco give " + player.getName() + " 500");

        // --- RARE REWARD ---
        if (random.nextInt(100) < 5) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "crate give key cosmic " + player.getName() + " 1");
            player.sendMessage(ChatColor.LIGHT_PURPLE + "You found a Cosmic Key inside!");
        }
    }

    private ItemStack getCustomShard(Material blockType) {
        ItemStack item;
        String name;

        if (blockType == Material.MAGMA_BLOCK) {
            item = new ItemStack(Material.BLAZE_POWDER);
            name = "&c&lIgnis Ember";
        } else if (blockType == Material.BLUE_ICE) {
            item = new ItemStack(Material.PRISMARINE_SHARD);
            name = "&b&lCryo Fragment";
        } else if (blockType == Material.EMERALD_ORE) {
            item = new ItemStack(Material.EMERALD);
            name = "&a&lTerra Cluster";
        } else {
            item = new ItemStack(Material.AMETHYST_SHARD);
            name = "&d&lAether Shard";
        }

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Forged from the fallen stars.");
        lore.add(ChatColor.GRAY + "Contains pure astral energy.");
        meta.setLore(lore);

        // GLINT EFFECT (Unbreaking 1 + Hide Flags)
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        item.setItemMeta(meta);
        return item;
    }
}