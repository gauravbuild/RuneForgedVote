package me.gauravbuilds.runeforgedvote.managers;

import me.gauravbuilds.runeforgedvote.RuneForgedVote;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MeteorManager {

    private final RuneForgedVote plugin;
    private final List<Location> impactZones = new ArrayList<>();
    private final Random random = new Random();

    public MeteorManager(RuneForgedVote plugin) {
        this.plugin = plugin;
        loadLocations();
    }

    public void loadLocations() {
        impactZones.clear();
        FileConfiguration config = plugin.getConfig();
        // Load up to 10 locations
        for (int i = 1; i <= 10; i++) {
            String path = "locations.meteor-" + i;
            if (config.contains(path + ".world")) {
                World w = Bukkit.getWorld(config.getString(path + ".world"));
                double x = config.getDouble(path + ".x");
                double y = config.getDouble(path + ".y");
                double z = config.getDouble(path + ".z");
                impactZones.add(new Location(w, x, y, z));
            }
        }
    }

    public void startSequence() {
        if (impactZones.isEmpty()) {
            plugin.getLogger().warning("No meteor locations set! Use /rfvote setmeteor <1-10>");
            return;
        }

        // === PHASE 1: THE CHARGE (0s - 3s) ===
        // Set Time to Midnight for atmosphere
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "time set midnight RuneForgedVoteSpawn");

        // Play Warning Hum/Portal sounds
        for (Location loc : impactZones) {
            loc.getWorld().playSound(loc, Sound.BLOCK_BEACON_AMBIENT, 2f, 0.5f);
            loc.getWorld().spawnParticle(Particle.PORTAL, loc.clone().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.1);
        }

        // Start Rain after 3 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                startRainPhase();
            }
        }.runTaskLater(plugin, 60L);
    }

    private void startRainPhase() {
        // === PHASE 2: THE RAIN ===
        // Runs for 15 seconds (300 ticks)
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= 300) {
                    startAftermathPhase();
                    this.cancel();
                    return;
                }

                // Every 10 ticks (0.5s), pick a random spot to fire a meteor
                // Increased frequency slightly since they fall slower now
                if (ticks % 10 == 0) {
                    if (!impactZones.isEmpty()) {
                        Location target = impactZones.get(random.nextInt(impactZones.size()));
                        spawnFallingMeteor(target);
                    }
                }
                ticks += 5;
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }

    private void spawnFallingMeteor(Location target) {
        // Start 40 blocks up (Slightly lower so players see them spawn)
        Location startLoc = target.clone().add(0, 40, 0);
        World world = target.getWorld();

        // The Crystal (Armor Stand)
        ArmorStand meteor = (ArmorStand) world.spawnEntity(startLoc, EntityType.ARMOR_STAND);
        meteor.setVisible(false);
        meteor.setGravity(false);
        meteor.setMarker(true); // Tiny hitbox
        meteor.getEquipment().setHelmet(new ItemStack(Material.AMETHYST_CLUSTER));
        meteor.setHeadPose(new EulerAngle(random.nextDouble(), random.nextDouble(), random.nextDouble()));

        // Falling Logic
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!meteor.isValid() || meteor.getLocation().getY() <= target.getY()) {
                    impact(target);
                    meteor.remove();
                    this.cancel();
                    return;
                }

                // --- SPEED FIX ---
                // Old Speed: 1.5 blocks/tick (Way too fast)
                // New Speed: 0.45 blocks/tick (Slow, cinematic glide)
                meteor.teleport(meteor.getLocation().subtract(0, 0.45, 0));

                // Rotation (Tumble effect)
                EulerAngle current = meteor.getHeadPose();
                meteor.setHeadPose(current.add(0.05, 0.05, 0.05));

                // --- TRAIL ---
                Location pLoc = meteor.getLocation().add(0, 1.5, 0);
                world.spawnParticle(Particle.DRAGON_BREATH, pLoc, 2, 0.1, 0.1, 0.1, 0); // Void trail
                world.spawnParticle(Particle.END_ROD, pLoc, 1, 0.05, 0.05, 0.05, 0);    // White sparkles
                world.spawnParticle(Particle.FIREWORK, pLoc, 0, 0, 0, 0, 0); // Trailing sparks
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void impact(Location loc) {
        World world = loc.getWorld();

        // Impact Visuals
        world.spawnParticle(Particle.FLASH, loc, 1);
        world.spawnParticle(Particle.WITCH, loc, 30, 1, 0.5, 1, 0.2); // Purple explosion
        world.spawnParticle(Particle.LAVA, loc, 10, 0.5, 0.5, 0.5, 0.1);    // Embers
        world.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, loc, 5, 0.2, 0.2, 0.2, 0.05);

        // Sound (Heavy Thud + Crystal Break)
        world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1f, 0.6f);
        world.playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_BREAK, 2f, 0.5f);
    }

    private void startAftermathPhase() {
        // === PHASE 3: THE AFTERMATH ===
        // Restore Time
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "time set night RuneForgedVoteSpawn");

        // Smoke Effect on ground
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= 100) { // 5 seconds
                    this.cancel();
                    return;
                }

                for (Location loc : impactZones) {
                    loc.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, loc, 2, 0.3, 0.1, 0.3, 0.01);
                }
                ticks += 10;
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }
}