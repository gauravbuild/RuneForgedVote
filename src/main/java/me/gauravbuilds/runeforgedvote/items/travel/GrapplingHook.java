package me.gauravbuilds.runeforgedvote.items.travel;

import me.gauravbuilds.runeforgedvote.RuneForgedVote;
import org.bukkit.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import java.util.Arrays;

public class GrapplingHook implements Listener {
    private final RuneForgedVote plugin;
    public static final int ID = 5004;

    public GrapplingHook(RuneForgedVote p) { this.plugin = p; }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        NamespacedKey key = new NamespacedKey(plugin, "grappling_hook");
        plugin.getServer().removeRecipe(key);

        ShapedRecipe r = new ShapedRecipe(key, getItem());
        r.shape(" T ", "TFT", " T ");
        r.setIngredient('F', Material.FISHING_ROD);
        r.setIngredient('T', Material.EMERALD);
        plugin.getServer().addRecipe(r);
    }

    public static ItemStack getItem() {
        ItemStack i = new ItemStack(Material.FISHING_ROD);
        ItemMeta m = i.getItemMeta();
        m.setDisplayName(ChatColor.GREEN + "🪝 Grappling Hook");
        m.setCustomModelData(ID);
        m.setLore(Arrays.asList(ChatColor.GRAY + "Swing through the jungle.", ChatColor.GOLD + "Ability: Hoist", ChatColor.YELLOW + "Pulls you towards the hook."));
        i.setItemMeta(m);
        return i;
    }

    @EventHandler
    public void onFish(PlayerFishEvent e) {
        if (!isItem(e.getPlayer().getInventory().getItemInMainHand())) return;

        if (e.getState() == PlayerFishEvent.State.REEL_IN || e.getState() == PlayerFishEvent.State.IN_GROUND) {
            Location pLoc = e.getPlayer().getLocation();
            Location hLoc = e.getHook().getLocation();

            // Calculate Vector
            Vector v = hLoc.toVector().subtract(pLoc.toVector()).normalize().multiply(1.5).setY(0.5);
            e.getPlayer().setVelocity(v);
            e.getPlayer().playSound(pLoc, Sound.ENTITY_MAGMA_CUBE_JUMP, 1f, 1f);
        }
    }

    private boolean isItem(ItemStack i) { return i != null && i.hasItemMeta() && i.getItemMeta().hasCustomModelData() && i.getItemMeta().getCustomModelData() == ID; }
}