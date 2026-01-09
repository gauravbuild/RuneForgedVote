package me.gauravbuilds.runeforgedvote;

import me.gauravbuilds.runeforgedvote.listeners.VoteListener;
import me.gauravbuilds.runeforgedvote.listeners.NpcListener;
import me.gauravbuilds.runeforgedvote.managers.CrystalManager;
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
import org.bukkit.command.TabCompleter;
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

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // 1. Initialize Managers
        this.pillarManager = new PillarManager(this);
        this.voteManager = new VoteManager(this);
        this.crystalManager = new CrystalManager(this);

        // 2. Register Events
        getServer().getPluginManager().registerEvents(new VoteListener(this), this);
        getServer().getPluginManager().registerEvents(new NpcListener(this), this);

        // 3. Start Visuals
        crystalManager.startAnimation();

        // 4. Register Commands & Tab Completer
        // We set the tab completer to "this" because this class implements TabCompleter below
        if (getCommand("rfvote") != null) {
            getCommand("rfvote").setExecutor(this);
            getCommand("rfvote").setTabCompleter(this);
        }

        getLogger().info("The Oracle is listening for stars...");
    }

    @Override
    public void onDisable() {
        if (crystalManager != null) {
            crystalManager.removeCrystal();
        }
    }

    public VoteManager getVoteManager() { return voteManager; }
    public CrystalManager getCrystalManager() { return crystalManager; }
    public PillarManager getPillarManager() { return pillarManager; }

    // ==========================================
    //              COMMAND LOGIC
    // ==========================================

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!command.getName().equalsIgnoreCase("rfvote")) return true;

        if (!sender.hasPermission("rfvote.admin")) {
            sender.sendMessage(ChatColor.RED + "Only the Oracle can use this.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.GOLD + "=== RuneForgedVote Help ===");
            sender.sendMessage(ChatColor.YELLOW + "/rfvote setpillar <name> " + ChatColor.GRAY + "- Set pillar location");
            sender.sendMessage(ChatColor.YELLOW + "/rfvote setaltar " + ChatColor.GRAY + "- Set crystal location");
            sender.sendMessage(ChatColor.YELLOW + "/rfvote fakevote <player> <amount> " + ChatColor.GRAY + "- Test votes");
            sender.sendMessage(ChatColor.YELLOW + "/rfvote reload " + ChatColor.GRAY + "- Reload config");
            return true;
        }

        String sub = args[0].toLowerCase();

        // 1. RELOAD
        if (sub.equals("reload")) {
            reloadConfig();
            sender.sendMessage(ChatColor.GREEN + "Config reloaded.");
            return true;
        }

        // 2. SET ALTAR
        if (sub.equals("setaltar")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Players only.");
                return true;
            }
            saveLocation("locations.altar", ((Player) sender).getLocation());
            sender.sendMessage(ChatColor.GREEN + "Altar location set!");
            return true;
        }

        // 3. SET PILLAR: /rfvote setpillar <name>
        if (sub.equals("setpillar")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Players only.");
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Usage: /rfvote setpillar <ignis/cryo/terra/aether>");
                return true;
            }
            String name = args[1].toLowerCase();
            saveLocation("locations.pillar-" + name, ((Player) sender).getLocation());
            sender.sendMessage(ChatColor.GREEN + "Pillar '" + name + "' set!");
            return true;
        }

        // 4. INTERACT: /rfvote interact <player> <guardian_name>
        if (sub.equals("interact")) {
            if (args.length < 3) {
                sender.sendMessage(ChatColor.RED + "Usage: /rfvote interact <player> <guardian>");
                return true;
            }
            Player target = Bukkit.getPlayer(args[1]);
            String guardianName = args[2].toLowerCase();

            if (target != null && target.isOnline()) {
                handleNpcInteraction(target, guardianName);
            } else {
                sender.sendMessage(ChatColor.RED + "Player not found.");
            }
            return true;
        }

        // 5. FAKEVOTE: /rfvote fakevote <player> <amount>
        if (sub.equals("fakevote")) {
            String pName = (args.length > 1) ? args[1] : "TestPlayer";
            int amount = 1;

            if (args.length > 2) {
                try {
                    amount = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Invalid amount. Simulating 1 vote.");
                }
            }

            sender.sendMessage(ChatColor.YELLOW + "Simulating " + amount + " vote(s) for " + pName + "...");

            // Execute the loop
            for (int i = 0; i < amount; i++) {
                voteManager.handleVote(pName, "FakeVote");
            }

            sender.sendMessage(ChatColor.GREEN + "Simulation complete. Total Votes: " + voteManager.getGlobalVotes());
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Unknown command.");
        return true;
    }

    // ==========================================
    //            TAB COMPLETION LOGIC
    // ==========================================

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission("rfvote.admin")) return Collections.emptyList();

        List<String> completions = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();

        // Argument 1: Subcommands
        if (args.length == 1) {
            suggestions.add("interact");
            suggestions.add("setpillar");
            suggestions.add("setaltar");
            suggestions.add("reload");
            suggestions.add("fakevote");
            StringUtil.copyPartialMatches(args[0], suggestions, completions);
            Collections.sort(completions);
            return completions;
        }

        String sub = args[0].toLowerCase();

        // Argument 2:
        // setpillar -> <guardian>
        // interact -> <player>
        // fakevote -> <player>
        if (args.length == 2) {
            if (sub.equals("setpillar")) {
                suggestions.addAll(Arrays.asList("ignis", "cryo", "terra", "aether"));
            } else if (sub.equals("interact") || sub.equals("fakevote")) {
                return null; // Return null to show online players automatically
            }
            StringUtil.copyPartialMatches(args[1], suggestions, completions);
            Collections.sort(completions);
            return completions;
        }

        // Argument 3:
        // interact -> <guardian>
        // fakevote -> <amount>
        if (args.length == 3) {
            if (sub.equals("interact")) {
                suggestions.addAll(Arrays.asList("ignis", "cryo", "terra", "aether"));
            } else if (sub.equals("fakevote")) {
                suggestions.addAll(Arrays.asList("1", "5", "10", "32", "50", "64"));
            }
            StringUtil.copyPartialMatches(args[2], suggestions, completions);
            Collections.sort(completions);
            return completions;
        }

        return Collections.emptyList();
    }

    // ==========================================
    //              HELPER METHODS
    // ==========================================

    private void handleNpcInteraction(Player player, String guardianName) {
        boolean demoMode = getConfig().getBoolean("demo-mode", true);

        if (demoMode) {
            // Check Cooldown
            long lastVote = getConfig().getLong("data." + player.getUniqueId() + "." + guardianName, 0);
            long currentTime = System.currentTimeMillis();
            long cooldownTime = TimeUnit.HOURS.toMillis(24);

            if (currentTime - lastVote < cooldownTime) {
                long remaining = cooldownTime - (currentTime - lastVote);
                long hours = TimeUnit.MILLISECONDS.toHours(remaining);
                long minutes = TimeUnit.MILLISECONDS.toMinutes(remaining) % 60;

                String msg = getConfig().getString("messages.cooldown", "&cWait %time%.")
                        .replace("%time%", String.format("%dh %dm", hours, minutes));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
                player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1f, 1f);
                return;
            }

            // Capitalize first letter
            String displayName = guardianName.substring(0, 1).toUpperCase() + guardianName.substring(1);
            getVoteManager().handleVote(player.getName(), displayName);

            // Save Cooldown
            getConfig().set("data." + player.getUniqueId() + "." + guardianName, currentTime);
            saveConfig();

        } else {
            // Send Link
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