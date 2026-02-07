package me.gauravbuilds.runeforgedvote.items.combat;

import me.gauravbuilds.runeforgedvote.RuneForgedVote;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Arrays;

public class EscapeTotem implements Listener {
    private final RuneForgedVote plugin;
    public static final int ID = 3009;

    public EscapeTotem(RuneForgedVote p) { this.plugin = p; }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        NamespacedKey key = new NamespacedKey(plugin, "escape_totem");
        plugin.getServer().removeRecipe(key);

        ShapedRecipe r = new ShapedRecipe(key, getItem());
        r.shape(" A ", "ATA", " A ");
        r.setIngredient('T', Material.TOTEM_OF_UNDYING);
        r.setIngredient('A', Material.AMETHYST_SHARD);
        plugin.getServer().addRecipe(r);
    }

    public static ItemStack getItem() {
        ItemStack i = new ItemStack(Material.TOTEM_OF_UNDYING);
        ItemMeta m = i.getItemMeta();
        m.setDisplayName(ChatColor.LIGHT_PURPLE + "🔮 Escape Totem");
        m.setCustomModelData(ID);
        m.setLore(Arrays.asList(ChatColor.GRAY + "The void offers a way out.", ChatColor.GOLD + "Ability: Rift Walk", ChatColor.YELLOW + "Teleports you to spawn on fatal damage.", ChatColor.RED + "Single Use."));
        i.setItemMeta(m);
        return i;
    }

    @EventHandler
    public void onFatalDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player p = (Player) e.getEntity();

        // Check if dead
        if (p.getHealth() - e.getFinalDamage() <= 0) {
            ItemStack main = p.getInventory().getItemInMainHand();
            ItemStack off = p.getInventory().getItemInOffHand();
            ItemStack totem = isItem(main) ? main : (isItem(off) ? off : null);

            if (totem != null) {
                e.setCancelled(true);
                p.setHealth(p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
                totem.setAmount(totem.getAmount() - 1); // Consume

                // Teleport to Spawn
                Location spawn = p.getWorld().getSpawnLocation(); // Or VoteSpawn if set
                p.teleport(spawn);

                p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
                p.sendMessage(ChatColor.LIGHT_PURPLE + "The totem whisked you away from death!");
            }
        }
    }
    private boolean isItem(ItemStack i) { return i != null && i.hasItemMeta() && i.getItemMeta().hasCustomModelData() && i.getItemMeta().getCustomModelData() == ID; }
}