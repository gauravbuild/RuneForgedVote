package me.gauravbuilds.runeforgedvote.items.mining;

import me.gauravbuilds.runeforgedvote.RuneForgedVote;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SeismicHammer implements Listener {
    private final RuneForgedVote plugin;
    public static final int ID = 2003;
    private final List<Material> TARGETS = Arrays.asList(Material.STONE, Material.COBBLESTONE, Material.DEEPSLATE, Material.COBBLED_DEEPSLATE, Material.ANDESITE, Material.DIORITE, Material.GRANITE, Material.TUFF);

    public SeismicHammer(RuneForgedVote p) { this.plugin = p; }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        NamespacedKey key = new NamespacedKey(plugin, "seismic_hammer");
        plugin.getServer().removeRecipe(key); // FIX

        ShapedRecipe r = new ShapedRecipe(key, getItem());
        r.shape(" T ", "TPT", " T ");
        r.setIngredient('P', Material.NETHERITE_PICKAXE);
        r.setIngredient('T', Material.EMERALD);
        plugin.getServer().addRecipe(r);
    }

    public static ItemStack getItem() {
        ItemStack i = new ItemStack(Material.NETHERITE_PICKAXE);
        ItemMeta m = i.getItemMeta();
        m.setDisplayName(ChatColor.YELLOW + "🔨 Seismic Hammer");
        m.setCustomModelData(ID);
        List<String> l = new ArrayList<>();
        l.add(ChatColor.GRAY + "Shatters the earth itself.");
        l.add(ChatColor.GOLD + "Ability: Tectonic Crush");
        l.add(ChatColor.YELLOW + "Breaks 3x3 Stone/Deepslate.");
        l.add(ChatColor.RED + "Cannot be repaired.");
        m.setLore(l);
        i.setItemMeta(m);
        return i;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        if (!isItem(e.getPlayer().getInventory().getItemInMainHand())) return;
        if (!TARGETS.contains(e.getBlock().getType())) return;

        Block center = e.getBlock();
        List<Block> toBreak = new ArrayList<>();
        float pitch = e.getPlayer().getLocation().getPitch();

        if (pitch < -45 || pitch > 45) {
            for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) toBreak.add(center.getRelative(x, 0, z));
        } else {
            BlockFace face = e.getPlayer().getFacing();
            if (face == BlockFace.NORTH || face == BlockFace.SOUTH) {
                for (int x = -1; x <= 1; x++) for (int y = -1; y <= 1; y++) toBreak.add(center.getRelative(x, y, 0));
            } else {
                for (int z = -1; z <= 1; z++) for (int y = -1; y <= 1; y++) toBreak.add(center.getRelative(0, y, z));
            }
        }

        for (Block b : toBreak) {
            if (TARGETS.contains(b.getType()) && !b.equals(center)) b.breakNaturally(e.getPlayer().getInventory().getItemInMainHand());
        }
        center.getWorld().playSound(center.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 1f, 0.5f);
    }

    @EventHandler
    public void onAnvil(PrepareAnvilEvent e) {
        if (isItem(e.getInventory().getFirstItem()) || isItem(e.getInventory().getSecondItem())) {
            e.setResult(null);
        }
    }

    private boolean isItem(ItemStack i) { return i != null && i.hasItemMeta() && i.getItemMeta().hasCustomModelData() && i.getItemMeta().getCustomModelData() == ID; }
}