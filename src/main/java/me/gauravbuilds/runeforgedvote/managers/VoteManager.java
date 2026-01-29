package me.gauravbuilds.runeforgedvote.managers;

import me.gauravbuilds.runeforgedvote.RuneForgedVote;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class VoteManager {

    private final RuneForgedVote plugin;
    private int globalVotes;
    private final int VOTES_FOR_PARTY;
    private final Random random = new Random();

    // Faction Tracking
    private final Map<String, Integer> factionVotes = new HashMap<>();
    private boolean isChatMuted = false;

    public VoteManager(RuneForgedVote plugin) {
        this.plugin = plugin;
        this.globalVotes = plugin.getConfig().getInt("global-votes", 0);
        this.VOTES_FOR_PARTY = plugin.getConfig().getInt("votes-needed", 50);
        resetFactions();
    }

    public void handleVote(String playerName, String serviceName) {
        globalVotes++;
        plugin.getConfig().set("global-votes", globalVotes);
        plugin.saveConfig();

        String guardian = identifyGuardian(serviceName);
        factionVotes.put(guardian, factionVotes.getOrDefault(guardian, 0) + 1);

        if (plugin.getBossBarManager() != null) {
            plugin.getBossBarManager().updateBar(globalVotes, VOTES_FOR_PARTY);
        }

        Player player = Bukkit.getPlayer(playerName);
        if (player != null) {
            giveRewards(player);
            plugin.getPillarManager().ignitePillar(guardian);
            player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1f, 1f);
            player.sendTitle(ChatColor.GOLD + "★ STAR IGNITED ★", ChatColor.GRAY + "You supported " + capitalize(guardian), 10, 40, 10);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.vote-received")));
        }

        String broadcast = plugin.getConfig().getString("messages.broadcast")
                .replace("%player%", playerName)
                .replace("%current%", String.valueOf(globalVotes))
                .replace("%max%", String.valueOf(VOTES_FOR_PARTY));
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', broadcast));

        if (globalVotes >= VOTES_FOR_PARTY) {
            startAstralAlignment();
        }
    }

    private void startAstralAlignment() {
        globalVotes = 0;
        plugin.getConfig().set("global-votes", 0);
        plugin.saveConfig();
        if (plugin.getBossBarManager() != null) plugin.getBossBarManager().updateBar(0, VOTES_FOR_PARTY);

        isChatMuted = true;
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.chat-muted")));

        plugin.getMeteorManager().startSequence();

        new BukkitRunnable() {
            @Override
            public void run() {
                sendVoteStatusMenu();
            }
        }.runTaskLater(plugin, 40L);

        new BukkitRunnable() {
            @Override
            public void run() {
                endParty();
            }
        }.runTaskLater(plugin, 320L);
    }

    private void sendVoteStatusMenu() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage("");
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l★ THE STARS ARE ALIGNING! ★"));
            p.sendMessage(ChatColor.GRAY + "The guardians are fighting for dominance...");
            p.sendMessage("");
            sendGuardianLine(p, "ignis", "&c🔥 Ignis", ChatColor.RED);
            sendGuardianLine(p, "cryo", "&b❄️ Cryo", ChatColor.AQUA);
            sendGuardianLine(p, "terra", "&a🌿 Terra", ChatColor.GREEN);
            sendGuardianLine(p, "aether", "&d🔮 Aether", ChatColor.LIGHT_PURPLE);
            p.sendMessage("");
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e&lCLICK TO VOTE &7to change the fate!"));
            p.sendMessage("");
        }
    }

    private void sendGuardianLine(Player p, String id, String displayName, ChatColor color) {
        int votes = factionVotes.getOrDefault(id, 0);
        String power = plugin.getConfig().getString("blessings." + id + ".description", "Unknown Power");
        String link = plugin.getConfig().getString("links." + id, "https://example.com");

        TextComponent message = new TextComponent(ChatColor.translateAlternateColorCodes('&',
                displayName + " &8[" + color + power + "&8] &7- &f" + votes + " Votes "));

        TextComponent button = new TextComponent("[VOTE]");
        button.setColor(net.md_5.bungee.api.ChatColor.GOLD);
        button.setBold(true);
        button.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link));
        button.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to vote for " + displayName).create()));

        message.addExtra(button);
        p.spigot().sendMessage(message);
    }

    private void endParty() {
        isChatMuted = false;
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.chat-unmuted")));

        String winner = getWinningFaction();
        applyBlessing(winner);

        // --- NEW GEODE LOGIC ---
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&6&lTHE STARS HAVE LANDED! &e&lHARVEST THE GEODES QUICKLY!"));
        plugin.getGeodeManager().spawnGeodes(winner);

        resetFactions();
    }

    private String getWinningFaction() {
        String winner = "aether";
        int max = -1;
        for (Map.Entry<String, Integer> entry : factionVotes.entrySet()) {
            if (entry.getValue() > max) {
                max = entry.getValue();
                winner = entry.getKey();
            }
        }
        return winner;
    }

    private void applyBlessing(String faction) {
        String path = "blessings." + faction;
        if (!plugin.getConfig().contains(path)) return;

        String effectName = plugin.getConfig().getString(path + ".effect");
        int amp = plugin.getConfig().getInt(path + ".amplifier");
        int dur = plugin.getConfig().getInt(path + ".duration") * 20;
        String msg = plugin.getConfig().getString(path + ".message");

        PotionEffectType type = PotionEffectType.getByName(effectName);
        if (type != null) {
            PotionEffect effect = new PotionEffect(type, dur, amp);
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.addPotionEffect(effect);
            }
        }
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', msg));
    }

    private void giveRewards(Player player) {
        ConsoleCommandSender console = Bukkit.getConsoleSender();
        List<String> commands = plugin.getConfig().getStringList("rewards.per-vote");
        for (String cmd : commands) Bukkit.dispatchCommand(console, cmd.replace("%player%", player.getName()));

        if (random.nextInt(100) < 5) {
            List<String> luckyCmds = plugin.getConfig().getStringList("rewards.lucky-vote");
            for (String cmd : luckyCmds) Bukkit.dispatchCommand(console, cmd.replace("%player%", player.getName()));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.lucky-trigger")));
        }
    }

    private String identifyGuardian(String input) {
        input = input.toLowerCase();
        if (input.contains("ignis")) return "ignis";
        if (input.contains("cryo")) return "cryo";
        if (input.contains("terra")) return "terra";
        if (input.contains("aether")) return "aether";
        return "aether";
    }

    private void resetFactions() {
        factionVotes.put("ignis", 0);
        factionVotes.put("cryo", 0);
        factionVotes.put("terra", 0);
        factionVotes.put("aether", 0);
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public boolean isChatMuted() { return isChatMuted; }
    public int getGlobalVotes() { return globalVotes; }
    public int getVotesNeeded() { return VOTES_FOR_PARTY; }
}