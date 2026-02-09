package me.gauravbuilds.runeforgedvote.hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.gauravbuilds.runeforgedvote.RuneForgedVote;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class RuneForgedPlaceholder extends PlaceholderExpansion {

    private final RuneForgedVote plugin;

    public RuneForgedPlaceholder(RuneForgedVote plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "runeforgedvote";
    }

    @Override
    public @NotNull String getAuthor() {
        return "zVortexLabs";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return "";

        // %runeforgedvote_total%
        if (params.equalsIgnoreCase("total")) {
            return String.valueOf(plugin.getVoteManager().getTotalVotes(player.getUniqueId()));
        }

        return null;
    }
}