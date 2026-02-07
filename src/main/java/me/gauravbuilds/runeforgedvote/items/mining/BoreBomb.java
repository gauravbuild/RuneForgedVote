package me.gauravbuilds.runeforgedvote.items.mining;

import me.gauravbuilds.runeforgedvote.RuneForgedVote;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import java.util.Arrays;

public class BoreBomb implements Listener {
    private final RuneForgedVote plugin;
    public static final int ID = 2007;

    public BoreBomb(RuneForgedVote p) { this.plugin = p; }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        NamespacedKey key = new NamespacedKey(plugin, "bore_bomb");
        plugin.getServer().removeRecipe(key); // FIX

        ShapedRecipe r = new ShapedRecipe(key, getItem());
        r.shape(" I ", "ITI", " I ");
        r.setIngredient('T', Material.TNT);
        r.setIngredient('I', Material.BLAZE_POWDER);
        plugin.getServer().addRecipe(r);
    }

    public static ItemStack getItem() {
        ItemStack i = new ItemStack(Material.TNT);
        ItemMeta m = i.getItemMeta();
        m.setDisplayName(ChatColor.RED + "💣 Bore Bomb");
        m.setCustomModelData(ID);
        m.setLore(Arrays.asList(ChatColor.GRAY + "A condensed directional charge.", ChatColor.GOLD + "Ability: Tunnel Blast", ChatColor.YELLOW + "Throw to create a 1x2x20 tunnel.", ChatColor.RED + "Consumable."));
        i.setItemMeta(m);
        return i;
    }

    @EventHandler
    public void onThrow(PlayerInteractEvent e) {
        if (e.getItem() != null && isItem(e.getItem())) {
            if (e.getAction().toString().contains("RIGHT")) {
                e.setCancelled(true);
                e.getItem().setAmount(e.getItem().getAmount() - 1);
                Snowball s = e.getPlayer().launchProjectile(Snowball.class);
                s.setItem(getItem());
                s.setCustomName("BoreBomb");
            }
        }
    }

    @EventHandler
    public void onHit(ProjectileHitEvent e) {
        if (e.getEntity() instanceof Snowball && "BoreBomb".equals(e.getEntity().getCustomName())) {
            Location loc = e.getEntity().getLocation();
            Vector dir = e.getEntity().getVelocity().normalize();

            for (int i = 0; i < 20; i++) {
                loc.add(dir);
                Block b = loc.getBlock();
                if (b.getType() != Material.BEDROCK && b.getType() != Material.OBSIDIAN) {
                    b.breakNaturally();
                    b.getRelative(0, 1, 0).breakNaturally();
                }
            }
            loc.getWorld().createExplosion(loc, 2F);
        }
    }
    private boolean isItem(ItemStack i) { return i != null && i.hasItemMeta() && i.getItemMeta().hasCustomModelData() && i.getItemMeta().getCustomModelData() == ID; }
}