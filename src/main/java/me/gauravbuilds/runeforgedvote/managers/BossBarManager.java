package me.gauravbuilds.runeforgedvote.managers;

import me.gauravbuilds.runeforgedvote.RuneForgedVote;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class BossBarManager implements Listener {

    private final RuneForgedVote plugin;
    private BossBar bossBar;

    public BossBarManager(RuneForgedVote plugin) {
        this.plugin = plugin;
        createBar();
    }

    private void createBar() {
        // Create a Purple Bar with 10 segments
        bossBar = Bukkit.createBossBar(
                formatTitle(0, 50),
                BarColor.PURPLE,
                BarStyle.SEGMENTED_10
        );
        bossBar.setProgress(0.0);

        // Add all currently online players
        for (Player p : Bukkit.getOnlinePlayers()) {
            bossBar.addPlayer(p);
        }
    }

    public void updateBar(int currentVotes, int maxVotes) {
        double progress = (double) currentVotes / maxVotes;

        // Clamp value between 0.0 and 1.0
        if (progress < 0) progress = 0;
        if (progress > 1) progress = 1;

        bossBar.setProgress(progress);
        bossBar.setTitle(formatTitle(currentVotes, maxVotes));

        // Change color based on progress (Blue -> Purple -> Red/White)
        if (progress >= 1.0) {
            bossBar.setColor(BarColor.WHITE); // Party Time!
            bossBar.setStyle(BarStyle.SOLID);
        } else if (progress >= 0.8) {
            bossBar.setColor(BarColor.RED); // Almost there!
        } else {
            bossBar.setColor(BarColor.PURPLE); // Normal
        }
    }

    private String formatTitle(int current, int max) {
        return ChatColor.translateAlternateColorCodes('&',
                "&d&lASTRAL ALIGNMENT &7» &f" + current + "&7/&f" + max + " Votes");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        // Show bar to players when they join
        if (bossBar != null) {
            bossBar.addPlayer(event.getPlayer());
        }
    }

    public void removeBar() {
        if (bossBar != null) {
            bossBar.removeAll();
        }
    }
}