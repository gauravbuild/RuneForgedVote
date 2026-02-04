package me.gauravbuilds.runeforgedvote.managers;

import me.gauravbuilds.runeforgedvote.RuneForgedVote;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class GeodeManager {

    private final RuneForgedVote plugin;
    // We use String keys "world,x,y,z" for 100% accurate tracking to avoid decimal errors
    private final Map<String, Material> originalBlocks = new HashMap<>();
    private final Set<String> activeCores = new HashSet<>();
    private final List<Location> geodeSpawnPoints = new ArrayList<>();

    public GeodeManager(RuneForgedVote plugin) {
        this.plugin = plugin;
        loadGeodeLocations();
    }

    public void loadGeodeLocations() {
        geodeSpawnPoints.clear();
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection section = config.getConfigurationSection("locations");

        if (section != null) {
            for (String key : section.getKeys(false)) {
                if (key.startsWith("geode-")) {
                    String path = "locations." + key;
                    if (config.contains(path + ".world")) {
                        World w = Bukkit.getWorld(config.getString(path + ".world"));
                        double x = config.getDouble(path + ".x");
                        double y = config.getDouble(path + ".y");
                        double z = config.getDouble(path + ".z");
                        if (w != null) {
                            // We immediately align to block coordinates
                            geodeSpawnPoints.add(new Location(w, (int)x, (int)y, (int)z));
                        }
                    }
                }
            }
        }
        plugin.getLogger().info("Loaded " + geodeSpawnPoints.size() + " Geode Mining Spots.");
    }

    public void spawnGeodes(String winner) {
        if (geodeSpawnPoints.isEmpty()) {
            return;
        }

        Material coreType;
        Particle particle;

        switch (winner.toLowerCase()) {
            case "ignis": // Fire
                coreType = Material.MAGMA_BLOCK;
                particle = Particle.FLAME;
                break;
            case "cryo": // Ice
                coreType = Material.BLUE_ICE;
                particle = Particle.SNOWFLAKE;
                break;
            case "terra": // Nature
                coreType = Material.EMERALD_ORE;
                particle = Particle.HAPPY_VILLAGER;
                break;
            default: // Aether
                coreType = Material.AMETHYST_BLOCK;
                particle = Particle.DRAGON_BREATH;
                break;
        }

        for (Location loc : geodeSpawnPoints) {
            // Spawn 1 block UP so we don't destroy the floor
            Location spawnLoc = loc.clone().add(0, 1, 0);
            generateSingleGeode(spawnLoc, coreType);
        }

        // Decay Timer (30s)
        new BukkitRunnable() {
            int seconds = 30;
            @Override
            public void run() {
                if (seconds <= 0 || activeCores.isEmpty()) {
                    cleanupGeodes();
                    this.cancel();
                    return;
                }

                // Visuals on active blocks
                for (String locKey : activeCores) {
                    Location loc = parseKey(locKey);
                    if (loc != null) {
                        loc.getWorld().spawnParticle(particle, loc.clone().add(0.5, 0.5, 0.5), 5, 0.5, 0.5, 0.5, 0.1);
                        if (seconds <= 5) {
                            loc.getWorld().spawnParticle(Particle.LARGE_SMOKE, loc.clone().add(0.5, 1, 0.5), 2, 0, 0, 0, 0.05);
                        }
                    }
                }
                seconds--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void generateSingleGeode(Location center, Material core) {
        Block block = center.getBlock();
        String key = getKey(center);

        // Store original (likely AIR)
        if (!originalBlocks.containsKey(key)) {
            originalBlocks.put(key, block.getType());
        }

        // Set to Core
        block.setType(core);
        activeCores.add(key);

        // Sound
        center.getWorld().playSound(center, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 2f, 0.5f);
    }

    // --- Helper for Block Tracking ---
    private String getKey(Location loc) {
        return loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }

    private Location parseKey(String key) {
        try {
            String[] parts = key.split(",");
            World w = Bukkit.getWorld(parts[0]);
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            int z = Integer.parseInt(parts[3]);
            return new Location(w, x, y, z);
        } catch (Exception e) { return null; }
    }

    public boolean isGeodeCore(Location loc) {
        return activeCores.contains(getKey(loc));
    }

    public void removeSingleGeode(Location coreLoc) {
        String key = getKey(coreLoc);
        if (!activeCores.contains(key)) return;

        activeCores.remove(key);
        restoreBlock(key);
    }

    public void cleanupGeodes() {
        // Restore all remaining blocks
        for (String key : new HashSet<>(activeCores)) {
            restoreBlock(key);
        }
        activeCores.clear();
        originalBlocks.clear();
        Bukkit.broadcastMessage(ChatColor.GRAY + "The astral energy dissipates...");
    }

    private void restoreBlock(String key) {
        Location loc = parseKey(key);
        if (loc != null && originalBlocks.containsKey(key)) {
            loc.getBlock().setType(originalBlocks.get(key));
        } else if (loc != null) {
            loc.getBlock().setType(Material.AIR); // Safe default
        }
    }
}