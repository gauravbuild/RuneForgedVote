package me.gauravbuilds.runeforgedvote.items.mining;

import me.gauravbuilds.runeforgedvote.RuneForgedVote;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class IgnisPickaxe implements Listener {

    private final RuneForgedVote plugin;
    private final Random random = new Random();
    public static final int CUSTOM_MODEL_DATA = 2001;

    public IgnisPickaxe(RuneForgedVote plugin) {
        this.plugin = plugin;
    }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        ItemStack pickaxe = getItem();
        NamespacedKey key = new NamespacedKey(plugin, "ignis_pickaxe");

        // FIX: Remove old recipe before adding new one
        plugin.getServer().removeRecipe(key);

        ShapedRecipe recipe = new ShapedRecipe(key, pickaxe);
        recipe.shape(" E ", "EPE", " E ");
        recipe.setIngredient('P', Material.NETHERITE_PICKAXE);
        recipe.setIngredient('E', Material.BLAZE_POWDER);

        plugin.getServer().addRecipe(recipe);
    }

    public static ItemStack getItem() {
        ItemStack item = new ItemStack(Material.NETHERITE_PICKAXE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6&l🔥 Ignis Pickaxe"));
        meta.setCustomModelData(CUSTOM_MODEL_DATA);

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Forged in the heart of a star.");
        lore.add("");
        lore.add(ChatColor.GOLD + "Item Ability: Super Heat");
        lore.add(ChatColor.YELLOW + "Autosmelts Iron and Gold ores");
        lore.add(ChatColor.YELLOW + "with extreme efficiency.");
        lore.add("");
        lore.add(ChatColor.DARK_GRAY + "RuneForged Artifact");
        meta.setLore(lore);

        meta.addEnchant(Enchantment.EFFICIENCY, 6, true);
        meta.addEnchant(Enchantment.FORTUNE, 3, true);
        meta.addEnchant(Enchantment.UNBREAKING, 3, true);

        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack hand = player.getInventory().getItemInMainHand();

        if (!isIgnisPickaxe(hand)) return;

        Block block = event.getBlock();
        Material dropType = null;

        if (block.getType() == Material.IRON_ORE || block.getType() == Material.DEEPSLATE_IRON_ORE || block.getType() == Material.RAW_IRON_BLOCK) {
            dropType = Material.IRON_INGOT;
        } else if (block.getType() == Material.GOLD_ORE || block.getType() == Material.DEEPSLATE_GOLD_ORE || block.getType() == Material.RAW_GOLD_BLOCK) {
            dropType = Material.GOLD_INGOT;
        } else if (block.getType() == Material.ANCIENT_DEBRIS) {
            dropType = Material.NETHERITE_SCRAP;
        }

        if (dropType != null) {
            event.setDropItems(false);

            int amount = 1;
            if (dropType != Material.NETHERITE_SCRAP) {
                int fortune = hand.getEnchantmentLevel(Enchantment.FORTUNE);
                if (fortune > 0) {
                    int bonus = random.nextInt(fortune + 2) - 1;
                    if (bonus < 0) bonus = 0;
                    amount += bonus;
                }
            }

            block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(dropType, amount));
            block.getWorld().spawnParticle(org.bukkit.Particle.FLAME, block.getLocation().add(0.5, 0.5, 0.5), 5, 0.2, 0.2, 0.2, 0.05);
            block.getWorld().playSound(block.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.5f, 1f);
        }
    }

    private boolean isIgnisPickaxe(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().hasCustomModelData() && item.getItemMeta().getCustomModelData() == CUSTOM_MODEL_DATA;
    }
}