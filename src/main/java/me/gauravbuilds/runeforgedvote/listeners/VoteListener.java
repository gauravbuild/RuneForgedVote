package me.gauravbuilds.runeforgedvote.listeners;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import me.gauravbuilds.runeforgedvote.RuneForgedVote;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class VoteListener implements Listener {

    private final RuneForgedVote plugin;

    public VoteListener(RuneForgedVote plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onVotifierEvent(VotifierEvent event) {
        Vote vote = event.getVote();

        // Debug message to Console (Helps you know if Votifier is actually working)
        plugin.getLogger().info("Received vote from " + vote.getUsername() + " via " + vote.getServiceName());

        // CRITICAL: Votifier runs async. We must jump to the Main Thread to modify the world.
        Bukkit.getScheduler().runTask(plugin, () -> {
            plugin.getVoteManager().handleVote(vote.getUsername(), vote.getServiceName());
        });
    }
}