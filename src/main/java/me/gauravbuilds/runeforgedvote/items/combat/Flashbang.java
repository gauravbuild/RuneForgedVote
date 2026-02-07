package me.gauravbuilds.runeforgedvote.items.combat;

import me.gauravbuilds.runeforgedvote.RuneForgedVote;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.Arrays;

public class Flashbang implements Listener {
    private final RuneForgedVote plugin;
    public static final int ID = 3006;

    public Flashbang(RuneForgedVote p) { this.plugin = p; }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        NamespacedKey key = new NamespacedKey(plugin, "flashbang");
        plugin.getServer().removeRecipe(key);

        ShapedRecipe r = new ShapedRecipe(key, getItem());
        r.shape(" A ", "ASA", " A ");
        r.setIngredient('S', Material.SNOWBALL);
        r.setIngredient('A', Material.AMETHYST_SHARD);
        plugin.getServer().addRecipe(r);
    }

    public static ItemStack getItem() {
        ItemStack i = new ItemStack(Material.SNOWBALL);
        ItemMeta m = i.getItemMeta();
        m.setDisplayName(ChatColor.WHITE + "✴ Flashbang");
        m.setCustomModelData(ID);
        m.setLore(Arrays.asList(ChatColor.GRAY + "A grenade of pure light.", ChatColor.GOLD + "Ability: Blind", ChatColor.YELLOW + "Blinds enemies near impact.", ChatColor.RED + "Consumable."));
        i.setItemMeta(m);
        return i;
    }

    @EventHandler
    public void onThrow(PlayerInteractEvent e) {
        if (e.getItem() != null && isItem(e.getItem())) {
            if (e.getAction().toString().contains("RIGHT")) {
                e.setCancelled(true);
                e.getItem().setAmount(e.getItem().getAmount() - 1);
                Snowball s = e.getPlayer().launchProjectile(Snowball.class);
                s.setItem(getItem());
                s.setCustomName("Flashbang");
            }
        }
    }

    @EventHandler
    public void onHit(ProjectileHitEvent e) {
        if (e.getEntity() instanceof Snowball && "Flashbang".equals(e.getEntity().getCustomName())) {
            Location loc = e.getEntity().getLocation();
            loc.getWorld().spawnParticle(Particle.FLASH, loc, 5);
            loc.getWorld().playSound(loc, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 2f, 1.5f);

            for (Entity nearby : loc.getWorld().getNearbyEntities(loc, 5, 5, 5)) {
                if (nearby instanceof LivingEntity) {
                    ((LivingEntity) nearby).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0)); // 5s
                    ((LivingEntity) nearby).addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1));
                }
            }
        }
    }
    private boolean isItem(ItemStack i) { return i != null && i.hasItemMeta() && i.getItemMeta().hasCustomModelData() && i.getItemMeta().getCustomModelData() == ID; }
}