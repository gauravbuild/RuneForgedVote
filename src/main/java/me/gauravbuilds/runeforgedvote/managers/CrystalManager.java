package me.gauravbuilds.runeforgedvote.managers;

import me.gauravbuilds.runeforgedvote.RuneForgedVote;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

public class CrystalManager {

    private final RuneForgedVote plugin;
    private ArmorStand crystalEntity;
    private ArmorStand hologramEntity; // The text above it
    private double currentAngle = 0.0;
    private Location spawnLoc;

    public CrystalManager(RuneForgedVote plugin) {
        this.plugin = plugin;
    }

    public void startAnimation() {
        // Run every tick (1/20th second) for smooth animation
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!plugin.isEnabled()) {
                    this.cancel();
                    removeCrystal();
                    return;
                }
                updateCrystal();
            }
        }.runTaskTimer(plugin, 20L, 1L);
    }

    private void updateCrystal() {
        // 1. Check if location is set
        if (spawnLoc == null) {
            loadLocation();
            if (spawnLoc == null) return; // Still null? Wait.
        }

        // 2. Ensure Entity Exists
        if (crystalEntity == null || !crystalEntity.isValid()) {
            spawnCrystal();
        }

        // 3. Calculate Speed based on Votes
        // Logic: Base Speed (0.1) + (Votes * 0.08)
        // 0 Votes = 0.1 (Slow)
        // 50 Votes = 4.1 (Super Fast)
        int votes = plugin.getVoteManager().getGlobalVotes();
        int maxVotes = plugin.getVoteManager().getVotesNeeded();

        double speed = 0.1 + (votes * 0.08);

        // 4. Rotate
        currentAngle += speed;
        if (currentAngle > 360) currentAngle = 0;

        // Convert math angle to EulerAngle (Radians)
        double radians = Math.toRadians(currentAngle);
        crystalEntity.setHeadPose(new EulerAngle(0, radians, 0));

        // 5. Particles
        // More votes = More/Different particles
        if (votes >= (maxVotes - 5)) {
            // CRITICAL STATE (Purple & Red)
            spawnLoc.getWorld().spawnParticle(Particle.DRAGON_BREATH, spawnLoc.clone().add(0, 1.5, 0), 2, 0.2, 0.2, 0.2, 0.05);
            spawnLoc.getWorld().spawnParticle(Particle.FLAME, spawnLoc.clone().add(0, 1.5, 0), 1, 0.1, 0.1, 0.1, 0.05);
        } else if (votes > 0) {
            // ACTIVE STATE (Blue/Purple)
            spawnLoc.getWorld().spawnParticle(Particle.WITCH, spawnLoc.clone().add(0, 1.5, 0), 1, 0.2, 0.2, 0.2, 0);
        }

        // 6. Update Hologram Text
        updateHologram(votes, maxVotes);
    }

    private void spawnCrystal() {
        // Remove old ones first to prevent duplicates
        removeCrystal();

        World world = spawnLoc.getWorld();
        if (world == null) return;

        // --- THE CRYSTAL (Armor Stand) ---
        crystalEntity = (ArmorStand) world.spawnEntity(spawnLoc, EntityType.ARMOR_STAND);
        crystalEntity.setVisible(false);
        crystalEntity.setGravity(false);
        crystalEntity.setBasePlate(false);
        crystalEntity.setArms(false);
        crystalEntity.setInvulnerable(true);
        // Put the item on head (Nether Star or Amethyst)
        crystalEntity.getEquipment().setHelmet(new ItemStack(Material.AMETHYST_CLUSTER));

        // --- THE HOLOGRAM (Armor Stand above) ---
        Location holoLoc = spawnLoc.clone().add(0, 0.8, 0); // slightly higher
        hologramEntity = (ArmorStand) world.spawnEntity(holoLoc, EntityType.ARMOR_STAND);
        hologramEntity.setVisible(false);
        hologramEntity.setGravity(false);
        hologramEntity.setMarker(true); // Tiny hitbox
        hologramEntity.setCustomNameVisible(true);
        hologramEntity.setCustomName(ChatColor.DARK_PURPLE + "The Oracle");
    }

    private void updateHologram(int votes, int max) {
        if (hologramEntity == null || !hologramEntity.isValid()) return;

        if (votes >= (max - 5)) {
            // HYPE MODE
            hologramEntity.setCustomName(ChatColor.translateAlternateColorCodes('&',
                    "&c&k||| &4&lALIGNMENT IMMINENT &c&k|||"));
        } else {
            // NORMAL MODE
            hologramEntity.setCustomName(ChatColor.translateAlternateColorCodes('&',
                    "&d&lTHE ORACLE &7(&b" + votes + "/" + max + "&7)"));
        }
    }

    public void removeCrystal() {
        if (crystalEntity != null) {
            crystalEntity.remove();
            crystalEntity = null;
        }
        if (hologramEntity != null) {
            hologramEntity.remove();
            hologramEntity = null;
        }
    }

    // Check config for location
    private void loadLocation() {
        FileConfiguration config = plugin.getConfig();
        if (config.contains("locations.altar.world")) {
            World w = Bukkit.getWorld(config.getString("locations.altar.world"));
            double x = config.getDouble("locations.altar.x");
            double y = config.getDouble("locations.altar.y");
            double z = config.getDouble("locations.altar.z");
            this.spawnLoc = new Location(w, x, y, z);
        }
    }
}