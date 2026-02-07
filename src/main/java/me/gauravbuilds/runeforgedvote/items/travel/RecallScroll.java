package me.gauravbuilds.runeforgedvote.items.travel;

import me.gauravbuilds.runeforgedvote.RuneForgedVote;
import org.bukkit.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.Arrays;

public class RecallScroll implements Listener {
    private final RuneForgedVote plugin;
    public static final int ID = 5006;

    public RecallScroll(RuneForgedVote p) { this.plugin = p; }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        NamespacedKey key = new NamespacedKey(plugin, "recall_scroll");
        plugin.getServer().removeRecipe(key);

        ShapedRecipe r = new ShapedRecipe(key, getItem());
        r.shape(" A ", "APA", " A ");
        r.setIngredient('P', Material.PAPER);
        r.setIngredient('A', Material.AMETHYST_SHARD);
        plugin.getServer().addRecipe(r);
    }

    public static ItemStack getItem() {
        ItemStack i = new ItemStack(Material.PAPER);
        ItemMeta m = i.getItemMeta();
        m.setDisplayName(ChatColor.LIGHT_PURPLE + "📜 Recall Scroll");
        m.setCustomModelData(ID);
        m.setLore(Arrays.asList(ChatColor.GRAY + "Remember where you came from.", ChatColor.GOLD + "Ability: Recall", ChatColor.YELLOW + "Right-click to mark location.", ChatColor.YELLOW + "Teleports you back in 10s.", ChatColor.RED + "Consumable."));
        i.setItemMeta(m);
        return i;
    }

    @EventHandler
    public void onUse(PlayerInteractEvent e) {
        if (e.getAction().toString().contains("RIGHT") && isItem(e.getItem())) {
            e.getItem().setAmount(e.getItem().getAmount() - 1);
            Location loc = e.getPlayer().getLocation();
            e.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE + "Recall point set! Returning in 10s...");

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (e.getPlayer().isOnline()) {
                        e.getPlayer().teleport(loc);
                        e.getPlayer().playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
                        e.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE + "Recalled!");
                    }
                }
            }.runTaskLater(plugin, 200L); // 10s
        }
    }

    private boolean isItem(ItemStack i) { return i != null && i.hasItemMeta() && i.getItemMeta().hasCustomModelData() && i.getItemMeta().getCustomModelData() == ID; }
}