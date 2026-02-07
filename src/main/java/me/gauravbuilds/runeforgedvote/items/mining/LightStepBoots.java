package me.gauravbuilds.runeforgedvote.items.mining;

import me.gauravbuilds.runeforgedvote.RuneForgedVote;
import org.bukkit.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.Arrays;

public class LightStepBoots implements Listener {
    private final RuneForgedVote plugin;
    public static final int ID = 2010;

    public LightStepBoots(RuneForgedVote p) { this.plugin = p; }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        NamespacedKey key = new NamespacedKey(plugin, "light_step_boots");
        plugin.getServer().removeRecipe(key); // FIX

        ShapedRecipe r = new ShapedRecipe(key, getItem());
        r.shape(" I ", "IBI", " I ");
        r.setIngredient('B', Material.NETHERITE_BOOTS);
        r.setIngredient('I', Material.BLAZE_POWDER);
        plugin.getServer().addRecipe(r);

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (org.bukkit.entity.Player p : Bukkit.getOnlinePlayers()) {
                checkBoots(p);
            }
        }, 0L, 100L);
    }

    public static ItemStack getItem() {
        ItemStack i = new ItemStack(Material.NETHERITE_BOOTS);
        ItemMeta m = i.getItemMeta();
        m.setDisplayName(ChatColor.GOLD + "☀ Light-Step Boots");
        m.setCustomModelData(ID);
        m.setLore(Arrays.asList(ChatColor.GRAY + "Walk in the light.", ChatColor.GOLD + "Ability: Eternal Day", ChatColor.YELLOW + "Grants Night Vision when worn."));
        i.setItemMeta(m);
        return i;
    }

    private void checkBoots(org.bukkit.entity.Player p) {
        ItemStack boots = p.getInventory().getBoots();
        if (isItem(boots)) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 220, 0, false, false));
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (e.getFrom().getBlockX() != e.getTo().getBlockX() || e.getFrom().getBlockZ() != e.getTo().getBlockZ()) {
            ItemStack boots = e.getPlayer().getInventory().getBoots();
            if (isItem(boots) && !e.getPlayer().hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
                checkBoots(e.getPlayer());
            }
        }
    }

    private boolean isItem(ItemStack i) { return i != null && i.hasItemMeta() && i.getItemMeta().hasCustomModelData() && i.getItemMeta().getCustomModelData() == ID; }
}