package me.gauravbuilds.runeforgedvote.managers;

import me.gauravbuilds.runeforgedvote.RuneForgedVote;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class PillarManager {

    private final RuneForgedVote plugin;
    // Store original block types to restore them safely
    private final Map<Location, Material> originalBlocks = new HashMap<>();

    public PillarManager(RuneForgedVote plugin) {
        this.plugin = plugin;
    }

    /**
     * Triggers the visual effect on a specific pillar.
     * @param guardianName The name of the guardian (ignis, cryo, terra, aether)
     */
    public void ignitePillar(String guardianName) {
        // 1. Get Location by Name
        Location loc = getPillarLocation(guardianName.toLowerCase());

        if (loc == null) {
            // If specific pillar not found, maybe pick a random one?
            // Or just return. Let's return to keep it strict.
            return;
        }

        // 2. Lightning Effect
        World world = loc.getWorld();
        if (world == null) return;

        world.strikeLightningEffect(loc);

        // 3. Sound
        world.playSound(loc, Sound.ITEM_TRIDENT_THUNDER, 1f, 1f);
        world.playSound(loc, Sound.BLOCK_BEACON_ACTIVATE, 2f, 1.5f);

        // 4. Particles (Beam Effect)
        new BukkitRunnable() {
            double y = 0;
            @Override
            public void run() {
                if (y > 10) { this.cancel(); return; }
                // Spiraling particles
                double x = Math.cos(y) * 0.5;
                double z = Math.sin(y) * 0.5;
                world.spawnParticle(Particle.END_ROD, loc.clone().add(0.5 + x, y, 0.5 + z), 1, 0, 0, 0, 0);
                world.spawnParticle(Particle.END_ROD, loc.clone().add(0.5 - x, y, 0.5 - z), 1, 0, 0, 0, 0);
                y += 0.5;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        // 5. Block Swap
        activateBlock(loc);
    }

    private void activateBlock(Location loc) {
        Block block = loc.getBlock();
        if (block.getType() == Material.SEA_LANTERN) return;

        originalBlocks.putIfAbsent(loc, block.getType());
        block.setType(Material.SEA_LANTERN);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (originalBlocks.containsKey(loc)) {
                    block.setType(originalBlocks.get(loc));
                    originalBlocks.remove(loc);
                }
            }
        }.runTaskLater(plugin, 60L); // 3 Seconds
    }

    private Location getPillarLocation(String name) {
        FileConfiguration config = plugin.getConfig();
        String path = "locations.pillar-" + name; // locations.pillar-ignis

        if (!config.contains(path + ".world")) return null;

        World w = Bukkit.getWorld(config.getString(path + ".world"));
        double x = config.getDouble(path + ".x");
        double y = config.getDouble(path + ".y");
        double z = config.getDouble(path + ".z");

        return new Location(w, x, y, z);
    }
}