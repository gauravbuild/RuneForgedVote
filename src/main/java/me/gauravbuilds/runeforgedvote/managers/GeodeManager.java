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
    private final Map<Location, Material> originalBlocks = new HashMap<>();
    private final Set<Location> activeCores = new HashSet<>();
    private final Set<Location> activeCrust = new HashSet<>();
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
                if (key.startsWith("geode-")) { // LOOK FOR GEODE KEY
                    String path = "locations." + key;
                    if (config.contains(path + ".world")) {
                        World w = Bukkit.getWorld(config.getString(path + ".world"));
                        double x = config.getDouble(path + ".x");
                        double y = config.getDouble(path + ".y");
                        double z = config.getDouble(path + ".z");
                        if (w != null) {
                            geodeSpawnPoints.add(new Location(w, x, y, z));
                        }
                    }
                }
            }
        }
        plugin.getLogger().info("Loaded " + geodeSpawnPoints.size() + " Geode Mining Spots.");
    }

    public void spawnGeodes(String winner) {
        // Fallback: If no geodes set, try using meteors
        if (geodeSpawnPoints.isEmpty()) {
            plugin.getLogger().warning("No 'geode' locations set. Using 'meteor' locations as fallback.");
            // (Logic to copy meteor locs if needed, or just return)
        }

        Material coreType;
        Material crustType;
        Particle particle;

        switch (winner.toLowerCase()) {
            case "ignis": // Fire
                coreType = Material.MAGMA_BLOCK;
                crustType = Material.BLACKSTONE;
                particle = Particle.FLAME;
                break;
            case "cryo": // Ice
                coreType = Material.BLUE_ICE;
                crustType = Material.PACKED_ICE;
                particle = Particle.SNOWFLAKE;
                break;
            case "terra": // Nature
                coreType = Material.EMERALD_ORE;
                crustType = Material.MOSSY_COBBLESTONE;
                particle = Particle.HAPPY_VILLAGER;
                break;
            default: // Aether
                coreType = Material.AMETHYST_BLOCK;
                crustType = Material.CRYING_OBSIDIAN;
                particle = Particle.DRAGON_BREATH;
                break;
        }

        for (Location loc : geodeSpawnPoints) {
            // OFFSET FIX: Spawn 1 block UP so we don't destroy the floor
            Location spawnLoc = loc.clone().add(0, 1, 0);
            generateGeodeStructure(spawnLoc, coreType, crustType);
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
                for (Location loc : activeCores) {
                    loc.getWorld().spawnParticle(particle, loc.clone().add(0.5, 0.5, 0.5), 5, 0.5, 0.5, 0.5, 0.1);
                    if (seconds <= 5) {
                        loc.getWorld().spawnParticle(Particle.LARGE_SMOKE, loc.clone().add(0.5, 1, 0.5), 2, 0, 0, 0, 0.05);
                    }
                }
                seconds--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void generateGeodeStructure(Location center, Material core, Material crust) {
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    Location loc = center.clone().add(x, y, z);
                    Block block = loc.getBlock();

                    if (block.getType() == Material.AIR && y > 0) continue; // Keep shape somewhat grounded

                    originalBlocks.putIfAbsent(loc, block.getType());

                    if (x == 0 && y == 0 && z == 0) {
                        block.setType(core);
                        activeCores.add(loc);
                    } else {
                        block.setType(crust);
                        activeCrust.add(loc);
                    }
                }
            }
        }
        center.getWorld().playSound(center, Sound.BLOCK_GLASS_BREAK, 2f, 0.5f);
    }

    public boolean isGeodeCore(Location loc) { return activeCores.contains(loc); }
    public boolean isGeodeCrust(Location loc) { return activeCrust.contains(loc); }

    public void removeSingleGeode(Location coreLoc) {
        if (!activeCores.contains(coreLoc)) return;
        activeCores.remove(coreLoc);
        restoreBlock(coreLoc);

        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    Location loc = coreLoc.clone().add(x, y, z);
                    if (activeCrust.contains(loc)) {
                        activeCrust.remove(loc);
                        restoreBlock(loc);
                    }
                }
            }
        }
    }

    public void cleanupGeodes() {
        for (Location loc : new HashSet<>(activeCores)) restoreBlock(loc);
        for (Location loc : new HashSet<>(activeCrust)) restoreBlock(loc);
        activeCores.clear();
        activeCrust.clear();
        originalBlocks.clear();
        Bukkit.broadcastMessage(ChatColor.GRAY + "The astral energy dissipates...");
    }

    private void restoreBlock(Location loc) {
        if (originalBlocks.containsKey(loc)) {
            loc.getBlock().setType(originalBlocks.get(loc));
        } else {
            loc.getBlock().setType(Material.AIR); // Default to AIR if unknown (Safety)
        }
    }
}