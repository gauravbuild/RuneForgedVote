package me.gauravbuilds.runeforgedvote.items.farming;

import me.gauravbuilds.runeforgedvote.RuneForgedVote;
import org.bukkit.*;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import java.util.Arrays;

public class MobNet implements Listener {
    private final RuneForgedVote plugin;
    public static final int ID = 4007;
    private final NamespacedKey storedMobKey;

    public MobNet(RuneForgedVote p) {
        this.plugin = p;
        this.storedMobKey = new NamespacedKey(plugin, "mob_net_type");
    }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        NamespacedKey key = new NamespacedKey(plugin, "mob_net");
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
        m.setDisplayName(ChatColor.WHITE + "🕸 Mob Net");
        m.setCustomModelData(ID);
        m.setLore(Arrays.asList(ChatColor.GRAY + "Catches tiny creatures.", ChatColor.GOLD + "Ability: Capture", ChatColor.YELLOW + "Right-click an animal to catch it.", ChatColor.YELLOW + "Right-click ground to release."));
        i.setItemMeta(m);
        return i;
    }

    // Capture
    @EventHandler
    public void onEntityClick(PlayerInteractEntityEvent e) {
        ItemStack hand = e.getPlayer().getInventory().getItemInMainHand();
        if (!isItem(hand)) return;

        // Prevent capturing if already full
        ItemMeta meta = hand.getItemMeta();
        if (meta.getPersistentDataContainer().has(storedMobKey, PersistentDataType.STRING)) return;

        Entity entity = e.getRightClicked();
        if (entity instanceof Animals) {
            String type = entity.getType().name();

            // Store data
            meta.getPersistentDataContainer().set(storedMobKey, PersistentDataType.STRING, type);
            meta.setDisplayName(ChatColor.GREEN + "🕸 Mob Net (" + type + ")");
            hand.setItemMeta(meta);

            entity.remove();
            e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_EGG_THROW, 1f, 1f);
            e.getPlayer().sendMessage(ChatColor.GREEN + "Caught a " + type + "!");
        }
    }

    // Release
    @EventHandler
    public void onGroundClick(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        ItemStack hand = e.getItem();
        if (!isItem(hand)) return;

        ItemMeta meta = hand.getItemMeta();
        if (!meta.getPersistentDataContainer().has(storedMobKey, PersistentDataType.STRING)) return;

        String typeStr = meta.getPersistentDataContainer().get(storedMobKey, PersistentDataType.STRING);
        EntityType type = EntityType.valueOf(typeStr);

        Location loc = e.getClickedBlock().getLocation().add(0.5, 1, 0.5);
        loc.getWorld().spawnEntity(loc, type);

        // Reset Item
        meta.getPersistentDataContainer().remove(storedMobKey);
        meta.setDisplayName(ChatColor.WHITE + "🕸 Mob Net");
        hand.setItemMeta(meta);

        e.getPlayer().playSound(loc, Sound.ENTITY_CHICKEN_EGG, 1f, 1f);
    }

    private boolean isItem(ItemStack i) { return i != null && i.hasItemMeta() && i.getItemMeta().hasCustomModelData() && i.getItemMeta().getCustomModelData() == ID; }
}