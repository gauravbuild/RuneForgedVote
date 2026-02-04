package me.gauravbuilds.runeforgedvote;

import me.gauravbuilds.runeforgedvote.listeners.ChatListener;
import me.gauravbuilds.runeforgedvote.listeners.GeodeListener;
import me.gauravbuilds.runeforgedvote.listeners.VoteListener;
import me.gauravbuilds.runeforgedvote.listeners.NpcListener;
import me.gauravbuilds.runeforgedvote.managers.BossBarManager;
import me.gauravbuilds.runeforgedvote.managers.CrystalManager;
import me.gauravbuilds.runeforgedvote.managers.GeodeManager;
import me.gauravbuilds.runeforgedvote.managers.MeteorManager;
import me.gauravbuilds.runeforgedvote.managers.PillarManager;
import me.gauravbuilds.runeforgedvote.managers.VoteManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
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
    private GeodeManager geodeManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.pillarManager = new PillarManager(this);
        this.meteorManager = new MeteorManager(this);
        this.bossBarManager = new BossBarManager(this);
        this.geodeManager = new GeodeManager(this);
        this.voteManager = new VoteManager(this);
        this.crystalManager = new CrystalManager(this);

        getServer().getPluginManager().registerEvents(new VoteListener(this), this);
        getServer().getPluginManager().registerEvents(new NpcListener(this), this);
        getServer().getPluginManager().registerEvents(bossBarManager, this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new GeodeListener(this), this);
        getServer().getPluginManager().registerEvents(new me.gauravbuilds.runeforgedvote.listeners.RecipeProtectionListener(), this);

        crystalManager.startAnimation();

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
        if (geodeManager != null) geodeManager.cleanupGeodes();
    }

    public VoteManager getVoteManager() { return voteManager; }
    public CrystalManager getCrystalManager() { return crystalManager; }
    public PillarManager getPillarManager() { return pillarManager; }
    public BossBarManager getBossBarManager() { return bossBarManager; }
    public MeteorManager getMeteorManager() { return meteorManager; }
    public GeodeManager getGeodeManager() { return geodeManager; }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        // HIDDEN COMMAND FOR CHAT CLICK VOTING
        // Anyone can run this, but VoteManager handles checks (if event is active, if voted already)
        if (args.length >= 2 && args[0].equalsIgnoreCase("internalvote")) {
            if (!(sender instanceof Player)) return true;
            String faction = args[1];
            voteManager.handleInternalVote((Player) sender, faction);
            return true;
        }

        // ADMIN COMMANDS BELOW
        if (!sender.hasPermission("rfvote.admin")) {
            sender.sendMessage(ChatColor.RED + "Only the Oracle can use this.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.GOLD + "=== RuneForgedVote Help ===");
            sender.sendMessage(ChatColor.YELLOW + "/rfvote giveitem <player> <item> <amount>");
            sender.sendMessage(ChatColor.YELLOW + "/rfvote setpillar <name>");
            sender.sendMessage(ChatColor.YELLOW + "/rfvote setmeteor <number>");
            sender.sendMessage(ChatColor.YELLOW + "/rfvote setgeode <number>");
            sender.sendMessage(ChatColor.YELLOW + "/rfvote setaltar");
            sender.sendMessage(ChatColor.YELLOW + "/rfvote fakevote <player> <amount>");
            sender.sendMessage(ChatColor.YELLOW + "/rfvote reload");
            return true;
        }

        String sub = args[0].toLowerCase();

        // GIVE ITEM
        if (sub.equals("giveitem")) {
            if (args.length < 3) {
                sender.sendMessage(ChatColor.RED + "Usage: /rfvote giveitem <player> <ignis/cryo/terra/aether> <amount>");
                return true;
            }
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found.");
                return true;
            }
            String itemType = args[2];
            int amount = 1;
            if (args.length > 3) {
                try { amount = Integer.parseInt(args[3]); } catch (Exception ignored) {}
            }

            ItemStack item = GeodeListener.getShardByName(itemType);
            item.setAmount(amount);
            target.getInventory().addItem(item);
            sender.sendMessage(ChatColor.GREEN + "Gave " + amount + " " + itemType + " to " + target.getName());
            return true;
        }

        // OTHER COMMANDS (Unchanged)
        if (sub.equals("reload")) {
            reloadConfig();
            meteorManager.loadLocations();
            geodeManager.loadGeodeLocations();
            sender.sendMessage(ChatColor.GREEN + "Config reloaded.");
            return true;
        }
        if (sub.equals("setaltar")) {
            if (!(sender instanceof Player)) return true;
            saveLocation("locations.altar", ((Player) sender).getLocation());
            sender.sendMessage(ChatColor.GREEN + "Altar location set!");
            return true;
        }
        if (sub.equals("setmeteor")) {
            if (!(sender instanceof Player)) return true;
            if (args.length < 2) return true;
            try {
                int id = Integer.parseInt(args[1]);
                saveLocation("locations.meteor-" + id, ((Player) sender).getLocation());
                sender.sendMessage(ChatColor.GREEN + "Meteor Visual Zone #" + id + " set!");
                meteorManager.loadLocations();
            } catch (Exception e) {}
            return true;
        }
        if (sub.equals("setgeode")) {
            if (!(sender instanceof Player)) return true;
            if (args.length < 2) return true;
            try {
                int id = Integer.parseInt(args[1]);
                saveLocation("locations.geode-" + id, ((Player) sender).getLocation());
                sender.sendMessage(ChatColor.GREEN + "Geode Mining Spot #" + id + " set!");
                geodeManager.loadGeodeLocations();
            } catch (Exception e) {}
            return true;
        }
        if (sub.equals("setpillar")) {
            if (!(sender instanceof Player)) return true;
            if (args.length < 2) return true;
            String name = args[1].toLowerCase();
            saveLocation("locations.pillar-" + name, ((Player) sender).getLocation());
            sender.sendMessage(ChatColor.GREEN + "Pillar '" + name + "' set!");
            return true;
        }
        if (sub.equals("interact")) {
            if (args.length < 3) return true;
            Player target = Bukkit.getPlayer(args[1]);
            String guardianName = args[2].toLowerCase();
            if (target != null) handleNpcInteraction(target, guardianName);
            return true;
        }
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

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission("rfvote.admin")) return Collections.emptyList();
        List<String> suggestions = new ArrayList<>();
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            suggestions.addAll(Arrays.asList("interact", "giveitem", "setpillar", "setaltar", "setmeteor", "setgeode", "reload", "fakevote"));
            StringUtil.copyPartialMatches(args[0], suggestions, completions);
            Collections.sort(completions);
            return completions;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("giveitem")) {
            if (args.length == 2) {
                return null; // Player list
            }
            if (args.length == 3) {
                suggestions.addAll(Arrays.asList("ignis", "cryo", "terra", "aether"));
                StringUtil.copyPartialMatches(args[2], suggestions, completions);
                Collections.sort(completions);
                return completions;
            }
            if (args.length == 4) {
                suggestions.addAll(Arrays.asList("1", "16", "64"));
                return suggestions;
            }
        }

        // ... existing tab logic for setpillar/etc ...
        if (args.length == 2) {
            if (sub.equals("setpillar")) suggestions.addAll(Arrays.asList("ignis", "cryo", "terra", "aether"));
            else if (sub.equals("setmeteor") || sub.equals("setgeode")) {
                for (int i = 1; i <= 20; i++) suggestions.add(String.valueOf(i));
            }
            StringUtil.copyPartialMatches(args[1], suggestions, completions);
            Collections.sort(completions);
            return completions;
        }

        return Collections.emptyList();
    }

    // Helper Methods (NpcInteraction, SaveLocation) remain same
    private void handleNpcInteraction(Player player, String guardianName) {
        // ... Same as before ...
        // Just copy the method from previous file if you are replacing whole file, or keep it if partial
        // For completeness I assume you know to keep the helper methods
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