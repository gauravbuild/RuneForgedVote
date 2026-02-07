package me.gauravbuilds.runeforgedvote.items.combat;

import me.gauravbuilds.runeforgedvote.RuneForgedVote;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;
import java.util.Arrays;

public class Molotov implements Listener {
    private final RuneForgedVote plugin;
    public static final int ID = 3007;

    public Molotov(RuneForgedVote p) { this.plugin = p; }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        NamespacedKey key = new NamespacedKey(plugin, "molotov");
        plugin.getServer().removeRecipe(key);

        ShapedRecipe r = new ShapedRecipe(key, getItem());
        r.shape(" I ", "IPI", " I ");
        r.setIngredient('P', Material.SPLASH_POTION);
        r.setIngredient('I', Material.BLAZE_POWDER);
        plugin.getServer().addRecipe(r);
    }

    public static ItemStack getItem() {
        ItemStack i = new ItemStack(Material.SPLASH_POTION);
        PotionMeta pm = (PotionMeta) i.getItemMeta();
        pm.setBasePotionType(PotionType.FIRE_RESISTANCE); // Visual color
        pm.setDisplayName(ChatColor.GOLD + "🔥 Molotov Cocktail");
        pm.setCustomModelData(ID);
        pm.setLore(Arrays.asList(ChatColor.GRAY + "Volatile liquid fire.", ChatColor.GOLD + "Ability: Inferno", ChatColor.YELLOW + "Creates a fire field on impact.", ChatColor.RED + "Consumable."));
        i.setItemMeta(pm);
        return i;
    }

    @EventHandler
    public void onThrow(PlayerInteractEvent e) {
        if (e.getItem() != null && isItem(e.getItem())) {
            if (e.getAction().toString().contains("RIGHT")) {
                e.setCancelled(true);
                e.getItem().setAmount(e.getItem().getAmount() - 1);
                ThrownPotion p = e.getPlayer().launchProjectile(ThrownPotion.class);
                p.setItem(getItem());
                p.setCustomName("Molotov");
            }
        }
    }

    @EventHandler
    public void onHit(ProjectileHitEvent e) {
        if (e.getEntity() instanceof ThrownPotion && "Molotov".equals(e.getEntity().getCustomName())) {
            Location loc = e.getEntity().getLocation();
            // Create 3x3 fire
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    Block b = loc.clone().add(x, 0, z).getBlock();
                    if (b.getType() == Material.AIR) b.setType(Material.FIRE);
                }
            }
            loc.getWorld().playSound(loc, Sound.ITEM_FIRECHARGE_USE, 1f, 1f);
        }
    }
    private boolean isItem(ItemStack i) { return i != null && i.hasItemMeta() && i.getItemMeta().hasCustomModelData() && i.getItemMeta().getCustomModelData() == ID; }
}