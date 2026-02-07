package me.gauravbuilds.runeforgedvote.items.combat;

import me.gauravbuilds.runeforgedvote.RuneForgedVote;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Arrays;

public class TerraShield implements Listener {
    private final RuneForgedVote plugin;
    public static final int ID = 3004;

    public TerraShield(RuneForgedVote p) { this.plugin = p; }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        NamespacedKey key = new NamespacedKey(plugin, "terra_shield");
        plugin.getServer().removeRecipe(key);

        ShapedRecipe r = new ShapedRecipe(key, getItem());
        r.shape(" T ", "TST", " T ");
        r.setIngredient('S', Material.SHIELD);
        r.setIngredient('T', Material.EMERALD); // Terra Cluster
        plugin.getServer().addRecipe(r);
    }

    public static ItemStack getItem() {
        ItemStack i = new ItemStack(Material.SHIELD);
        ItemMeta m = i.getItemMeta();
        m.setDisplayName(ChatColor.GREEN + "🛡 Terra Shield");
        m.setCustomModelData(ID);
        m.setLore(Arrays.asList(ChatColor.GRAY + "Spiked with living roots.", ChatColor.GOLD + "Ability: Spiked Guard", ChatColor.YELLOW + "Blocking deals damage and knockback.", ChatColor.RED + "Breaks fast."));
        i.setItemMeta(m);
        return i;
    }

    @EventHandler
    public void onDefend(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player victim = (Player) e.getEntity();

        if (victim.isBlocking() && (isItem(victim.getInventory().getItemInMainHand()) || isItem(victim.getInventory().getItemInOffHand()))) {
            // Apply Thorns Damage
            e.getDamager().sendMessage(ChatColor.RED + "You hit the spiked shield!");

            // Custom Thorns logic (independent of enchant)
            if (e.getDamager() instanceof org.bukkit.entity.LivingEntity) {
                ((org.bukkit.entity.LivingEntity) e.getDamager()).damage(3.0, victim);
                // Knockback
                e.getDamager().setVelocity(victim.getLocation().getDirection().multiply(1.5).setY(0.5));
            }

            // Extra Durability Damage
            ItemStack shield = isItem(victim.getInventory().getItemInMainHand()) ? victim.getInventory().getItemInMainHand() : victim.getInventory().getItemInOffHand();
            Damageable meta = (Damageable) shield.getItemMeta();
            meta.setDamage(meta.getDamage() + 5); // 5x durability loss
            shield.setItemMeta(meta);
            if (meta.getDamage() >= shield.getType().getMaxDurability()) shield.setAmount(0);
        }
    }
    private boolean isItem(ItemStack i) { return i != null && i.hasItemMeta() && i.getItemMeta().hasCustomModelData() && i.getItemMeta().getCustomModelData() == ID; }
}