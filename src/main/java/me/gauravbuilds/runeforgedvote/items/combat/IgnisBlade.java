package me.gauravbuilds.runeforgedvote.items.combat;

import me.gauravbuilds.runeforgedvote.RuneForgedVote;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.List;

public class IgnisBlade implements Listener {
    private final RuneForgedVote plugin;
    public static final int ID = 3001;

    public IgnisBlade(RuneForgedVote p) { this.plugin = p; }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        NamespacedKey key = new NamespacedKey(plugin, "ignis_blade");
        plugin.getServer().removeRecipe(key);

        ShapedRecipe r = new ShapedRecipe(key, getItem());
        r.shape(" I ", "ISI", " I ");
        r.setIngredient('S', Material.NETHERITE_SWORD);
        r.setIngredient('I', Material.BLAZE_POWDER); // Ignis Ember
        plugin.getServer().addRecipe(r);
    }

    public static ItemStack getItem() {
        ItemStack i = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta m = i.getItemMeta();
        m.setDisplayName(ChatColor.GOLD + "🔥 Ignis Blade");
        m.setCustomModelData(ID);
        List<String> l = new ArrayList<>();
        l.add(ChatColor.GRAY + "A blade wreathed in eternal flame.");
        l.add(ChatColor.GOLD + "Ability: Searing Heat");
        l.add(ChatColor.YELLOW + "Sets enemies on fire.");
        l.add(ChatColor.YELLOW + "Deals +4 Dmg to burning targets.");
        m.setLore(l);
        m.addEnchant(Enchantment.FIRE_ASPECT, 1, true);
        i.setItemMeta(m);
        return i;
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)) return;
        Player p = (Player) e.getDamager();
        ItemStack hand = p.getInventory().getItemInMainHand();

        if (!isItem(hand)) return;

        if (e.getEntity() instanceof LivingEntity) {
            LivingEntity target = (LivingEntity) e.getEntity();

            // Critical Hit Logic
            if (target.getFireTicks() > 0) {
                e.setDamage(e.getDamage() + 4);
                p.getWorld().spawnParticle(Particle.FLAME, target.getLocation().add(0, 1, 0), 10, 0.2, 0.2, 0.2, 0.05);
                p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.5f, 1.5f);
            }

            // Ensure fire is applied (Fire Aspect handles this mostly, but we reinforce it)
            target.setFireTicks(100); // 5 seconds
        }
    }
    private boolean isItem(ItemStack i) { return i != null && i.hasItemMeta() && i.getItemMeta().hasCustomModelData() && i.getItemMeta().getCustomModelData() == ID; }
}