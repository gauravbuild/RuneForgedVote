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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBreak(BlockBreakEvent event) {
        if (plugin.getGeodeManager().isGeodeCore(event.getBlock().getLocation())) {
            event.setCancelled(false);
            event.setDropItems(false);
            event.setExpToDrop(0);

            handleLoot(event.getPlayer(), event.getBlock().getType());
            plugin.getGeodeManager().removeSingleGeode(event.getBlock().getLocation());
        }
    }

    private void handleLoot(Player player, Material type) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 2f);
        player.spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation(), 20, 0.5, 0.5, 0.5, 0.2);
        player.sendMessage(ChatColor.GOLD + "★ You harvested the Star Core!");

        // 1. Drop Specific Fragment
        ItemStack shard = getCustomShard(type);
        player.getWorld().dropItemNaturally(player.getLocation(), shard);

        // 2. Drop Money Pouch (Random)
        int roll = random.nextInt(100);
        int amount = 0;
        if (roll < 50) amount = 200; // 50%
        else if (roll < 80) amount = 500; // 30%
        else if (roll < 95) amount = 1000; // 15%
        else if (roll < 99) amount = 5000; // 4%
        else amount = 10000; // 1%

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "eco give " + player.getName() + " " + amount);
        player.sendMessage(ChatColor.GREEN + "+ $" + amount);

        // 3. Stardust Reward
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "rune givecatalyst " + player.getName() + " 1");
    }

    // PUBLIC static so other classes can use it
    public static ItemStack getCustomShard(Material blockType) {
        ItemStack item;
        String name;
        int cmd; // <--- THIS IS THE MISSING KEY

        if (blockType == Material.MAGMA_BLOCK) {
            item = new ItemStack(Material.BLAZE_POWDER);
            name = "&c&lIgnis Ember";
            cmd = 1001; // ID for Ignis
        } else if (blockType == Material.BLUE_ICE) {
            item = new ItemStack(Material.PRISMARINE_SHARD);
            name = "&b&lCryo Fragment";
            cmd = 1002; // ID for Cryo
        } else if (blockType == Material.EMERALD_ORE) {
            item = new ItemStack(Material.EMERALD);
            name = "&a&lTerra Cluster";
            cmd = 1003; // ID for Terra
        } else {
            item = new ItemStack(Material.AMETHYST_SHARD);
            name = "&d&lAether Shard";
            cmd = 1004; // ID for Aether
        }

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

        // --- VITAL: Set the ID so we can protect it ---
        meta.setCustomModelData(cmd);

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Forged from the fallen stars.");
        lore.add(ChatColor.GRAY + "Contains pure astral energy.");
        meta.setLore(lore);

        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);

        return item;
    }

    public static ItemStack getShardByName(String name) {
        if (name.equalsIgnoreCase("ignis")) return getCustomShard(Material.MAGMA_BLOCK);
        if (name.equalsIgnoreCase("cryo")) return getCustomShard(Material.BLUE_ICE);
        if (name.equalsIgnoreCase("terra")) return getCustomShard(Material.EMERALD_ORE);
        return getCustomShard(Material.AMETHYST_BLOCK);
    }
}