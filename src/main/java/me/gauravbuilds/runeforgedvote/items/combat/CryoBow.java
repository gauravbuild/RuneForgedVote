package me.gauravbuilds.runeforgedvote.items.combat;

import me.gauravbuilds.runeforgedvote.RuneForgedVote;
import org.bukkit.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.Arrays;

public class CryoBow implements Listener {
    private final RuneForgedVote plugin;
    public static final int ID = 3002;

    public CryoBow(RuneForgedVote p) { this.plugin = p; }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        NamespacedKey key = new NamespacedKey(plugin, "cryo_bow");
        plugin.getServer().removeRecipe(key);

        ShapedRecipe r = new ShapedRecipe(key, getItem());
        r.shape(" C ", "CBC", " C ");
        r.setIngredient('B', Material.BOW);
        r.setIngredient('C', Material.PRISMARINE_SHARD); // Cryo Fragment
        plugin.getServer().addRecipe(r);
    }

    public static ItemStack getItem() {
        ItemStack i = new ItemStack(Material.BOW);
        ItemMeta m = i.getItemMeta();
        m.setDisplayName(ChatColor.AQUA + "🏹 Cryo Bow");
        m.setCustomModelData(ID);
        m.setLore(Arrays.asList(ChatColor.GRAY + "Chills the air around it.", ChatColor.GOLD + "Ability: Frostbite", ChatColor.YELLOW + "Arrows apply Slowness II for 3s."));
        i.setItemMeta(m);
        return i;
    }

    // Tag the arrow when shot
    @EventHandler
    public void onShoot(EntityShootBowEvent e) {
        if (isItem(e.getBow()) && e.getProjectile() instanceof Arrow) {
            e.getProjectile().setMetadata("CryoArrow", new FixedMetadataValue(plugin, true));
            e.getEntity().getWorld().playSound(e.getEntity().getLocation(), Sound.BLOCK_GLASS_BREAK, 1f, 2f);
        }
    }

    // Check tag on hit
    @EventHandler
    public void onHit(EntityDamageByEntityEvent e) {
        if (e.getDamager().hasMetadata("CryoArrow") && e.getEntity() instanceof LivingEntity) {
            LivingEntity target = (LivingEntity) e.getEntity();
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1)); // 3s, Lvl 2
            target.getWorld().spawnParticle(Particle.SNOWFLAKE, target.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3, 0.05);
        }
    }
    private boolean isItem(ItemStack i) { return i != null && i.hasItemMeta() && i.getItemMeta().hasCustomModelData() && i.getItemMeta().getCustomModelData() == ID; }
}