package me.gauravbuilds.runeforgedvote.items.mining;

import me.gauravbuilds.runeforgedvote.RuneForgedVote;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Arrays;

public class ObsidianEater implements Listener {
    private final RuneForgedVote plugin;
    public static final int ID = 2005;

    public ObsidianEater(RuneForgedVote p) { this.plugin = p; }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        NamespacedKey key = new NamespacedKey(plugin, "obsidian_eater");
        plugin.getServer().removeRecipe(key); // FIX

        ShapedRecipe r = new ShapedRecipe(key, getItem());
        r.shape(" C ", "CPC", " C ");
        r.setIngredient('P', Material.DIAMOND_PICKAXE);
        r.setIngredient('C', Material.PRISMARINE_SHARD);
        plugin.getServer().addRecipe(r);
    }

    public static ItemStack getItem() {
        ItemStack i = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemMeta m = i.getItemMeta();
        m.setDisplayName(ChatColor.AQUA + "❄ Obsidian Eater");
        m.setCustomModelData(ID);
        m.setLore(Arrays.asList(ChatColor.GRAY + "Freezes magma glass instantly.", ChatColor.GOLD + "Ability: Thermal Shock", ChatColor.YELLOW + "Instantly breaks Obsidian.", ChatColor.RED + "Max 100 Uses."));
        i.setItemMeta(m);
        return i;
    }

    @EventHandler
    public void onDamage(BlockDamageEvent e) {
        ItemStack hand = e.getItemInHand();
        if (!isItem(hand)) return;
        if (e.getBlock().getType() == Material.OBSIDIAN || e.getBlock().getType() == Material.CRYING_OBSIDIAN) {
            e.setInstaBreak(true);

            Damageable meta = (Damageable) hand.getItemMeta();
            meta.setDamage(meta.getDamage() + 15);
            hand.setItemMeta(meta);
            if (meta.getDamage() >= hand.getType().getMaxDurability()) hand.setAmount(0);

            e.getPlayer().playSound(e.getBlock().getLocation(), Sound.BLOCK_GLASS_BREAK, 1f, 0.5f);
        }
    }
    private boolean isItem(ItemStack i) { return i != null && i.hasItemMeta() && i.getItemMeta().hasCustomModelData() && i.getItemMeta().getCustomModelData() == ID; }
}