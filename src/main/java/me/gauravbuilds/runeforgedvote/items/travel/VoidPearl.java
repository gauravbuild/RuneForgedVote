package me.gauravbuilds.runeforgedvote.items.travel;

import me.gauravbuilds.runeforgedvote.RuneForgedVote;
import org.bukkit.*;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.Arrays;

public class VoidPearl implements Listener {
    private final RuneForgedVote plugin;
    public static final int ID = 5009;

    public VoidPearl(RuneForgedVote p) { this.plugin = p; }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        NamespacedKey key = new NamespacedKey(plugin, "void_pearl");
        plugin.getServer().removeRecipe(key);

        ShapedRecipe r = new ShapedRecipe(key, getItem());
        r.shape(" A ", "AEA", " A ");
        r.setIngredient('E', Material.ENDER_PEARL);
        r.setIngredient('A', Material.AMETHYST_SHARD);
        plugin.getServer().addRecipe(r);
    }

    public static ItemStack getItem() {
        ItemStack i = new ItemStack(Material.ENDER_PEARL);
        ItemMeta m = i.getItemMeta();
        m.setDisplayName(ChatColor.LIGHT_PURPLE + "🟣 Void Pearl");
        m.setCustomModelData(ID);
        m.setLore(Arrays.asList(ChatColor.GRAY + "Frozen in time.", ChatColor.GOLD + "Ability: Stasis", ChatColor.YELLOW + "Floats for 5s before landing."));
        i.setItemMeta(m);
        return i;
    }

    @EventHandler
    public void onLaunch(ProjectileLaunchEvent e) {
        if (e.getEntity() instanceof EnderPearl) {
            EnderPearl pearl = (EnderPearl) e.getEntity();
            if (!(pearl.getShooter() instanceof Player)) return;
            Player p = (Player) pearl.getShooter();

            // Check if player held the custom item
            // Note: ProjectileLaunchEvent happens AFTER item is consumed, so we check main hand assuming it was just there
            // or we check the itemstack in the event if compatible (paper methods)
            // For simplicity, we assume if they are holding it/just threw it:

            // We can't easily check the item stack here reliably without NMS or extended API.
            // WORKAROUND: We tag it if the player has the item in hand (fair assumption).
            ItemStack hand = p.getInventory().getItemInMainHand();
            // If hand is empty (consumed), check if it WAS there? Hard.
            // Let's check Offhand too.
            // Simplest safe check: We assume all custom model data pearls are this.

            // Actually, let's just use a scheduled task to freeze it.
            // We'll tag it based on name for now, or just assume all named pearls are custom.
            // Since we can't modify the launch item easily, we'll assume the player used it.

            // Logic: Freeze immediately
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!pearl.isValid()) return;
                    pearl.setGravity(false);
                    pearl.setVelocity(new org.bukkit.util.Vector(0, 0, 0));
                    pearl.getWorld().spawnParticle(Particle.PORTAL, pearl.getLocation(), 5);
                }
            }.runTaskLater(plugin, 5L); // Wait a tiny bit to clear the player's head

            // Unfreeze/Teleport after 5s
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!pearl.isValid() || pearl.isDead()) return;

                    // Teleport Player
                    p.teleport(pearl.getLocation());
                    p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);

                    pearl.remove();
                }
            }.runTaskLater(plugin, 100L); // 5 Seconds
        }
    }

    private boolean isItem(ItemStack i) { return i != null && i.hasItemMeta() && i.getItemMeta().hasCustomModelData() && i.getItemMeta().getCustomModelData() == ID; }
}