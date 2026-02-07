package me.gauravbuilds.runeforgedvote.items.farming;

import me.gauravbuilds.runeforgedvote.RuneForgedVote;
import org.bukkit.*;
import org.bukkit.entity.Animals;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Arrays;

public class BreedingCandy implements Listener {
    private final RuneForgedVote plugin;
    public static final int ID = 4009;

    public BreedingCandy(RuneForgedVote p) { this.plugin = p; }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        NamespacedKey key = new NamespacedKey(plugin, "breeding_candy");
        plugin.getServer().removeRecipe(key);

        ShapedRecipe r = new ShapedRecipe(key, getItem());
        r.shape(" A ", "ASA", " A ");
        r.setIngredient('S', Material.SUGAR);
        r.setIngredient('A', Material.AMETHYST_SHARD);
        plugin.getServer().addRecipe(r);
    }

    public static ItemStack getItem() {
        ItemStack i = new ItemStack(Material.SUGAR);
        ItemMeta m = i.getItemMeta();
        m.setDisplayName(ChatColor.LIGHT_PURPLE + "🍬 Breeding Candy");
        m.setCustomModelData(ID);
        m.setLore(Arrays.asList(ChatColor.GRAY + "Sweet and magical.", ChatColor.GOLD + "Ability: Aphrodisiac", ChatColor.YELLOW + "Animals breed instantly.", ChatColor.YELLOW + "Ignores cooldowns.", ChatColor.RED + "Consumable."));
        i.setItemMeta(m);
        return i;
    }

    @EventHandler
    public void onFeed(PlayerInteractEntityEvent e) {
        ItemStack hand = e.getPlayer().getInventory().getItemInMainHand();
        if (!isItem(hand)) return;

        if (e.getRightClicked() instanceof Animals) {
            Animals animal = (Animals) e.getRightClicked();
            e.setCancelled(true);
            hand.setAmount(hand.getAmount() - 1);

            if (!animal.isAdult()) animal.setAge(0); // Grow up
            animal.setLoveModeTicks(600); // 30s
            animal.getWorld().spawnParticle(Particle.HEART, animal.getLocation().add(0, 1, 0), 10);
            e.getPlayer().playSound(animal.getLocation(), Sound.ENTITY_CAT_PURR, 1f, 1f);
        }
    }
    private boolean isItem(ItemStack i) { return i != null && i.hasItemMeta() && i.getItemMeta().hasCustomModelData() && i.getItemMeta().getCustomModelData() == ID; }
}