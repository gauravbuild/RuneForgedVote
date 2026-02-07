package me.gauravbuilds.runeforgedvote.items.farming;

import me.gauravbuilds.runeforgedvote.RuneForgedVote;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Arrays;
import java.util.Collection;

public class HarvesterScythe implements Listener {
    private final RuneForgedVote plugin;
    public static final int ID = 4003;

    public HarvesterScythe(RuneForgedVote p) { this.plugin = p; }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        NamespacedKey key = new NamespacedKey(plugin, "harvester_scythe");
        plugin.getServer().removeRecipe(key);

        ShapedRecipe r = new ShapedRecipe(key, getItem());
        r.shape(" I ", "IHI", " I ");
        r.setIngredient('H', Material.NETHERITE_HOE);
        r.setIngredient('I', Material.BLAZE_POWDER);
        plugin.getServer().addRecipe(r);
    }

    public static ItemStack getItem() {
        ItemStack i = new ItemStack(Material.NETHERITE_HOE);
        ItemMeta m = i.getItemMeta();
        m.setDisplayName(ChatColor.GOLD + "🌾 Harvester Scythe");
        m.setCustomModelData(ID);
        m.setLore(Arrays.asList(ChatColor.GRAY + "Reaps the fields.", ChatColor.GOLD + "Ability: Auto-Harvest", ChatColor.YELLOW + "Right-click to Harvest & Replant", ChatColor.YELLOW + "a 5x5 area of crops."));
        i.setItemMeta(m);
        return i;
    }

    @EventHandler
    public void onUse(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!isItem(e.getItem())) return;

        Block center = e.getClickedBlock();
        if (center == null) return;

        // Check 5x5
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                Block b = center.getRelative(x, 0, z);
                if (b.getBlockData() instanceof Ageable) {
                    Ageable crop = (Ageable) b.getBlockData();
                    if (crop.getAge() == crop.getMaximumAge()) {
                        // Harvest
                        Collection<ItemStack> drops = b.getDrops(e.getItem());
                        for (ItemStack drop : drops) {
                            // Don't drop seeds if we replant (simple logic: just drop all, player has plenty)
                            b.getWorld().dropItemNaturally(b.getLocation(), drop);
                        }

                        // Replant
                        crop.setAge(0);
                        b.setBlockData(crop);
                        b.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, b.getLocation(), 1);
                    }
                }
            }
        }
        e.getPlayer().playSound(center.getLocation(), Sound.ITEM_CROP_PLANT, 1f, 1f);
    }
    private boolean isItem(ItemStack i) { return i != null && i.hasItemMeta() && i.getItemMeta().hasCustomModelData() && i.getItemMeta().getCustomModelData() == ID; }
}