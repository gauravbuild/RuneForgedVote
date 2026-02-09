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

import java.util.*;

public class VoteManager {

    private final RuneForgedVote plugin;
    private int globalVotes;
    private final int VOTES_FOR_PARTY;
    private final Random random = new Random();

    // Faction Tracking
    private final Map<String, Integer> factionVotes = new HashMap<>();
    private final Set<UUID> hasVotedInEvent = new HashSet<>(); // Track players who voted in the mini-event
    private boolean isChatMuted = false;
    private boolean isEventActive = false; // Is the mini-vote active?

    public VoteManager(RuneForgedVote plugin) {
        this.plugin = plugin;
        this.globalVotes = plugin.getConfig().getInt("global-votes", 0);
        this.VOTES_FOR_PARTY = plugin.getConfig().getInt("votes-needed", 50);
        resetFactions();
    }

    public int getTotalVotes(UUID uuid) {
        return plugin.getConfig().getInt("stats." + uuid + ".total-votes", 0);
    }

    private void incrementTotalVotes(UUID uuid) {
        int current = getTotalVotes(uuid);
        plugin.getConfig().set("stats." + uuid + ".total-votes", current + 1);
        plugin.saveConfig();
    }

    public void handleVote(String playerName, String serviceName) {
        // --- UPDATE: Track Total Votes for Leaderboard ---
        // We use OfflinePlayer because the player might not be online when voting
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        incrementTotalVotes(offlinePlayer.getUniqueId());
        // -------------------------------------------------

        globalVotes++;
        plugin.getConfig().set("global-votes", globalVotes);
        plugin.saveConfig();

        // 1. Regular Vote Processing (NuVotifier)
        String guardian = identifyGuardian(serviceName);

        // Only count towards faction if NOT in event mode (Event mode uses manual voting)
        if (!isEventActive) {
            factionVotes.put(guardian, factionVotes.getOrDefault(guardian, 0) + 1);
        }

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

    // --- NEW: Handle Internal Click Vote (The Mini-Game) ---
    public void handleInternalVote(Player player, String faction) {
        if (!isEventActive) {
            player.sendMessage(ChatColor.RED + "The stars are not currently aligning.");
            return;
        }
        if (hasVotedInEvent.contains(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You have already cast your vote for this alignment!");
            return;
        }

        // Register Vote
        hasVotedInEvent.add(player.getUniqueId());
        factionVotes.put(faction, factionVotes.getOrDefault(faction, 0) + 1);

        // Feedback
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        player.sendMessage(ChatColor.GREEN + "You voted for " + capitalize(faction) + "!");

        // Update Chat for Everyone
        broadcastVoteStatus();
    }

    private void startAstralAlignment() {
        globalVotes = 0;
        plugin.getConfig().set("global-votes", 0);
        plugin.saveConfig();
        if (plugin.getBossBarManager() != null) plugin.getBossBarManager().updateBar(0, VOTES_FOR_PARTY);

        isChatMuted = true;
        isEventActive = true;
        hasVotedInEvent.clear();
        resetFactions(); // Start fresh for the mini-game

        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.chat-muted")));

        plugin.getMeteorManager().startSequence();

        new BukkitRunnable() {
            @Override
            public void run() {
                broadcastVoteStatus();
            }
        }.runTaskLater(plugin, 40L);

        new BukkitRunnable() {
            @Override
            public void run() {
                endParty();
            }
        }.runTaskLater(plugin, 320L); // 16 seconds
    }

    private void broadcastVoteStatus() {
        // Clear chat slightly to make it look like a "Live Dashboard"
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(""); // Spacer
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l★ ASTRAL ALIGNMENT VOTING ★"));
            p.sendMessage(ChatColor.GRAY + "Click below to choose the Faction Blessing!");
            p.sendMessage("");

            sendGuardianLine(p, "ignis", "&c🔥 Ignis", ChatColor.RED);
            sendGuardianLine(p, "cryo", "&b❄️ Cryo", ChatColor.AQUA);
            sendGuardianLine(p, "terra", "&a🌿 Terra", ChatColor.GREEN);
            sendGuardianLine(p, "aether", "&d🔮 Aether", ChatColor.LIGHT_PURPLE);
            p.sendMessage("");
        }
    }

    private void sendGuardianLine(Player p, String id, String displayName, ChatColor color) {
        int votes = factionVotes.getOrDefault(id, 0);
        String power = plugin.getConfig().getString("blessings." + id + ".description", "Unknown Power");

        TextComponent message = new TextComponent(ChatColor.translateAlternateColorCodes('&',
                displayName + " &8[" + color + power + "&8] &7- &f" + votes + " Votes "));

        TextComponent button = new TextComponent("[CLICK TO VOTE]");
        button.setColor(net.md_5.bungee.api.ChatColor.GOLD);
        button.setBold(true);
        // Uses a RUN_COMMAND to trigger the internal vote method
        button.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/rfvote internalvote " + id));
        button.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to support " + displayName).create()));

        message.addExtra(button);
        p.spigot().sendMessage(message);
    }

    private void endParty() {
        isChatMuted = false;
        isEventActive = false;
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.chat-unmuted")));

        String winner = getWinningFaction();
        applyBlessing(winner);

        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&6&lTHE STARS HAVE LANDED! &e&lHARVEST THE GEODES QUICKLY!"));
        plugin.getGeodeManager().spawnGeodes(winner);

        // Give Party Rewards (Keys/Money)
        ConsoleCommandSender console = Bukkit.getConsoleSender();
        List<String> partyCmds = plugin.getConfig().getStringList("rewards.party");
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.5f, 0.8f);
            for (String cmd : partyCmds) {
                Bukkit.dispatchCommand(console, cmd.replace("%player%", p.getName()));
            }
        }

        resetFactions();
    }

    private String getWinningFaction() {
        // Collect winners
        List<String> candidates = new ArrayList<>();
        int max = -1;

        // Find max
        for (Map.Entry<String, Integer> entry : factionVotes.entrySet()) {
            if (entry.getValue() > max) {
                max = entry.getValue();
            }
        }

        // Add all with max score to list
        for (Map.Entry<String, Integer> entry : factionVotes.entrySet()) {
            if (entry.getValue() == max) {
                candidates.add(entry.getKey());
            }
        }

        // If no votes (max is 0) or tie, pick random from list
        if (candidates.isEmpty()) {
            // Absolute fallback if map is empty
            List<String> all = Arrays.asList("ignis", "cryo", "terra", "aether");
            return all.get(random.nextInt(all.size()));
        }

        return candidates.get(random.nextInt(candidates.size()));
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