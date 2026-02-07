package me.gauravbuilds.runeforgedvote.items.combat;

import me.gauravbuilds.runeforgedvote.RuneForgedVote;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Arrays;

public class VampireDagger implements Listener {
    private final RuneForgedVote plugin;
    public static final int ID = 3003;

    public VampireDagger(RuneForgedVote p) { this.plugin = p; }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        NamespacedKey key = new NamespacedKey(plugin, "vampire_dagger");
        plugin.getServer().removeRecipe(key);

        ShapedRecipe r = new ShapedRecipe(key, getItem());
        r.shape(" A ", "ASA", " A ");
        r.setIngredient('S', Material.IRON_SWORD); // Iron = Low Damage
        r.setIngredient('A', Material.AMETHYST_SHARD); // Aether Shard
        plugin.getServer().addRecipe(r);
    }

    public static ItemStack getItem() {
        ItemStack i = new ItemStack(Material.IRON_SWORD);
        ItemMeta m = i.getItemMeta();
        m.setDisplayName(ChatColor.DARK_RED + "🩸 Vampire Dagger");
        m.setCustomModelData(ID);
        m.setLore(Arrays.asList(ChatColor.GRAY + "Thirsts for life essence.", ChatColor.GOLD + "Ability: Life Leech", ChatColor.YELLOW + "Heals +1 Heart per hit.", ChatColor.RED + "Cannot be repaired."));
        i.setItemMeta(m);
        return i;
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)) return;
        Player p = (Player) e.getDamager();

        if (isItem(p.getInventory().getItemInMainHand())) {
            double max = p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            double newHealth = Math.min(max, p.getHealth() + 2.0); // +2 HP = 1 Heart
            p.setHealth(newHealth);

            p.getWorld().spawnParticle(Particle.HEART, p.getLocation().add(0, 2, 0), 1);
            p.playSound(p.getLocation(), Sound.ENTITY_WITCH_DRINK, 0.5f, 1f);
        }
    }
    private boolean isItem(ItemStack i) { return i != null && i.hasItemMeta() && i.getItemMeta().hasCustomModelData() && i.getItemMeta().getCustomModelData() == ID; }
}