package me.gauravbuilds.runeforgedvote.items.farming;

import me.gauravbuilds.runeforgedvote.RuneForgedVote;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.*;

public class LumberjackAxe implements Listener {
    private final RuneForgedVote plugin;
    public static final int ID = 4004;

    public LumberjackAxe(RuneForgedVote p) { this.plugin = p; }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        NamespacedKey key = new NamespacedKey(plugin, "lumberjack_axe");
        plugin.getServer().removeRecipe(key);

        ShapedRecipe r = new ShapedRecipe(key, getItem());
        r.shape(" T ", "TAT", " T ");
        r.setIngredient('A', Material.NETHERITE_AXE);
        r.setIngredient('T', Material.EMERALD);
        plugin.getServer().addRecipe(r);
    }

    public static ItemStack getItem() {
        ItemStack i = new ItemStack(Material.NETHERITE_AXE);
        ItemMeta m = i.getItemMeta();
        m.setDisplayName(ChatColor.GOLD + "🪓 Lumberjack Axe");
        m.setCustomModelData(ID);
        m.setLore(Arrays.asList(ChatColor.GRAY + "Timbeeeer!", ChatColor.GOLD + "Ability: Treecapitator", ChatColor.YELLOW + "Breaks entire trees instantly.", ChatColor.RED + "Durability drains per log."));
        i.setItemMeta(m);
        return i;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        ItemStack hand = e.getPlayer().getInventory().getItemInMainHand();
        if (!isItem(hand)) return;
        if (!isLog(e.getBlock().getType())) return;

        Set<Block> tree = new HashSet<>();
        findTree(e.getBlock(), e.getBlock().getType(), tree);

        for (Block b : tree) {
            b.breakNaturally(hand);
        }

        Damageable meta = (Damageable) hand.getItemMeta();
        meta.setDamage(meta.getDamage() + tree.size());
        hand.setItemMeta(meta);
        if (meta.getDamage() >= hand.getType().getMaxDurability()) hand.setAmount(0);
    }

    private void findTree(Block start, Material type, Set<Block> result) {
        if (result.size() > 100) return; // Limit size
        if (result.contains(start)) return;
        result.add(start);

        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) { // Look up/down/around
                for (int z = -1; z <= 1; z++) {
                    Block rel = start.getRelative(x, y, z);
                    if (rel.getType() == type) findTree(rel, type, result);
                }
            }
        }
    }

    private boolean isLog(Material m) {
        return m.name().contains("_LOG");
    }

    private boolean isItem(ItemStack i) { return i != null && i.hasItemMeta() && i.getItemMeta().hasCustomModelData() && i.getItemMeta().getCustomModelData() == ID; }
}