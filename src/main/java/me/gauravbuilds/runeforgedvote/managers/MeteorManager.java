package me.gauravbuilds.runeforgedvote.managers;

import me.gauravbuilds.runeforgedvote.RuneForgedVote;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
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
        ConfigurationSection section = config.getConfigurationSection("locations");

        if (section == null) return;

        // Loop through EVERYTHING in 'locations' section
        for (String key : section.getKeys(false)) {
            // Only look for keys starting with "meteor-"
            if (key.startsWith("meteor-")) {
                String path = "locations." + key;
                if (config.contains(path + ".world")) {
                    World w = Bukkit.getWorld(config.getString(path + ".world"));
                    double x = config.getDouble(path + ".x");
                    double y = config.getDouble(path + ".y");
                    double z = config.getDouble(path + ".z");
                    if (w != null) {
                        impactZones.add(new Location(w, x, y, z));
                    }
                }
            }
        }
        plugin.getLogger().info("Loaded " + impactZones.size() + " Meteor Impact Zones.");
    }

    public void startSequence() {
        if (impactZones.isEmpty()) {
            plugin.getLogger().warning("No meteor locations set! Use /rfvote setmeteor <number>");
            return;
        }

        // === PHASE 1: THE CHARGE (0s - 3s) ===
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "time set midnight RuneForgedVoteSpawn");

        for (Location loc : impactZones) {
            loc.getWorld().playSound(loc, Sound.BLOCK_BEACON_AMBIENT, 2f, 0.5f);
            loc.getWorld().spawnParticle(Particle.PORTAL, loc.clone().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.1);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                startRainPhase();
            }
        }.runTaskLater(plugin, 60L);
    }

    private void startRainPhase() {
        // === PHASE 2: THE RAIN ===
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= 300) {
                    startAftermathPhase();
                    this.cancel();
                    return;
                }

                // If you have LOTS of meteors (e.g. 50+), we should spawn them faster
                // Logic: Spawn 1 meteor every X ticks based on how many zones we have
                int spawnRate = impactZones.size() > 20 ? 2 : 10;

                if (ticks % spawnRate == 0) {
                    if (!impactZones.isEmpty()) {
                        Location target = impactZones.get(random.nextInt(impactZones.size()));
                        spawnFallingMeteor(target);
                    }
                }
                ticks += 2; // Tick faster to handle high density
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    private void spawnFallingMeteor(Location target) {
        Location startLoc = target.clone().add(0, 40, 0);
        World world = target.getWorld();

        ArmorStand meteor = (ArmorStand) world.spawnEntity(startLoc, EntityType.ARMOR_STAND);
        meteor.setVisible(false);
        meteor.setGravity(false);
        meteor.setMarker(true);
        meteor.getEquipment().setHelmet(new ItemStack(Material.AMETHYST_CLUSTER));
        meteor.setHeadPose(new EulerAngle(random.nextDouble(), random.nextDouble(), random.nextDouble()));

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!meteor.isValid() || meteor.getLocation().getY() <= target.getY()) {
                    impact(target);
                    meteor.remove();
                    this.cancel();
                    return;
                }
                meteor.teleport(meteor.getLocation().subtract(0, 0.45, 0));
                EulerAngle current = meteor.getHeadPose();
                meteor.setHeadPose(current.add(0.05, 0.05, 0.05));

                Location pLoc = meteor.getLocation().add(0, 1.5, 0);
                world.spawnParticle(Particle.DRAGON_BREATH, pLoc, 2, 0.1, 0.1, 0.1, 0);
                world.spawnParticle(Particle.END_ROD, pLoc, 1, 0.05, 0.05, 0.05, 0);
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void impact(Location loc) {
        World world = loc.getWorld();
        world.spawnParticle(Particle.FLASH, loc, 1);
        world.spawnParticle(Particle.WITCH, loc, 30, 1, 0.5, 1, 0.2);
        world.spawnParticle(Particle.LAVA, loc, 10, 0.5, 0.5, 0.5, 0.1);
        world.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, loc, 5, 0.2, 0.2, 0.2, 0.05);
        world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1f, 0.6f);
        world.playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_BREAK, 2f, 0.5f);
    }

    private void startAftermathPhase() {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "time set night RuneForgedVoteSpawn");
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= 100) { this.cancel(); return; }
                for (Location loc : impactZones) {
                    loc.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, loc, 2, 0.3, 0.1, 0.3, 0.01);
                }
                ticks += 10;
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }
}