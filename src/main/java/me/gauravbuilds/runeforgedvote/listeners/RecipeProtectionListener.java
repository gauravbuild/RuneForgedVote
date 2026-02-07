package me.gauravbuilds.runeforgedvote.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class RecipeProtectionListener implements Listener {

    private final int ID_IGNIS = 1001;
    private final int ID_CRYO = 1002;
    private final int ID_TERRA = 1003;
    private final int ID_AETHER = 1004;

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        boolean hasShards = false;
        boolean hasArtifactInput = false;

        // 1. Scan the crafting grid (Matrix)
        for (ItemStack item : event.getInventory().getMatrix()) {
            if (item == null || item.getType() == Material.AIR) continue;

            if (isCustomShard(item)) {
                hasShards = true;
            }
            // CHECK: Is the player trying to use an existing Artifact as an ingredient?
            if (isRuneForgedArtifact(item)) {
                hasArtifactInput = true;
            }
        }

        // RULE 1: Never allow an existing Artifact to be used as an ingredient.
        // (Prevents crafting an Ignis Pickaxe using an existing Ignis Pickaxe)
        if (hasArtifactInput) {
            event.getInventory().setResult(null);
            return;
        }

        // RULE 2: If using Shards, the result MUST be a valid RuneForged Artifact.
        // (Prevents using Shards for vanilla recipes like Eye of Ender)
        if (hasShards) {
            ItemStack result = event.getInventory().getResult();
            if (result == null || !isRuneForgedArtifact(result)) {
                event.getInventory().setResult(null);
            }
        }
    }

    @EventHandler
    public void onBrewingStandClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        if (event.getInventory().getType() != InventoryType.BREWING) return;

        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();

        if (isCustomShard(cursor) || isCustomShard(current)) {
            if (event.getSlotType() == InventoryType.SlotType.FUEL ||
                    event.getSlotType() == InventoryType.SlotType.CRAFTING) {

                event.setCancelled(true);
                event.getWhoClicked().sendMessage(ChatColor.RED + "The energy of this artifact is too unstable for alchemy!");
            }
        }
    }

    private boolean isCustomShard(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        if (!item.hasItemMeta()) return false;
        if (!item.getItemMeta().hasCustomModelData()) return false;

        int data = item.getItemMeta().getCustomModelData();
        return data >= 1001 && data <= 1004;
    }

    private boolean isRuneForgedArtifact(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        if (!item.hasItemMeta()) return false;
        if (!item.getItemMeta().hasCustomModelData()) return false;

        // Items start at 2001 (Ignis Pickaxe)
        return item.getItemMeta().getCustomModelData() >= 2000;
    }
}