package me.gauravbuilds.runeforgedvote.items.mining;

import me.gauravbuilds.runeforgedvote.RuneForgedVote;
import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EnderSignal;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Arrays;

public class CaveFinder implements Listener {
    private final RuneForgedVote plugin;
    public static final int ID = 2008;

    public CaveFinder(RuneForgedVote p) { this.plugin = p; }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        NamespacedKey key = new NamespacedKey(plugin, "cave_finder");
        plugin.getServer().removeRecipe(key); // FIX

        ShapedRecipe r = new ShapedRecipe(key, getItem());
        r.shape(" A ", "AEA", " A ");
        r.setIngredient('E', Material.ENDER_EYE);
        r.setIngredient('A', Material.AMETHYST_SHARD);
        plugin.getServer().addRecipe(r);
    }

    public static ItemStack getItem() {
        ItemStack i = new ItemStack(Material.ENDER_EYE);
        ItemMeta m = i.getItemMeta();
        m.setDisplayName(ChatColor.LIGHT_PURPLE + "👁 Cave Finder");
        m.setCustomModelData(ID);
        m.setLore(Arrays.asList(ChatColor.GRAY + "Sees what is hidden.", ChatColor.GOLD + "Ability: X-Ray Vision", ChatColor.YELLOW + "Highlights nearest Spawner/Chest.", ChatColor.RED + "Consumable."));
        i.setItemMeta(m);
        return i;
    }

    @EventHandler
    public void onUse(PlayerInteractEvent e) {
        if (e.getItem() != null && isItem(e.getItem())) {
            if (e.getAction().toString().contains("RIGHT")) {
                e.setCancelled(true);
                e.getItem().setAmount(e.getItem().getAmount() - 1);

                Location loc = e.getPlayer().getLocation();
                Location found = null;
                double minDist = 10000;

                int radius = 2;
                Chunk center = loc.getChunk();

                for (int x = -radius; x <= radius; x++) {
                    for (int z = -radius; z <= radius; z++) {
                        Chunk c = loc.getWorld().getChunkAt(center.getX() + x, center.getZ() + z);
                        for (BlockState tile : c.getTileEntities()) {
                            if (tile instanceof CreatureSpawner || tile instanceof Chest) {
                                double d = tile.getLocation().distance(loc);
                                if (d < minDist) {
                                    minDist = d;
                                    found = tile.getLocation();
                                }
                            }
                        }
                    }
                }

                if (found != null) {
                    EnderSignal signal = loc.getWorld().spawn(loc, EnderSignal.class);
                    signal.setTargetLocation(found);
                    signal.setDropItem(false);
                    e.getPlayer().sendMessage(ChatColor.GREEN + "The eye pulls towards treasure...");
                } else {
                    e.getPlayer().sendMessage(ChatColor.RED + "No significant structures found nearby.");
                }
            }
        }
    }
    private boolean isItem(ItemStack i) { return i != null && i.hasItemMeta() && i.getItemMeta().hasCustomModelData() && i.getItemMeta().getCustomModelData() == ID; }
}