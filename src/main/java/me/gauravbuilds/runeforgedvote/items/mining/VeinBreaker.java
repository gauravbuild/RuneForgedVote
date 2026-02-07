package me.gauravbuilds.runeforgedvote.items.mining;

import me.gauravbuilds.runeforgedvote.RuneForgedVote;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.*;

public class VeinBreaker implements Listener {
    private final RuneForgedVote plugin;
    public static final int ID = 2004;
    private final Set<Material> ORES = new HashSet<>(Arrays.asList(Material.COAL_ORE, Material.DEEPSLATE_COAL_ORE, Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE, Material.COPPER_ORE, Material.DEEPSLATE_COPPER_ORE, Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE, Material.REDSTONE_ORE, Material.DEEPSLATE_REDSTONE_ORE, Material.EMERALD_ORE, Material.DEEPSLATE_EMERALD_ORE, Material.LAPIS_ORE, Material.DEEPSLATE_LAPIS_ORE, Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE, Material.NETHER_QUARTZ_ORE, Material.NETHER_GOLD_ORE));

    public VeinBreaker(RuneForgedVote p) { this.plugin = p; }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        NamespacedKey key = new NamespacedKey(plugin, "vein_breaker");
        plugin.getServer().removeRecipe(key); // FIX

        ShapedRecipe r = new ShapedRecipe(key, getItem());
        r.shape(" I ", "IPI", " I ");
        r.setIngredient('P', Material.DIAMOND_PICKAXE);
        r.setIngredient('I', Material.BLAZE_POWDER);
        plugin.getServer().addRecipe(r);
    }

    public static ItemStack getItem() {
        ItemStack i = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemMeta m = i.getItemMeta();
        m.setDisplayName(ChatColor.YELLOW + "⚡ Vein Breaker");
        m.setCustomModelData(ID);
        m.setLore(Arrays.asList(ChatColor.GRAY + "Channels lightning through ore veins.", ChatColor.GOLD + "Ability: Chain Reaction", ChatColor.YELLOW + "Mines connected ores instantly.", ChatColor.RED + "Low Durability."));
        i.setItemMeta(m);
        return i;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        ItemStack hand = e.getPlayer().getInventory().getItemInMainHand();
        if (!isItem(hand)) return;
        if (!ORES.contains(e.getBlock().getType())) return;

        Set<Block> vein = new HashSet<>();
        findVein(e.getBlock(), e.getBlock().getType(), vein);

        for (Block b : vein) {
            if (!b.equals(e.getBlock())) b.breakNaturally(hand);
        }

        Damageable meta = (Damageable) hand.getItemMeta();
        meta.setDamage(meta.getDamage() + vein.size());
        hand.setItemMeta(meta);
        if (meta.getDamage() >= hand.getType().getMaxDurability()) hand.setAmount(0);

        e.getBlock().getWorld().playSound(e.getBlock().getLocation(), Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1f, 2f);
    }

    private void findVein(Block start, Material type, Set<Block> result) {
        if (result.size() > 64) return;
        if (result.contains(start)) return;
        result.add(start);

        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x==0 && y==0 && z==0) continue;
                    Block rel = start.getRelative(x, y, z);
                    if (rel.getType() == type) findVein(rel, type, result);
                }
            }
        }
    }
    private boolean isItem(ItemStack i) { return i != null && i.hasItemMeta() && i.getItemMeta().hasCustomModelData() && i.getItemMeta().getCustomModelData() == ID; }
}