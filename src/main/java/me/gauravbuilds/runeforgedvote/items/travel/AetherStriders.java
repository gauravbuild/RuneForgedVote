package me.gauravbuilds.runeforgedvote.items.travel;

import me.gauravbuilds.runeforgedvote.RuneForgedVote;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.Arrays;

public class AetherStriders implements Listener {
    private final RuneForgedVote plugin;
    public static final int ID = 5001;

    public AetherStriders(RuneForgedVote p) { this.plugin = p; }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        NamespacedKey key = new NamespacedKey(plugin, "aether_striders");
        plugin.getServer().removeRecipe(key);

        ShapedRecipe r = new ShapedRecipe(key, getItem());
        r.shape(" A ", "ABA", " A ");
        r.setIngredient('B', Material.NETHERITE_BOOTS);
        r.setIngredient('A', Material.AMETHYST_SHARD);
        plugin.getServer().addRecipe(r);
    }

    public static ItemStack getItem() {
        ItemStack i = new ItemStack(Material.NETHERITE_BOOTS);
        ItemMeta m = i.getItemMeta();
        m.setDisplayName(ChatColor.LIGHT_PURPLE + "🔮 Aether Striders");
        m.setCustomModelData(ID);
        m.setLore(Arrays.asList(ChatColor.GRAY + "Walk on the wind.", ChatColor.GOLD + "Ability: Weightless", ChatColor.YELLOW + "Grants Speed II.", ChatColor.YELLOW + "Negates Fall Damage."));
        i.setItemMeta(m);
        return i;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (isItem(p.getInventory().getBoots()) && !p.hasPotionEffect(PotionEffectType.SPEED)) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1, false, false));
        }
    }

    @EventHandler
    public void onFall(EntityDamageEvent e) {
        if (e.getCause() == EntityDamageEvent.DamageCause.FALL && e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if (isItem(p.getInventory().getBoots())) {
                e.setCancelled(true);
            }
        }
    }

    private boolean isItem(ItemStack i) { return i != null && i.hasItemMeta() && i.getItemMeta().hasCustomModelData() && i.getItemMeta().getCustomModelData() == ID; }
}