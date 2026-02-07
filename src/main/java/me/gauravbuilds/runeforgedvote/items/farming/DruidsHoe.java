package me.gauravbuilds.runeforgedvote.items.farming;

import me.gauravbuilds.runeforgedvote.RuneForgedVote;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.List;

public class DruidsHoe implements Listener {
    private final RuneForgedVote plugin;
    public static final int ID = 4001;

    public DruidsHoe(RuneForgedVote p) { this.plugin = p; }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        NamespacedKey key = new NamespacedKey(plugin, "druids_hoe");
        plugin.getServer().removeRecipe(key);

        ShapedRecipe r = new ShapedRecipe(key, getItem());
        r.shape(" T ", "THT", " T ");
        r.setIngredient('H', Material.NETHERITE_HOE);
        r.setIngredient('T', Material.EMERALD); // Terra Cluster
        plugin.getServer().addRecipe(r);
    }

    public static ItemStack getItem() {
        ItemStack i = new ItemStack(Material.NETHERITE_HOE);
        ItemMeta m = i.getItemMeta();
        m.setDisplayName(ChatColor.GREEN + "🌿 Druid's Hoe");
        m.setCustomModelData(ID);
        List<String> l = new ArrayList<>();
        l.add(ChatColor.GRAY + "Blessed by the earth.");
        l.add(ChatColor.GOLD + "Ability: Fertile Ground");
        l.add(ChatColor.YELLOW + "Tills a 3x3 area.");
        m.setLore(l);
        m.addEnchant(Enchantment.UNBREAKING, 3, true);
        i.setItemMeta(m);
        return i;
    }

    @EventHandler
    public void onTill(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!isItem(e.getItem())) return;

        Block center = e.getClickedBlock();
        if (center == null) return;

        Material type = center.getType();
        if (type == Material.GRASS_BLOCK || type == Material.DIRT || type == Material.DIRT_PATH) {
            // FIX: Use center.getWorld() instead of e.getWorld()
            center.getWorld().playSound(center.getLocation(), Sound.ITEM_HOE_TILL, 1f, 1f);

            // Till 3x3
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    Block b = center.getRelative(x, 0, z);
                    Material t = b.getType();
                    if (t == Material.GRASS_BLOCK || t == Material.DIRT || t == Material.DIRT_PATH) {
                        b.setType(Material.FARMLAND);
                        b.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, b.getLocation().add(0.5, 1, 0.5), 2);
                    }
                }
            }
        }
    }
    private boolean isItem(ItemStack i) { return i != null && i.hasItemMeta() && i.getItemMeta().hasCustomModelData() && i.getItemMeta().getCustomModelData() == ID; }
}