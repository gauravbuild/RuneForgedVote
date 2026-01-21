package me.gauravbuilds.runeforgedvote.listeners;

import me.gauravbuilds.runeforgedvote.RuneForgedVote;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    private final RuneForgedVote plugin;

    public ChatListener(RuneForgedVote plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (plugin.getVoteManager().isChatMuted()) {
            if (event.getPlayer().hasPermission("rfvote.bypassmute")) return;

            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.chat-muted", "&cShhh... The stars are aligning.")));
        }
    }
}