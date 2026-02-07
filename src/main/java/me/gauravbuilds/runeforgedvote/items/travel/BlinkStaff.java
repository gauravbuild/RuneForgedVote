package me.gauravbuilds.runeforgedvote.items.travel;

import me.gauravbuilds.runeforgedvote.RuneForgedVote;
import org.bukkit.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Arrays;

public class BlinkStaff implements Listener {
    private final RuneForgedVote plugin;
    public static final int ID = 5003;

    public BlinkStaff(RuneForgedVote p) { this.plugin = p; }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        NamespacedKey key = new NamespacedKey(plugin, "blink_staff");
        plugin.getServer().removeRecipe(key);

        ShapedRecipe r = new ShapedRecipe(key, getItem());
        r.shape(" A ", "ASA", " A ");
        r.setIngredient('S', Material.STICK);
        r.setIngredient('A', Material.AMETHYST_SHARD);
        plugin.getServer().addRecipe(r);
    }

    public static ItemStack getItem() {
        ItemStack i = new ItemStack(Material.STICK);
        ItemMeta m = i.getItemMeta();
        m.setDisplayName(ChatColor.LIGHT_PURPLE + "🪄 Blink Staff");
        m.setCustomModelData(ID);
        m.setLore(Arrays.asList(ChatColor.GRAY + "Bend space.", ChatColor.GOLD + "Ability: Blink", ChatColor.YELLOW + "Teleport 8 blocks forward.", ChatColor.YELLOW + "Passes through walls."));
        i.setItemMeta(m);
        return i;
    }

    @EventHandler
    public void onUse(PlayerInteractEvent e) {
        if (e.getAction().toString().contains("RIGHT") && isItem(e.getItem())) {
            Location loc = e.getPlayer().getLocation();
            Location target = loc.clone().add(loc.getDirection().multiply(8));

            // Safety: If target is solid, try to find air above
            if (target.getBlock().getType().isSolid()) {
                target = target.getWorld().getHighestBlockAt(target).getLocation().add(0, 1, 0);
            }

            e.getPlayer().teleport(target);
            e.getPlayer().playSound(target, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
            e.getPlayer().getWorld().spawnParticle(Particle.PORTAL, loc, 10);
        }
    }

    private boolean isItem(ItemStack i) { return i != null && i.hasItemMeta() && i.getItemMeta().hasCustomModelData() && i.getItemMeta().getCustomModelData() == ID; }
}