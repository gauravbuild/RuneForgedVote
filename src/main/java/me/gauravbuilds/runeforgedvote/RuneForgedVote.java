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
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class RuneForgedVote extends JavaPlugin {

    private VoteManager voteManager;
    private CrystalManager crystalManager;
    private PillarManager pillarManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.pillarManager = new PillarManager(this);
        this.voteManager = new VoteManager(this);
        this.crystalManager = new CrystalManager(this);

        // Register Listeners
        getServer().getPluginManager().registerEvents(new VoteListener(this), this);
        getServer().getPluginManager().registerEvents(new NpcListener(this), this); // Assuming you added this earlier

        crystalManager.startAnimation();
        getLogger().info("The Oracle is listening for stars...");
    }

    @Override
    public void onDisable() {
        if (crystalManager != null) crystalManager.removeCrystal();
    }

    public VoteManager getVoteManager() { return voteManager; }
    public CrystalManager getCrystalManager() { return crystalManager; }
    public PillarManager getPillarManager() { return pillarManager; }

    // --- COMMANDS ---
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!command.getName().equalsIgnoreCase("rfvote")) return true;

        if (!sender.hasPermission("rfvote.admin")) {
            sender.sendMessage(ChatColor.RED + "Only the Oracle can use this.");
            return true;
        }

        if (args.length == 0) return false;

        // 1. RELOAD
        if (args[0].equalsIgnoreCase("reload")) {
            reloadConfig();
            sender.sendMessage(ChatColor.GREEN + "Config reloaded.");
            return true;
        }

        // 2. SET ALTAR
        if (args[0].equalsIgnoreCase("setaltar")) {
            if (!(sender instanceof Player)) return true;
            saveLocation("locations.altar", ((Player) sender).getLocation());
            sender.sendMessage(ChatColor.GREEN + "Altar location set!");
            return true;
        }

        // 3. SET PILLAR: /rfvote setpillar <ignis/cryo/terra/aether>
        if (args[0].equalsIgnoreCase("setpillar") && args.length > 1) {
            if (!(sender instanceof Player)) return true;
            String name = args[1].toLowerCase();
            saveLocation("locations.pillar-" + name, ((Player) sender).getLocation());
            sender.sendMessage(ChatColor.GREEN + "Pillar '" + name + "' set!");
            return true;
        }

        // 4. INTERACT: /rfvote interact <player> <guardian_name>
        if (args[0].equalsIgnoreCase("interact") && args.length > 2) {
            Player target = Bukkit.getPlayer(args[1]);
            String guardianName = args[2].toLowerCase();

            if (target != null && target.isOnline()) {
                handleNpcInteraction(target, guardianName);
            } else {
                sender.sendMessage(ChatColor.RED + "Player not found.");
            }
            return true;
        }

        // 5. FAKE VOTE: /rfvote fakevote <player> [amount]
        if (args[0].equalsIgnoreCase("fakevote")) {
            String pName = (args.length > 1) ? args[1] : "TestPlayer";
            int amount = 1;

            // Check for amount argument
            if (args.length > 2) {
                try {
                    amount = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Invalid amount. Using 1.");
                }
            }

            sender.sendMessage(ChatColor.YELLOW + "Simulating " + amount + " vote(s) for " + pName + "...");

            // Loop to trigger multiple votes
            for (int i = 0; i < amount; i++) {
                // Slight delay logic isn't strictly needed for testing,
                // but running it directly updates the counter instantly.
                voteManager.handleVote(pName, "FakeVote");
            }

            sender.sendMessage(ChatColor.GREEN + "Done! Global votes are now: " + voteManager.getGlobalVotes());
            return true;
        }

        return false;
    }

    // --- TAB COMPLETER ---
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission("rfvote.admin")) return Collections.emptyList();

        // Arg 0: Subcommands
        if (args.length == 1) {
            return Arrays.asList("interact", "setpillar", "setaltar", "reload", "fakevote");
        }

        // /rfvote setpillar <guardian>
        if (args[0].equalsIgnoreCase("setpillar")) {
            if (args.length == 2) return Arrays.asList("ignis", "cryo", "terra", "aether");
        }

        // /rfvote interact <player> <guardian>
        if (args[0].equalsIgnoreCase("interact")) {
            if (args.length == 2) return null; // Player names
            if (args.length == 3) return Arrays.asList("ignis", "cryo", "terra", "aether");
        }

        // /rfvote fakevote <player> <amount>
        if (args[0].equalsIgnoreCase("fakevote")) {
            if (args.length == 2) return null; // Player names
            if (args.length == 3) return Arrays.asList("1", "5", "10", "50");
        }

        return Collections.emptyList();
    }

    // --- NPC LOGIC ---
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

            // TRIGGER MAGIC
            // Capitalize first letter for visual niceness (ignis -> Ignis)
            String displayName = guardianName.substring(0, 1).toUpperCase() + guardianName.substring(1);
            getVoteManager().handleVote(player.getName(), displayName);

            // Save Cooldown
            getConfig().set("data." + player.getUniqueId() + "." + guardianName, currentTime);
            saveConfig();

        } else {
            // Live Link Logic
            // Map the name to ID (or just use specific keys in config if you want)
            // Defaulting to link '1' for now as per previous simple setup,
            // or you can add map logic here.
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