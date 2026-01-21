package me.gauravbuilds.runeforgedvote;

import me.gauravbuilds.runeforgedvote.listeners.ChatListener;
import me.gauravbuilds.runeforgedvote.listeners.VoteListener;
import me.gauravbuilds.runeforgedvote.listeners.NpcListener;
import me.gauravbuilds.runeforgedvote.managers.BossBarManager;
import me.gauravbuilds.runeforgedvote.managers.CrystalManager;
import me.gauravbuilds.runeforgedvote.managers.MeteorManager;
import me.gauravbuilds.runeforgedvote.managers.PillarManager;
import me.gauravbuilds.runeforgedvote.managers.VoteManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RuneForgedVote extends JavaPlugin {

    private VoteManager voteManager;
    private CrystalManager crystalManager;
    private PillarManager pillarManager;
    private BossBarManager bossBarManager;
    private MeteorManager meteorManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // 1. Initialize Managers
        this.pillarManager = new PillarManager(this);
        this.meteorManager = new MeteorManager(this);
        this.bossBarManager = new BossBarManager(this);
        this.voteManager = new VoteManager(this);
        this.crystalManager = new CrystalManager(this);

        // 2. Register Events
        getServer().getPluginManager().registerEvents(new VoteListener(this), this);
        getServer().getPluginManager().registerEvents(new NpcListener(this), this);
        getServer().getPluginManager().registerEvents(bossBarManager, this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this); // <--- REGISTER CHAT LISTENER

        // 3. Start Visuals
        crystalManager.startAnimation();

        // 4. Register Commands
        if (getCommand("rfvote") != null) {
            getCommand("rfvote").setExecutor(this);
            getCommand("rfvote").setTabCompleter(this);
        }

        getLogger().info("The Oracle is listening for stars...");
    }

    @Override
    public void onDisable() {
        if (crystalManager != null) crystalManager.removeCrystal();
        if (bossBarManager != null) bossBarManager.removeBar();
    }

    public VoteManager getVoteManager() { return voteManager; }
    public CrystalManager getCrystalManager() { return crystalManager; }
    public PillarManager getPillarManager() { return pillarManager; }
    public BossBarManager getBossBarManager() { return bossBarManager; }
    public MeteorManager getMeteorManager() { return meteorManager; }

    // ... (Your existing Command Logic / onCommand & onTabComplete remains exactly the same) ...
    // ... (Paste the onCommand logic from previous response if needed, otherwise it's fine) ...

    // ==========================================
    //              COMMAND LOGIC
    // ==========================================

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("rfvote.admin")) {
            sender.sendMessage(ChatColor.RED + "Only the Oracle can use this.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.GOLD + "=== RuneForgedVote Help ===");
            sender.sendMessage(ChatColor.YELLOW + "/rfvote setpillar <name>");
            sender.sendMessage(ChatColor.YELLOW + "/rfvote setmeteor <1-10>");
            sender.sendMessage(ChatColor.YELLOW + "/rfvote setaltar");
            sender.sendMessage(ChatColor.YELLOW + "/rfvote fakevote <player> <amount>");
            sender.sendMessage(ChatColor.YELLOW + "/rfvote reload");
            return true;
        }

        String sub = args[0].toLowerCase();

        // RELOAD
        if (sub.equals("reload")) {
            reloadConfig();
            meteorManager.loadLocations();
            sender.sendMessage(ChatColor.GREEN + "Config reloaded.");
            return true;
        }

        // SET ALTAR
        if (sub.equals("setaltar")) {
            if (!(sender instanceof Player)) return true;
            saveLocation("locations.altar", ((Player) sender).getLocation());
            sender.sendMessage(ChatColor.GREEN + "Altar location set!");
            return true;
        }

        // SET METEOR
        if (sub.equals("setmeteor")) {
            if (!(sender instanceof Player)) return true;
            if (args.length < 2) return true;
            saveLocation("locations.meteor-" + args[1], ((Player) sender).getLocation());
            sender.sendMessage(ChatColor.GREEN + "Meteor Impact Zone #" + args[1] + " set!");
            meteorManager.loadLocations();
            return true;
        }

        // SET PILLAR
        if (sub.equals("setpillar")) {
            if (!(sender instanceof Player)) return true;
            if (args.length < 2) return true;
            String name = args[1].toLowerCase();
            saveLocation("locations.pillar-" + name, ((Player) sender).getLocation());
            sender.sendMessage(ChatColor.GREEN + "Pillar '" + name + "' set!");
            return true;
        }

        // INTERACT
        if (sub.equals("interact")) {
            if (args.length < 3) return true;
            Player target = Bukkit.getPlayer(args[1]);
            String guardianName = args[2].toLowerCase();
            if (target != null) handleNpcInteraction(target, guardianName);
            return true;
        }

        // FAKEVOTE
        if (sub.equals("fakevote")) {
            String pName = (args.length > 1) ? args[1] : "TestPlayer";
            int amount = 1;
            if (args.length > 2) {
                try { amount = Integer.parseInt(args[2]); } catch (Exception ignored) {}
            }
            sender.sendMessage(ChatColor.YELLOW + "Simulating " + amount + " votes...");
            for (int i = 0; i < amount; i++) voteManager.handleVote(pName, "FakeVote");
            return true;
        }

        return true;
    }

    // ==========================================
    //            TAB COMPLETION LOGIC
    // ==========================================

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission("rfvote.admin")) return Collections.emptyList();
        List<String> suggestions = new ArrayList<>();
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            suggestions.addAll(Arrays.asList("interact", "setpillar", "setaltar", "setmeteor", "reload", "fakevote"));
            StringUtil.copyPartialMatches(args[0], suggestions, completions);
            Collections.sort(completions);
            return completions;
        }

        String sub = args[0].toLowerCase();
        if (args.length == 2) {
            if (sub.equals("setpillar")) suggestions.addAll(Arrays.asList("ignis", "cryo", "terra", "aether"));
            else if (sub.equals("setmeteor")) suggestions.addAll(Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10"));
            else if (sub.equals("interact") || sub.equals("fakevote")) return null;
            StringUtil.copyPartialMatches(args[1], suggestions, completions);
            Collections.sort(completions);
            return completions;
        }

        if (args.length == 3) {
            if (sub.equals("interact")) suggestions.addAll(Arrays.asList("ignis", "cryo", "terra", "aether"));
            else if (sub.equals("fakevote")) suggestions.addAll(Arrays.asList("1", "2", "4", "8", "16", "32", "64"));
            StringUtil.copyPartialMatches(args[2], suggestions, completions);
            Collections.sort(completions);
            return completions;
        }

        return Collections.emptyList();
    }

    // Helper Methods
    private void handleNpcInteraction(Player player, String guardianName) {
        boolean demoMode = getConfig().getBoolean("demo-mode", true);
        if (demoMode) {
            long lastVote = getConfig().getLong("data." + player.getUniqueId() + "." + guardianName, 0);
            long currentTime = System.currentTimeMillis();
            long cooldownTime = TimeUnit.HOURS.toMillis(24);
            if (currentTime - lastVote < cooldownTime) {
                long remaining = cooldownTime - (currentTime - lastVote);
                long hours = TimeUnit.MILLISECONDS.toHours(remaining);
                long minutes = TimeUnit.MILLISECONDS.toMinutes(remaining) % 60;
                String msg = getConfig().getString("messages.cooldown", "&cWait %time%")
                        .replace("%time%", String.format("%dh %dm", hours, minutes));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
                player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1f, 1f);
                return;
            }
            String displayName = guardianName.substring(0, 1).toUpperCase() + guardianName.substring(1);
            getVoteManager().handleVote(player.getName(), displayName);
            getConfig().set("data." + player.getUniqueId() + "." + guardianName, currentTime);
            saveConfig();
        } else {
            String link = getConfig().getString("links.1", "https://example.com");
            String msg = getConfig().getString("messages.link-sent", "&eGo vote: %link%").replace("%link%", link);
            TextComponent component = new TextComponent(ChatColor.translateAlternateColorCodes('&', msg));
            component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link));
            player.spigot().sendMessage(component);
        }
    }

    private void saveLocation(String path, Location loc) {
        getConfig().set(path + ".world", loc.getWorld().getName());
        getConfig().set(path + ".x", loc.getX());
        getConfig().set(path + ".y", loc.getY());
        getConfig().set(path + ".z", loc.getZ());
        saveConfig();
    }
}