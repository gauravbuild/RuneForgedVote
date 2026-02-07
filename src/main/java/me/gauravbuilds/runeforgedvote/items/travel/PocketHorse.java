package me.gauravbuilds.runeforgedvote.items.travel;

import me.gauravbuilds.runeforgedvote.RuneForgedVote;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.SkeletonHorse;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.Arrays;

public class PocketHorse implements Listener {
    private final RuneForgedVote plugin;
    public static final int ID = 5008;

    public PocketHorse(RuneForgedVote p) { this.plugin = p; }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        NamespacedKey key = new NamespacedKey(plugin, "pocket_horse");
        plugin.getServer().removeRecipe(key);

        ShapedRecipe r = new ShapedRecipe(key, getItem());
        r.shape(" T ", "TST", " T ");
        r.setIngredient('S', Material.SADDLE);
        r.setIngredient('T', Material.EMERALD);
        plugin.getServer().addRecipe(r);
    }

    public static ItemStack getItem() {
        ItemStack i = new ItemStack(Material.SADDLE);
        ItemMeta m = i.getItemMeta();
        m.setDisplayName(ChatColor.GREEN + "🐎 Pocket Horse");
        m.setCustomModelData(ID);
        m.setLore(Arrays.asList(ChatColor.GRAY + "A ghostly steed.", ChatColor.GOLD + "Ability: Summon", ChatColor.YELLOW + "Spawns a Skeleton Horse for 5m.", ChatColor.RED + "Consumable."));
        i.setItemMeta(m);
        return i;
    }

    @EventHandler
    public void onUse(PlayerInteractEvent e) {
        if (e.getAction().toString().contains("RIGHT") && isItem(e.getItem())) {
            e.getItem().setAmount(e.getItem().getAmount() - 1);

            SkeletonHorse horse = (SkeletonHorse) e.getPlayer().getWorld().spawnEntity(e.getPlayer().getLocation(), EntityType.SKELETON_HORSE);
            horse.setTamed(true);
            horse.setOwner(e.getPlayer());
            horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
            e.getPlayer().sendMessage(ChatColor.GREEN + "Steed summoned!");

            // Despawn after 5 minutes
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (horse.isValid()) {
                        horse.remove();
                        horse.getWorld().spawnParticle(Particle.POOF, horse.getLocation(), 10);
                    }
                }
            }.runTaskLater(plugin, 6000L);
        }
    }

    private boolean isItem(ItemStack i) { return i != null && i.hasItemMeta() && i.getItemMeta().hasCustomModelData() && i.getItemMeta().getCustomModelData() == ID; }
}