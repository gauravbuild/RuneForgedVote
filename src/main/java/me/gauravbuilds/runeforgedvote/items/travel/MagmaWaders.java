package me.gauravbuilds.runeforgedvote.items.travel;

import me.gauravbuilds.runeforgedvote.RuneForgedVote;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.Arrays;

public class MagmaWaders implements Listener {
    private final RuneForgedVote plugin;
    public static final int ID = 5002;

    public MagmaWaders(RuneForgedVote p) { this.plugin = p; }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        NamespacedKey key = new NamespacedKey(plugin, "magma_waders");
        plugin.getServer().removeRecipe(key);

        ShapedRecipe r = new ShapedRecipe(key, getItem());
        r.shape(" I ", "IBI", " I ");
        r.setIngredient('B', Material.NETHERITE_BOOTS);
        r.setIngredient('I', Material.BLAZE_POWDER);
        plugin.getServer().addRecipe(r);
    }

    public static ItemStack getItem() {
        ItemStack i = new ItemStack(Material.NETHERITE_BOOTS);
        ItemMeta m = i.getItemMeta();
        m.setDisplayName(ChatColor.GOLD + "🔥 Magma Waders");
        m.setCustomModelData(ID);
        m.setLore(Arrays.asList(ChatColor.GRAY + "Hot to the touch.", ChatColor.GOLD + "Ability: Lava Walker", ChatColor.YELLOW + "Allows walking on lava."));
        i.setItemMeta(m);
        return i;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (!isItem(e.getPlayer().getInventory().getBoots())) return;

        Block b = e.getTo().getBlock().getRelative(0, -1, 0);
        if (b.getType() == Material.LAVA) {
            b.setType(Material.BASALT);
            b.getWorld().spawnParticle(Particle.FLAME, b.getLocation().add(0.5, 1, 0.5), 2);

            // Revert back to Lava
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (b.getType() == Material.BASALT) b.setType(Material.LAVA);
                }
            }.runTaskLater(plugin, 60L); // 3 seconds
        }
    }

    private boolean isItem(ItemStack i) { return i != null && i.hasItemMeta() && i.getItemMeta().hasCustomModelData() && i.getItemMeta().getCustomModelData() == ID; }
}