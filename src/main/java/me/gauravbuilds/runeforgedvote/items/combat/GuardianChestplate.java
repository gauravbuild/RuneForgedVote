package me.gauravbuilds.runeforgedvote.items.combat;

import me.gauravbuilds.runeforgedvote.RuneForgedVote;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.*;

public class GuardianChestplate implements Listener {
    private final RuneForgedVote plugin;
    public static final int ID = 3005;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public GuardianChestplate(RuneForgedVote p) { this.plugin = p; }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        NamespacedKey key = new NamespacedKey(plugin, "guardian_chestplate");
        plugin.getServer().removeRecipe(key);

        ShapedRecipe r = new ShapedRecipe(key, getItem());
        r.shape(" T ", "TCT", " T ");
        r.setIngredient('C', Material.NETHERITE_CHESTPLATE);
        r.setIngredient('T', Material.EMERALD);
        plugin.getServer().addRecipe(r);
    }

    public static ItemStack getItem() {
        ItemStack i = new ItemStack(Material.NETHERITE_CHESTPLATE);
        ItemMeta m = i.getItemMeta();
        m.setDisplayName(ChatColor.GREEN + "🐢 Guardian Chestplate");
        m.setCustomModelData(ID);
        m.setLore(Arrays.asList(ChatColor.GRAY + "Protects the weak.", ChatColor.GOLD + "Ability: Last Stand", ChatColor.YELLOW + "Grants Resistance III if HP < 3 hearts.", ChatColor.GRAY + "Cooldown: 60s"));
        i.setItemMeta(m);
        return i;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player p = (Player) e.getEntity();

        // Check if wearing chestplate
        ItemStack chest = p.getInventory().getChestplate();
        if (!isItem(chest)) return;

        // Check HP Threshold (6.0 = 3 Hearts)
        if (p.getHealth() - e.getFinalDamage() <= 6.0) {
            long now = System.currentTimeMillis();
            if (!cooldowns.containsKey(p.getUniqueId()) || now - cooldowns.get(p.getUniqueId()) > 60000) {

                cooldowns.put(p.getUniqueId(), now);
                p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 2)); // 5s, Lvl 3
                p.getWorld().playSound(p.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1f, 0.5f);
                p.sendMessage(ChatColor.GREEN + "🛡 Guardian Shield Activated!");
                p.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, p.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5);
            }
        }
    }
    private boolean isItem(ItemStack i) { return i != null && i.hasItemMeta() && i.getItemMeta().hasCustomModelData() && i.getItemMeta().getCustomModelData() == ID; }
}