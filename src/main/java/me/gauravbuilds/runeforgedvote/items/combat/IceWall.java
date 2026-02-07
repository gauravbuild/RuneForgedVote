package me.gauravbuilds.runeforgedvote.items.combat;

import me.gauravbuilds.runeforgedvote.RuneForgedVote;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IceWall implements Listener {
    private final RuneForgedVote plugin;
    public static final int ID = 3008;

    public IceWall(RuneForgedVote p) { this.plugin = p; }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        NamespacedKey key = new NamespacedKey(plugin, "ice_wall");
        plugin.getServer().removeRecipe(key);

        ShapedRecipe r = new ShapedRecipe(key, getItem());
        r.shape(" C ", "CPC", " C ");
        r.setIngredient('P', Material.PACKED_ICE);
        r.setIngredient('C', Material.PRISMARINE_SHARD);
        plugin.getServer().addRecipe(r);
    }

    public static ItemStack getItem() {
        ItemStack i = new ItemStack(Material.PACKED_ICE);
        ItemMeta m = i.getItemMeta();
        m.setDisplayName(ChatColor.AQUA + "❄ Ice Wall Generator");
        m.setCustomModelData(ID);
        m.setLore(Arrays.asList(ChatColor.GRAY + "Instant fortification.", ChatColor.GOLD + "Ability: Glacial Wall", ChatColor.YELLOW + "Right-click to spawn a 3x3 ice wall.", ChatColor.YELLOW + "Melts after 5 seconds.", ChatColor.RED + "Consumable."));
        i.setItemMeta(m);
        return i;
    }

    @EventHandler
    public void onUse(PlayerInteractEvent e) {
        if (e.getItem() != null && isItem(e.getItem())) {
            if (e.getAction().toString().contains("RIGHT")) {
                e.setCancelled(true);
                e.getItem().setAmount(e.getItem().getAmount() - 1);

                Location center = e.getPlayer().getLocation().add(e.getPlayer().getLocation().getDirection().multiply(2));
                List<Block> wallBlocks = new ArrayList<>();

                // 3x3 Vertical Wall
                for (int x = -1; x <= 1; x++) {
                    for (int y = 0; y < 3; y++) {
                        // Math to make it face the player is complex, simplifying to X/Z relative
                        Block b = center.clone().add(x, y, 0).getBlock();
                        if (b.getType() == Material.AIR) {
                            b.setType(Material.PACKED_ICE);
                            wallBlocks.add(b);
                        }
                    }
                }

                e.getPlayer().playSound(center, Sound.BLOCK_GLASS_PLACE, 1f, 1f);

                // Decay Task
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        for (Block b : wallBlocks) b.setType(Material.AIR);
                        center.getWorld().playSound(center, Sound.BLOCK_GLASS_BREAK, 1f, 1f);
                    }
                }.runTaskLater(plugin, 100L); // 5 Seconds
            }
        }
    }
    private boolean isItem(ItemStack i) { return i != null && i.hasItemMeta() && i.getItemMeta().hasCustomModelData() && i.getItemMeta().getCustomModelData() == ID; }
}