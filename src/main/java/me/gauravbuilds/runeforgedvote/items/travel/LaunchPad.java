package me.gauravbuilds.runeforgedvote.items.travel;

import me.gauravbuilds.runeforgedvote.RuneForgedVote;
import org.bukkit.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;
import java.util.Arrays;

public class LaunchPad implements Listener {
    private final RuneForgedVote plugin;
    public static final int ID = 5007;

    public LaunchPad(RuneForgedVote p) { this.plugin = p; }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        NamespacedKey key = new NamespacedKey(plugin, "launch_pad");
        plugin.getServer().removeRecipe(key);

        ShapedRecipe r = new ShapedRecipe(key, getItem());
        r.shape(" I ", "IPI", " I ");
        r.setIngredient('P', Material.STONE_PRESSURE_PLATE);
        r.setIngredient('I', Material.BLAZE_POWDER);
        plugin.getServer().addRecipe(r);
    }

    public static ItemStack getItem() {
        ItemStack i = new ItemStack(Material.STONE_PRESSURE_PLATE);
        ItemMeta m = i.getItemMeta();
        m.setDisplayName(ChatColor.RED + "🚀 Launch Pad");
        m.setCustomModelData(ID);
        m.setLore(Arrays.asList(ChatColor.GRAY + "To the moon.", ChatColor.GOLD + "Ability: Launch", ChatColor.YELLOW + "Place and Step to launch 20 blocks up.", ChatColor.RED + "Breaks after use."));
        i.setItemMeta(m);
        return i;
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        if (isItem(e.getItemInHand())) {
            // Mark the block with metadata so we know it's a Launch Pad
            e.getBlockPlaced().setMetadata("LaunchPad", new FixedMetadataValue(plugin, true));
            e.getPlayer().sendMessage(ChatColor.GREEN + "Launch Pad armed!");
        }
    }

    @EventHandler
    public void onStep(PlayerInteractEvent e) {
        if (e.getAction() == Action.PHYSICAL && e.getClickedBlock() != null) {
            if (e.getClickedBlock().hasMetadata("LaunchPad")) {
                e.setCancelled(true); // Don't trigger redstone signal

                // Launch
                e.getPlayer().setVelocity(new Vector(0, 3.0, 0)); // High launch
                e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1f, 1f);

                // Break block
                e.getClickedBlock().removeMetadata("LaunchPad", plugin);
                e.getClickedBlock().setType(Material.AIR);
                e.getClickedBlock().getWorld().spawnParticle(Particle.EXPLOSION, e.getClickedBlock().getLocation(), 1);
            }
        }
    }

    private boolean isItem(ItemStack i) { return i != null && i.hasItemMeta() && i.getItemMeta().hasCustomModelData() && i.getItemMeta().getCustomModelData() == ID; }
}