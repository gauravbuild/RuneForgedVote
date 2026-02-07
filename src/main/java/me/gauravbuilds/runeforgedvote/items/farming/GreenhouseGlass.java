package me.gauravbuilds.runeforgedvote.items.farming;

import me.gauravbuilds.runeforgedvote.RuneForgedVote;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import java.util.Arrays;

public class GreenhouseGlass implements Listener {
    private final RuneForgedVote plugin;
    public static final int ID = 4010;
    private final NamespacedKey glassKey;

    public GreenhouseGlass(RuneForgedVote p) {
        this.plugin = p;
        this.glassKey = new NamespacedKey(plugin, "is_greenhouse_glass");
    }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        NamespacedKey key = new NamespacedKey(plugin, "greenhouse_glass");
        plugin.getServer().removeRecipe(key);

        ShapedRecipe r = new ShapedRecipe(key, getItem());
        r.shape(" T ", "TGT", " T ");
        r.setIngredient('G', Material.GLASS);
        r.setIngredient('T', Material.EMERALD);
        plugin.getServer().addRecipe(r);
    }

    public static ItemStack getItem() {
        ItemStack i = new ItemStack(Material.GLASS);
        ItemMeta m = i.getItemMeta();
        m.setDisplayName(ChatColor.GREEN + "🏠 Greenhouse Glass");
        m.setCustomModelData(ID);
        m.setLore(Arrays.asList(ChatColor.GRAY + "Traps solar energy.", ChatColor.GOLD + "Ability: Photosynthesis", ChatColor.YELLOW + "Crops under this block grow", ChatColor.YELLOW + "2x faster."));
        i.setItemMeta(m);
        return i;
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        if (isItem(e.getItemInHand())) {
            // We can't store NBT on blocks easily without a database.
            // For simplicity, we just use Material.GLASS.
            // The "Magic" works if ANY Glass is above it? No, that's too OP.
            // Trick: We will assume players build greenhouses with this.
            // Since we can't save "Custom Block Data" easily in vanilla without external libs,
            // we will check if the player placed it, and maybe add a Metadata value to the chunk?
            // Actually, simplest way for a lightweight plugin:
            // Just let ANY Glass work? Or use Green Stained Glass as the "Magic" one?
            // Let's use LIME_STAINED_GLASS visually so players know.
            e.getBlockPlaced().setType(Material.LIME_STAINED_GLASS);
            e.getPlayer().sendMessage(ChatColor.GREEN + "Greenhouse constructed!");
        }
    }

    @EventHandler
    public void onGrow(BlockGrowEvent e) {
        // When a crop grows...
        Block b = e.getBlock();
        // Check blocks above for our Glass
        for (int y = 1; y <= 5; y++) {
            if (b.getRelative(0, y, 0).getType() == Material.LIME_STAINED_GLASS) {
                // Found it! 50% chance to double grow
                if (Math.random() > 0.5) {
                    if (e.getNewState().getBlockData() instanceof Ageable) {
                        Ageable crop = (Ageable) e.getNewState().getBlockData();
                        if (crop.getAge() < crop.getMaximumAge()) {
                            crop.setAge(Math.min(crop.getMaximumAge(), crop.getAge() + 1));
                            e.getNewState().setBlockData(crop);
                            b.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, b.getLocation(), 1);
                        }
                    }
                }
                return;
            }
        }
    }

    private boolean isItem(ItemStack i) { return i != null && i.hasItemMeta() && i.getItemMeta().hasCustomModelData() && i.getItemMeta().getCustomModelData() == ID; }
}