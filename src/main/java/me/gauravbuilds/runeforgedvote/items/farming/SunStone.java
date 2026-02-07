package me.gauravbuilds.runeforgedvote.items.farming;

import me.gauravbuilds.runeforgedvote.RuneForgedVote;
import org.bukkit.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Arrays;

public class SunStone implements Listener {
    private final RuneForgedVote plugin;
    public static final int ID = 4008;

    public SunStone(RuneForgedVote p) { this.plugin = p; }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        NamespacedKey key = new NamespacedKey(plugin, "sun_stone");
        plugin.getServer().removeRecipe(key);

        ShapedRecipe r = new ShapedRecipe(key, getItem());
        r.shape(" I ", "ISI", " I ");
        r.setIngredient('S', Material.SUNFLOWER);
        r.setIngredient('I', Material.BLAZE_POWDER);
        plugin.getServer().addRecipe(r);
    }

    public static ItemStack getItem() {
        ItemStack i = new ItemStack(Material.SUNFLOWER);
        ItemMeta m = i.getItemMeta();
        m.setDisplayName(ChatColor.GOLD + "☀ Sun Stone");
        m.setCustomModelData(ID);
        m.setLore(Arrays.asList(ChatColor.GRAY + "Controls the cycle.", ChatColor.GOLD + "Ability: Dawn", ChatColor.YELLOW + "Right-click to set time to Day.", ChatColor.RED + "Consumable."));
        i.setItemMeta(m);
        return i;
    }

    @EventHandler
    public void onUse(PlayerInteractEvent e) {
        if (e.getItem() != null && isItem(e.getItem()) && e.getAction().toString().contains("RIGHT")) {
            e.getItem().setAmount(e.getItem().getAmount() - 1);
            e.getPlayer().getWorld().setTime(1000); // Morning
            e.getPlayer().sendMessage(ChatColor.GOLD + "The sun rises at your command.");
            e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
        }
    }
    private boolean isItem(ItemStack i) { return i != null && i.hasItemMeta() && i.getItemMeta().hasCustomModelData() && i.getItemMeta().getCustomModelData() == ID; }
}