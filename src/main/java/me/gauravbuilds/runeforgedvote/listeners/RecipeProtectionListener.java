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

    // The Custom Model Data IDs we assigned in GeodeListener
    private final int ID_IGNIS = 1001;
    private final int ID_CRYO = 1002;
    private final int ID_TERRA = 1003;
    private final int ID_AETHER = 1004;

    /**
     * Prevents using Custom Shards in Vanilla Crafting Recipes.
     * Example: Prevents Ignis Ember (Blaze Powder) -> Ender Eye
     */
    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        for (ItemStack item : event.getInventory().getMatrix()) {
            if (isCustomShard(item)) {
                // If we find a custom shard in the grid, BLOCK the result.
                // (Later, we will add an exception here for our own custom recipes)
                event.getInventory().setResult(null);
                return;
            }
        }
    }

    /**
     * Prevents placing Custom Shards into Brewing Stands.
     * Example: Prevents Ignis Ember as Fuel or Ingredient.
     */
    @EventHandler
    public void onBrewingStandClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;

        // Only care if interacting with a Brewing Stand
        if (event.getInventory().getType() != InventoryType.BREWING) return;

        // Check if the item being moved/clicked is one of ours
        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();

        // Block placing OUR item into the stand
        if (isCustomShard(cursor) || isCustomShard(current)) {
            // Allow moving it back to player inventory, but BLOCK moving it into the stand slots
            if (event.getSlotType() == InventoryType.SlotType.FUEL ||
                    event.getSlotType() == InventoryType.SlotType.CRAFTING) { // 'Crafting' is the bottle/ingredient slot in brewing

                event.setCancelled(true);
                event.getWhoClicked().sendMessage(ChatColor.RED + "The energy of this artifact is too unstable for alchemy!");
            }
        }
    }

    /**
     * Helper to check if an item is one of our RuneForged Shards
     */
    private boolean isCustomShard(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        if (!item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();
        if (!meta.hasCustomModelData()) return false;

        int data = meta.getCustomModelData();
        return data == ID_IGNIS || data == ID_CRYO || data == ID_TERRA || data == ID_AETHER;
    }
}