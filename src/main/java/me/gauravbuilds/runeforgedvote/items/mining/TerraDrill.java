package me.gauravbuilds.runeforgedvote.items.mining;

import me.gauravbuilds.runeforgedvote.RuneForgedVote;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TerraDrill implements Listener {

    private final RuneForgedVote plugin;
    public static final int CUSTOM_MODEL_DATA = 2002;

    private final List<Material> ALLOWED_BLOCKS = Arrays.asList(
            Material.DIRT, Material.GRASS_BLOCK, Material.GRAVEL, Material.SAND,
            Material.RED_SAND, Material.COARSE_DIRT, Material.PODZOL, Material.MYCELIUM,
            Material.ROOTED_DIRT, Material.MUD, Material.CLAY, Material.SOUL_SAND, Material.SOUL_SOIL
    );

    public TerraDrill(RuneForgedVote plugin) {
        this.plugin = plugin;
    }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        ItemStack item = getItem();
        NamespacedKey key = new NamespacedKey(plugin, "terra_drill");

        // FIX
        plugin.getServer().removeRecipe(key);

        ShapedRecipe recipe = new ShapedRecipe(key, item);
        recipe.shape(" T ", "TST", " T ");
        recipe.setIngredient('S', Material.NETHERITE_SHOVEL);
        recipe.setIngredient('T', Material.EMERALD);

        plugin.getServer().addRecipe(recipe);
    }

    public static ItemStack getItem() {
        ItemStack item = new ItemStack(Material.NETHERITE_SHOVEL);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&a&l🌿 Terra Drill"));
        meta.setCustomModelData(CUSTOM_MODEL_DATA);

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Infused with the strength of nature.");
        lore.add("");
        lore.add(ChatColor.GOLD + "Item Ability: Earth Mover");
        lore.add(ChatColor.YELLOW + "Excavates a 3x3 area of soil");
        lore.add(ChatColor.YELLOW + "gravel, and sand instantly.");
        lore.add("");
        lore.add(ChatColor.DARK_GRAY + "RuneForged Artifact");
        meta.setLore(lore);

        meta.addEnchant(Enchantment.EFFICIENCY, 5, true);
        meta.addEnchant(Enchantment.UNBREAKING, 3, true);
        meta.addEnchant(Enchantment.SILK_TOUCH, 1, true);

        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (!isTerraDrill(hand)) return;

        Block center = event.getBlock();
        if (!ALLOWED_BLOCKS.contains(center.getType())) return;

        float pitch = player.getLocation().getPitch();
        List<Block> toBreak = new ArrayList<>();

        if (pitch < -45 || pitch > 45) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && z == 0) continue;
                    toBreak.add(center.getRelative(x, 0, z));
                }
            }
        } else {
            BlockFace face = player.getFacing();
            if (face == BlockFace.NORTH || face == BlockFace.SOUTH) {
                for (int x = -1; x <= 1; x++) {
                    for (int y = -1; y <= 1; y++) {
                        if (x == 0 && y == 0) continue;
                        toBreak.add(center.getRelative(x, y, 0));
                    }
                }
            } else {
                for (int z = -1; z <= 1; z++) {
                    for (int y = -1; y <= 1; y++) {
                        if (z == 0 && y == 0) continue;
                        toBreak.add(center.getRelative(0, y, z));
                    }
                }
            }
        }

        for (Block b : toBreak) {
            if (ALLOWED_BLOCKS.contains(b.getType())) {
                b.breakNaturally(hand);
            }
        }
        center.getWorld().playSound(center.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.5f, 0.5f);
    }

    private boolean isTerraDrill(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().hasCustomModelData() && item.getItemMeta().getCustomModelData() == CUSTOM_MODEL_DATA;
    }
}