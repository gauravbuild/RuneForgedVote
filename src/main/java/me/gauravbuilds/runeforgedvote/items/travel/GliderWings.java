package me.gauravbuilds.runeforgedvote.items.travel;

import me.gauravbuilds.runeforgedvote.RuneForgedVote;
import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Arrays;

public class GliderWings {
    private final RuneForgedVote plugin;
    public static final int ID = 5005;

    public GliderWings(RuneForgedVote p) { this.plugin = p; }

    public void register() {
        NamespacedKey key = new NamespacedKey(plugin, "glider_wings");
        plugin.getServer().removeRecipe(key);

        ShapedRecipe r = new ShapedRecipe(key, getItem());
        r.shape(" A ", "FEF", " A ");
        r.setIngredient('F', Material.FEATHER);
        r.setIngredient('E', Material.LEATHER_CHESTPLATE); // The "Chestplate" base
        r.setIngredient('A', Material.AMETHYST_SHARD);
        plugin.getServer().addRecipe(r);
    }

    public static ItemStack getItem() {
        // We give an ACTUAL Elytra, but crafted cheaply
        ItemStack i = new ItemStack(Material.ELYTRA);
        ItemMeta m = i.getItemMeta();
        m.setDisplayName(ChatColor.LIGHT_PURPLE + "🕊 Glider Wings");
        m.setCustomModelData(ID);
        m.setLore(Arrays.asList(ChatColor.GRAY + "Fragile wings.", ChatColor.GOLD + "Ability: Flight", ChatColor.YELLOW + "Works like an Elytra.", ChatColor.RED + "Low Durability."));
        i.setItemMeta(m);
        return i;
    }
}