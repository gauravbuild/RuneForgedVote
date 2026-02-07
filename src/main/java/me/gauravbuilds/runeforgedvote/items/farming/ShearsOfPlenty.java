package me.gauravbuilds.runeforgedvote.items.farming;

import me.gauravbuilds.runeforgedvote.RuneForgedVote;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor; // Added Missing Import
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Arrays;
import java.util.Random;

public class ShearsOfPlenty implements Listener {
    private final RuneForgedVote plugin;
    public static final int ID = 4002;
    private final Random random = new Random();

    public ShearsOfPlenty(RuneForgedVote p) { this.plugin = p; }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        NamespacedKey key = new NamespacedKey(plugin, "shears_of_plenty");
        plugin.getServer().removeRecipe(key);

        ShapedRecipe r = new ShapedRecipe(key, getItem());
        r.shape(" T ", "TST", " T ");
        r.setIngredient('S', Material.SHEARS);
        r.setIngredient('T', Material.EMERALD);
        plugin.getServer().addRecipe(r);
    }

    public static ItemStack getItem() {
        ItemStack i = new ItemStack(Material.SHEARS);
        ItemMeta m = i.getItemMeta();
        m.setDisplayName(ChatColor.YELLOW + "✂ Shears of Plenty");
        m.setCustomModelData(ID);
        m.setLore(Arrays.asList(ChatColor.GRAY + "Sharp and bountiful.", ChatColor.GOLD + "Ability: Abundance", ChatColor.YELLOW + "Sheep drop 4-5 Wool."));
        i.setItemMeta(m);
        return i;
    }

    @SuppressWarnings({"deprecation", "removal"})
    @EventHandler
    public void onShear(PlayerShearEntityEvent e) {
        if (!(e.getEntity() instanceof Sheep)) return;
        ItemStack hand = e.getItem();
        if (!isItem(hand)) return;

        Sheep sheep = (Sheep) e.getEntity();
        e.setCancelled(true);

        // Drop 4-5 Wool
        int amount = 4 + random.nextInt(2);
        Material woolType = convertColorToMaterial(sheep.getColor());
        if (woolType != null) {
            sheep.getWorld().dropItemNaturally(sheep.getLocation(), new ItemStack(woolType, amount));
        }

        // Standard Bukkit way (Deprecated but Safe)
        sheep.setSheared(true);
        sheep.getWorld().playSound(sheep.getLocation(), Sound.ENTITY_SHEEP_SHEAR, 1f, 1f);

        // Damage Item
        Damageable meta = (Damageable) hand.getItemMeta();
        meta.setDamage(meta.getDamage() + 1);
        hand.setItemMeta(meta);
        if (meta.getDamage() >= hand.getType().getMaxDurability()) hand.setAmount(0);
    }

    private Material convertColorToMaterial(DyeColor color) {
        if (color == null) return Material.WHITE_WOOL;
        try {
            return Material.valueOf(color.name() + "_WOOL");
        } catch (Exception e) { return Material.WHITE_WOOL; }
    }

    private boolean isItem(ItemStack i) { return i != null && i.hasItemMeta() && i.getItemMeta().hasCustomModelData() && i.getItemMeta().getCustomModelData() == ID; }
}