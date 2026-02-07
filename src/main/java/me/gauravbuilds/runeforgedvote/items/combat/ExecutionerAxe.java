package me.gauravbuilds.runeforgedvote.items.combat;

import me.gauravbuilds.runeforgedvote.RuneForgedVote;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Arrays;

public class ExecutionerAxe implements Listener {
    private final RuneForgedVote plugin;
    public static final int ID = 3010;

    public ExecutionerAxe(RuneForgedVote p) { this.plugin = p; }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        NamespacedKey key = new NamespacedKey(plugin, "executioner_axe");
        plugin.getServer().removeRecipe(key);

        ShapedRecipe r = new ShapedRecipe(key, getItem());
        r.shape(" I ", "IAI", " I ");
        r.setIngredient('A', Material.NETHERITE_AXE);
        r.setIngredient('I', Material.BLAZE_POWDER);
        plugin.getServer().addRecipe(r);
    }

    public static ItemStack getItem() {
        ItemStack i = new ItemStack(Material.NETHERITE_AXE);
        ItemMeta m = i.getItemMeta();
        m.setDisplayName(ChatColor.DARK_RED + "🪓 Executioner's Axe");
        m.setCustomModelData(ID);
        m.setLore(Arrays.asList(ChatColor.GRAY + "Ends suffering.", ChatColor.GOLD + "Ability: Execute", ChatColor.YELLOW + "Deals triple damage if target", ChatColor.YELLOW + "is below 2 hearts."));
        i.setItemMeta(m);
        return i;
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)) return;
        Player p = (Player) e.getDamager();
        if (!isItem(p.getInventory().getItemInMainHand())) return;

        if (e.getEntity() instanceof LivingEntity) {
            LivingEntity target = (LivingEntity) e.getEntity();

            // Check HP < 4 (2 Hearts)
            if (target.getHealth() < 4.0) {
                e.setDamage(e.getDamage() * 3); // TRIPLE DAMAGE
                p.getWorld().playSound(target.getLocation(), Sound.ENTITY_IRON_GOLEM_DAMAGE, 1f, 0.5f);
                p.getWorld().spawnParticle(Particle.CRIT, target.getLocation().add(0, 1, 0), 20);
                p.sendMessage(ChatColor.RED + "EXECUTE!");
            }
        }
    }
    private boolean isItem(ItemStack i) { return i != null && i.hasItemMeta() && i.getItemMeta().hasCustomModelData() && i.getItemMeta().getCustomModelData() == ID; }
}