package me.gauravbuilds.runeforgedvote.items.mining;

import me.gauravbuilds.runeforgedvote.RuneForgedVote;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Arrays;

public class BedrockBreaker implements Listener {
    private final RuneForgedVote plugin;
    public static final int ID = 2009;

    public BedrockBreaker(RuneForgedVote p) { this.plugin = p; }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        NamespacedKey key = new NamespacedKey(plugin, "bedrock_breaker");
        plugin.getServer().removeRecipe(key); // Keeps the reload fix

        ShapedRecipe r = new ShapedRecipe(key, getItem());
        r.shape("AAA", "APA", "AAA");
        r.setIngredient('P', Material.NETHERITE_PICKAXE);
        r.setIngredient('A', Material.AMETHYST_BLOCK);
        plugin.getServer().addRecipe(r);
    }

    public static ItemStack getItem() {
        ItemStack i = new ItemStack(Material.NETHERITE_PICKAXE);
        ItemMeta m = i.getItemMeta();
        m.setDisplayName(ChatColor.DARK_PURPLE + "⚛ Bedrock Breaker");
        m.setCustomModelData(ID);
        m.setLore(Arrays.asList(
                ChatColor.GRAY + "Defies the laws of physics.",
                ChatColor.GOLD + "Ability: Matter Dissolve",
                ChatColor.YELLOW + "Left-Click Bedrock to destroy it.",
                ChatColor.RED + "Single Use (Breaks Instantly)."
        ));
        i.setItemMeta(m);
        return i;
    }

    // CHANGED: We now listen for a Click, not a Break
    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        // 1. Must be a Left Click on a Block
        if (e.getAction() != Action.LEFT_CLICK_BLOCK) return;

        Block b = e.getClickedBlock();
        if (b == null) return;

        // 2. Must be holding the item
        ItemStack hand = e.getPlayer().getInventory().getItemInMainHand();
        if (!isItem(hand)) return;

        // 3. Must be Bedrock
        if (b.getType() == Material.BEDROCK) {
            // Cancel the punch animation/event
            e.setCancelled(true);

            // DESTROY LOGIC
            b.setType(Material.AIR);
            b.getWorld().spawnParticle(Particle.EXPLOSION, b.getLocation().add(0.5, 0.5, 0.5), 1);
            b.getWorld().playSound(b.getLocation(), Sound.BLOCK_ANVIL_DESTROY, 1f, 0.5f);

            // Remove the pickaxe (Single Use)
            e.getPlayer().getInventory().setItemInMainHand(null);
            e.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE + "The pickaxe shattered against the bedrock, destroying it forever!");
        }
    }

    private boolean isItem(ItemStack i) {
        return i != null && i.hasItemMeta() && i.getItemMeta().hasCustomModelData() && i.getItemMeta().getCustomModelData() == ID;
    }
}