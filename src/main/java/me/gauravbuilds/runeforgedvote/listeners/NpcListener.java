package me.gauravbuilds.runeforgedvote.listeners;

import me.gauravbuilds.runeforgedvote.RuneForgedVote;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class NpcListener implements Listener {

    private final RuneForgedVote plugin;

    // NPC Names to Service ID Mapping
    private final Map<String, Integer> npcMap = new HashMap<>();

    public NpcListener(RuneForgedVote plugin) {
        this.plugin = plugin;

        // Define your NPC Names here!
        // Make sure these match the names you give your Citizens/Entities
        npcMap.put("Ignis", 1);
        npcMap.put("Cryo", 2);
        npcMap.put("Terra", 3);
        npcMap.put("Aether", 4);
    }

    @EventHandler
    public void onNpcClick(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        String name = ChatColor.stripColor(entity.getCustomName());

        if (name == null || !npcMap.containsKey(name)) return;

        int serviceId = npcMap.get(name);
        Player player = event.getPlayer();
        event.setCancelled(true); // Don't actually "interact" (trade/ride)

        // --- CHECK MODE ---
        if (plugin.getConfig().getBoolean("demo-mode")) {
            handleDemoVote(player, serviceId, name);
        } else {
            handleLiveLink(player, serviceId);
        }
    }

    private void handleDemoVote(Player player, int id, String npcName) {
        // 1. Check Cooldown (24 Hours)
        long lastVote = plugin.getConfig().getLong("data." + player.getUniqueId() + "." + id, 0);
        long currentTime = System.currentTimeMillis();
        long cooldownTime = TimeUnit.HOURS.toMillis(24);

        if (currentTime - lastVote < cooldownTime) {
            // Still on cooldown
            long remaining = cooldownTime - (currentTime - lastVote);
            long hours = TimeUnit.MILLISECONDS.toHours(remaining);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(remaining) % 60;

            String msg = plugin.getConfig().getString("messages.cooldown", "&cWait %time%.")
                    .replace("%time%", String.format("%dh %dm", hours, minutes));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
            player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1f, 1f);
            return;
        }

        // 2. Process "Fake" Vote
        // We simulate the service name as the NPC name
        plugin.getVoteManager().handleVote(player.getName(), npcName);

        // 3. Save Cooldown
        plugin.getConfig().set("data." + player.getUniqueId() + "." + id, currentTime);
        plugin.saveConfig();
    }

    private void handleLiveLink(Player player, int id) {
        String link = plugin.getConfig().getString("links." + id, "https://example.com");
        String msg = plugin.getConfig().getString("messages.link-sent")
                .replace("%link%", link);

        // Send clickable link
        TextComponent component = new TextComponent(ChatColor.translateAlternateColorCodes('&', msg));
        component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link));

        player.spigot().sendMessage(component);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
    }
}