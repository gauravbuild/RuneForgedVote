package me.gauravbuilds.runeforgedvote;

import me.gauravbuilds.runeforgedvote.listeners.VoteListener;
import me.gauravbuilds.runeforgedvote.managers.CrystalManager;
import me.gauravbuilds.runeforgedvote.managers.PillarManager;
import me.gauravbuilds.runeforgedvote.managers.VoteManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class RuneForgedVote extends JavaPlugin {

    private VoteManager voteManager;
    private CrystalManager crystalManager;
    private PillarManager pillarManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // 1. Initialize Managers
        this.pillarManager = new PillarManager(this);
        this.voteManager = new VoteManager(this);     // Logic
        this.crystalManager = new CrystalManager(this); // Visuals

        // 2. Register Events
        getServer().getPluginManager().registerEvents(new VoteListener(this), this);

        // 3. Start Tasks
        crystalManager.startAnimation(); // Start the spinning

        getLogger().info("The Oracle is listening for stars...");
    }

    @Override
    public void onDisable() {
        if (crystalManager != null) {
            crystalManager.removeCrystal(); // Clean up armor stand on reload
        }
    }

    public VoteManager getVoteManager() { return voteManager; }
    public CrystalManager getCrystalManager() { return crystalManager; }
    public PillarManager getPillarManager() { return pillarManager; }

    // --- COMMANDS ---
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("rfvote")) return true;
        if (!sender.hasPermission("rfvote.admin")) {
            sender.sendMessage(ChatColor.RED + "Only the Oracle can use this.");
            return true;
        }

        if (args.length == 0) return false;

        if (args[0].equalsIgnoreCase("reload")) {
            reloadConfig();
            sender.sendMessage(ChatColor.GREEN + "Config reloaded.");
            return true;
        }

        // /rfvote fakevote <player>
        if (args[0].equalsIgnoreCase("fakevote")) {
            String pName = (args.length > 1) ? args[1] : "TestPlayer";
            voteManager.handleVote(pName, "TestService");
            sender.sendMessage(ChatColor.GREEN + "Fake vote triggered.");
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("Players only for setup commands.");
            return true;
        }
        Player player = (Player) sender;
        Location loc = player.getLocation();

        // /rfvote setaltar
        if (args[0].equalsIgnoreCase("setaltar")) {
            saveLocation("locations.altar", loc);
            player.sendMessage(ChatColor.GREEN + "Altar location set! (Restart/Reload to spawn crystal)");
            return true;
        }

        // /rfvote setpillar <1-4>
        if (args[0].equalsIgnoreCase("setpillar") && args.length > 1) {
            String id = args[1];
            if (!id.matches("[1-4]")) {
                player.sendMessage(ChatColor.RED + "Use 1, 2, 3, or 4.");
                return true;
            }
            saveLocation("locations.pillar-" + id, loc);
            player.sendMessage(ChatColor.GREEN + "Pillar " + id + " location set!");
            return true;
        }

        return false;
    }

    private void saveLocation(String path, Location loc) {
        getConfig().set(path + ".world", loc.getWorld().getName());
        getConfig().set(path + ".x", loc.getX());
        getConfig().set(path + ".y", loc.getY());
        getConfig().set(path + ".z", loc.getZ());
        saveConfig();
    }
}