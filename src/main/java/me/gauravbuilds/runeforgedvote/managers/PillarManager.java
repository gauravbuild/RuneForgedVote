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
    private final Random random = new Random();

    // Store original block types to restore them safely
    private final Map<Location, Material> originalBlocks = new HashMap<>();

    public PillarManager(RuneForgedVote plugin) {
        this.plugin = plugin;
    }

    /**
     * Triggers the visual effect on a specific pillar based on the voting service.
     * @param serviceName The name of the website (e.g., "PlanetMinecraft")
     */
    public void ignitePillar(String serviceName) {
        // 1. Identify which pillar ID (1-4) matches this service
        // For simplicity, we can randomize it if mapping isn't set,
        // or you can add specific "service-name" checks here.
        // Let's pick a random one to keep the area feeling alive regardless of where they voted.
        int pillarId = random.nextInt(4) + 1;

        // 2. Get Location
        Location loc = getPillarLocation(pillarId);
        if (loc == null) return; // Pillar not set yet

        // 3. Lightning Effect
        World world = loc.getWorld();
        if (world == null) return;

        world.strikeLightningEffect(loc); // Visual only, no damage

        // 4. Sound
        world.playSound(loc, Sound.ITEM_TRIDENT_THUNDER, 1f, 1f);
        world.playSound(loc, Sound.BLOCK_BEACON_ACTIVATE, 1f, 1.5f);

        // 5. Particles (Beam Effect)
        // Shoot particles from pillar up to the sky
        new BukkitRunnable() {
            double y = 0;
            @Override
            public void run() {
                if (y > 5) { this.cancel(); return; }
                world.spawnParticle(Particle.END_ROD, loc.clone().add(0.5, y, 0.5), 5, 0.1, 0.1, 0.1, 0.05);
                y += 0.5;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        // 6. Block Swap (Obsidian -> Sea Lantern)
        activateBlock(loc);
    }

    private void activateBlock(Location loc) {
        Block block = loc.getBlock();

        // Don't overwrite if it's already active (Sea Lantern)
        if (block.getType() == Material.SEA_LANTERN) return;

        // Save original state
        originalBlocks.putIfAbsent(loc, block.getType());

        // Change to glowing block
        block.setType(Material.SEA_LANTERN);

        // Schedule Restore (5 seconds later)
        new BukkitRunnable() {
            @Override
            public void run() {
                if (originalBlocks.containsKey(loc)) {
                    block.setType(originalBlocks.get(loc));
                    originalBlocks.remove(loc);
                }
            }
        }.runTaskLater(plugin, 100L); // 5 Seconds (20 ticks * 5)
    }

    private Location getPillarLocation(int id) {
        FileConfiguration config = plugin.getConfig();
        String path = "locations.pillar-" + id;

        if (!config.contains(path + ".world")) return null;

        World w = Bukkit.getWorld(config.getString(path + ".world"));
        double x = config.getDouble(path + ".x");
        double y = config.getDouble(path + ".y");
        double z = config.getDouble(path + ".z");

        return new Location(w, x, y, z);
    }
}