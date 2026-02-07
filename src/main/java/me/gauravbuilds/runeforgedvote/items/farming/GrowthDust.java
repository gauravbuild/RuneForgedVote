package me.gauravbuilds.runeforgedvote.items.farming;

import me.gauravbuilds.runeforgedvote.RuneForgedVote;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Arrays;

public class GrowthDust implements Listener {
    private final RuneForgedVote plugin;
    public static final int ID = 4006;

    public GrowthDust(RuneForgedVote p) { this.plugin = p; }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        NamespacedKey key = new NamespacedKey(plugin, "growth_dust");
        plugin.getServer().removeRecipe(key);

        ShapedRecipe r = new ShapedRecipe(key, getItem());
        r.shape(" A ", "ABA", " A ");
        r.setIngredient('B', Material.BONE_MEAL);
        r.setIngredient('A', Material.AMETHYST_SHARD);
        plugin.getServer().addRecipe(r);
    }

    public static ItemStack getItem() {
        ItemStack i = new ItemStack(Material.BONE_MEAL);
        ItemMeta m = i.getItemMeta();
        m.setDisplayName(ChatColor.LIGHT_PURPLE + "✨ Growth Dust");
        m.setCustomModelData(ID);
        m.setLore(Arrays.asList(ChatColor.GRAY + "Concentrated life force.", ChatColor.GOLD + "Ability: Surge", ChatColor.YELLOW + "Instantly grows crops in 3x3 area.", ChatColor.RED + "Consumable."));
        i.setItemMeta(m);
        return i;
    }

    @EventHandler
    public void onUse(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!isItem(e.getItem())) return;

        Block center = e.getClickedBlock();
        boolean used = false;

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Block b = center.getRelative(x, 0, z);
                if (b.getBlockData() instanceof Ageable) {
                    b.applyBoneMeal(BlockFace.UP); // Vanilla grow effect
                    b.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, b.getLocation(), 5);
                    used = true;
                }
            }
        }

        if (used) {
            e.getItem().setAmount(e.getItem().getAmount() - 1);
            e.getPlayer().playSound(center.getLocation(), Sound.ITEM_BONE_MEAL_USE, 1f, 1f);
        }
    }
    private boolean isItem(ItemStack i) { return i != null && i.hasItemMeta() && i.getItemMeta().hasCustomModelData() && i.getItemMeta().getCustomModelData() == ID; }
}