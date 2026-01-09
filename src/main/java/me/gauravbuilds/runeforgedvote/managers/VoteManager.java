package me.gauravbuilds.runeforgedvote.managers;

import me.gauravbuilds.runeforgedvote.RuneForgedVote;
import org.bukkit.*;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Random;

public class VoteManager {

    private final RuneForgedVote plugin;
    private int globalVotes;
    private final int VOTES_FOR_PARTY;
    private final Random random = new Random();

    public VoteManager(RuneForgedVote plugin) {
        this.plugin = plugin;
        this.globalVotes = plugin.getConfig().getInt("global-votes", 0);
        this.VOTES_FOR_PARTY = plugin.getConfig().getInt("votes-needed", 50);
    }

    public void handleVote(String playerName, String serviceName) {
        // 1. Update Counter
        globalVotes++;
        plugin.getConfig().set("global-votes", globalVotes);
        plugin.saveConfig();

        // 2. Handle Player Logic
        Player player = Bukkit.getPlayer(playerName);
        if (player != null) {
            giveRewards(player);

            // Visuals
            player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1f, 1f);
            player.sendTitle(
                    ChatColor.translateAlternateColorCodes('&', "&6&l★ STAR IGNITED ★"),
                    ChatColor.translateAlternateColorCodes('&', "&7You received &dStardust"),
                    10, 40, 10
            );
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.vote-received", "&eStar Ignited!")));

            // Trigger the Pillar Lightning
            plugin.getPillarManager().ignitePillar(serviceName);
        }

        // 3. Broadcast
        String broadcast = plugin.getConfig().getString("messages.broadcast", "&f%player% &7voted!");
        broadcast = broadcast.replace("%player%", playerName)
                .replace("%current%", String.valueOf(globalVotes))
                .replace("%max%", String.valueOf(VOTES_FOR_PARTY));
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', broadcast));

        // 4. Check for Party
        if (globalVotes >= VOTES_FOR_PARTY) {
            startAstralAlignment();
        }
    }

    private void giveRewards(Player player) {
        ConsoleCommandSender console = Bukkit.getConsoleSender();

        // A. Standard Rewards
        List<String> commands = plugin.getConfig().getStringList("rewards.per-vote");
        for (String cmd : commands) {
            Bukkit.dispatchCommand(console, cmd.replace("%player%", player.getName()));
        }

        // B. Lucky Chance (5%)
        if (random.nextInt(100) < 5) {
            List<String> luckyCmds = plugin.getConfig().getStringList("rewards.lucky-vote");
            for (String cmd : luckyCmds) {
                Bukkit.dispatchCommand(console, cmd.replace("%player%", player.getName()));
            }
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.lucky-trigger", "&dLucky Vote!")));
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);
            player.spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.1);
        }
    }

    private void startAstralAlignment() {
        // RESET
        globalVotes = 0;
        plugin.getConfig().set("global-votes", 0);
        plugin.saveConfig();

        // ANNOUNCE
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.party-start")));
        Bukkit.broadcastMessage("");

        final long originalTime = Bukkit.getWorlds().get(0).getTime();

        // 1. Set Midnight
        for (World world : Bukkit.getWorlds()) {
            world.setTime(18000);
        }

        // 2. Meteor Shower Task
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= 200) { // 10 seconds
                    endParty(originalTime);
                    this.cancel();
                    return;
                }
                if (ticks % 10 == 0) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        spawnMeteor(p.getLocation());
                    }
                }
                ticks += 10;
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }

    private void endParty(long originalTime) {
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.party-end")));
        Bukkit.getWorlds().get(0).setTime(originalTime);

        // Give Party Rewards
        ConsoleCommandSender console = Bukkit.getConsoleSender();
        List<String> partyCmds = plugin.getConfig().getStringList("rewards.party");

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.5f, 0.8f);
            for (String cmd : partyCmds) {
                Bukkit.dispatchCommand(console, cmd.replace("%player%", p.getName()));
            }
        }
    }

    private void spawnMeteor(Location center) {
        Location spawnLoc = center.clone().add(random.nextInt(10) - 5, 20, random.nextInt(10) - 5);
        Firework fw = center.getWorld().spawn(spawnLoc, Firework.class);
        FireworkMeta meta = fw.getFireworkMeta();

        meta.addEffect(FireworkEffect.builder()
                .with(FireworkEffect.Type.BALL_LARGE)
                .withColor(Color.PURPLE, Color.WHITE)
                .withFade(Color.BLUE)
                .trail(true)
                .build());

        meta.setPower(0);
        fw.setFireworkMeta(meta);
        fw.setVelocity(new org.bukkit.util.Vector(0, -2, 0));
    }

    public int getGlobalVotes() { return globalVotes; }
    public int getVotesNeeded() { return VOTES_FOR_PARTY; }
}