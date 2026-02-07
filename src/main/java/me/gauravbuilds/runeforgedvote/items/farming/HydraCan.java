package me.gauravbuilds.runeforgedvote.items.farming;

import me.gauravbuilds.runeforgedvote.RuneForgedVote;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Arrays;

public class HydraCan implements Listener {
    private final RuneForgedVote plugin;
    public static final int ID = 4005;

    public HydraCan(RuneForgedVote p) { this.plugin = p; }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        NamespacedKey key = new NamespacedKey(plugin, "hydra_can");
        plugin.getServer().removeRecipe(key);

        ShapedRecipe r = new ShapedRecipe(key, getItem());
        r.shape(" C ", "CBC", " C ");
        r.setIngredient('B', Material.BUCKET);
        r.setIngredient('C', Material.PRISMARINE_SHARD);
        plugin.getServer().addRecipe(r);
    }

    public static ItemStack getItem() {
        ItemStack i = new ItemStack(Material.BUCKET);
        ItemMeta m = i.getItemMeta();
        m.setDisplayName(ChatColor.AQUA + "💧 Hydra Can");
        m.setCustomModelData(ID);
        m.setLore(Arrays.asList(ChatColor.GRAY + "Never runs dry.", ChatColor.GOLD + "Ability: Monsoon", ChatColor.YELLOW + "Hydrates a 9x9 area instantly.", ChatColor.YELLOW + "No water source needed."));
        i.setItemMeta(m);
        return i;
    }

    @EventHandler
    public void onUse(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!isItem(e.getItem())) return;

        Block center = e.getClickedBlock();
        e.setCancelled(true); // Don't place bucket if it was full

        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                Block b = center.getRelative(x, 0, z);
                if (b.getType() == Material.FARMLAND) {
                    Farmland data = (Farmland) b.getBlockData();
                    data.setMoisture(data.getMaximumMoisture());
                    b.setBlockData(data);
                    b.getWorld().spawnParticle(Particle.SPLASH, b.getLocation().add(0.5, 1, 0.5), 3);
                }
            }
        }
        e.getPlayer().playSound(center.getLocation(), Sound.ITEM_BUCKET_EMPTY, 1f, 1f);
    }
    private boolean isItem(ItemStack i) { return i != null && i.hasItemMeta() && i.getItemMeta().hasCustomModelData() && i.getItemMeta().getCustomModelData() == ID; }
}