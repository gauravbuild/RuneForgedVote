package me.gauravbuilds.runeforgedvote.managers;

import me.gauravbuilds.runeforgedvote.RuneForgedVote;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

import java.util.Random;

public class CrystalManager {

    private final RuneForgedVote plugin;
    private final Random random = new Random();

    // Entities
    private ArmorStand crystalEntity;
    private ArmorStand hologramTitle;  // Top Line
    private ArmorStand hologramStatus; // Bottom Line

    // Variables
    private double currentAngle = 0.0;
    private Location spawnLoc;

    public CrystalManager(RuneForgedVote plugin) {
        this.plugin = plugin;
    }

    public void startAnimation() {
        // Run every tick for smooth rotation
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

        // Run every 5 ticks for "Area Atmosphere" (Performance friendly)
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!plugin.isEnabled()) {
                    this.cancel();
                    return;
                }
                playAmbientAtmosphere();
            }
        }.runTaskTimer(plugin, 20L, 5L);
    }

    private void updateCrystal() {
        if (spawnLoc == null) {
            loadLocation();
            if (spawnLoc == null) return;
        }

        if (crystalEntity == null || !crystalEntity.isValid()) {
            spawnCrystal();
        }

        int votes = plugin.getVoteManager().getGlobalVotes();
        int maxVotes = plugin.getVoteManager().getVotesNeeded();

        // 1. ROTATION LOGIC
        // Speed increases as votes get closer to 50
        double speed = 0.15 + (votes * 0.15);
        currentAngle += speed;
        if (currentAngle > 360) currentAngle = 0;
        crystalEntity.setHeadPose(new EulerAngle(0, Math.toRadians(currentAngle), 0));

        // 2. CENTER PARTICLES (The Beam)
        // A gentle spiral going up
        double y = (System.currentTimeMillis() % 2000) / 1000.0 * 2; // Loops 0 to 2
        double x = Math.cos(currentAngle * 0.05) * 0.7;
        double z = Math.sin(currentAngle * 0.05) * 0.7;
        spawnLoc.getWorld().spawnParticle(Particle.WITCH, spawnLoc.clone().add(x, 1 + y, z), 0, 0, 0, 0, 0);

        // 3. UPDATE TEXT
        updateHolograms(votes, maxVotes);
    }

    private void playAmbientAtmosphere() {
        if (spawnLoc == null) return;
        World world = spawnLoc.getWorld();

        // Spawn 3 random "Fireflies" within 10 blocks radius
        for (int i = 0; i < 3; i++) {
            double rx = (random.nextDouble() * 20) - 10; // -10 to +10
            double rz = (random.nextDouble() * 20) - 10;
            double ry = (random.nextDouble() * 5);       // 0 to 5 high

            Location particleLoc = spawnLoc.clone().add(rx, ry, rz);

            // "End Rod" looks like magic white sparkles
            world.spawnParticle(Particle.END_ROD, particleLoc, 1, 0, 0, 0, 0);

            // "Dragon Breath" looks like purple void dust (rarely)
            if (random.nextBoolean()) {
                world.spawnParticle(Particle.DRAGON_BREATH, particleLoc, 0, 0, 0.05, 0, 0);
            }
        }
    }

    private void spawnCrystal() {
        removeCrystal();
        World world = spawnLoc.getWorld();
        if (world == null) return;

        // --- 1. THE CRYSTAL ITEM ---
        crystalEntity = (ArmorStand) world.spawnEntity(spawnLoc, EntityType.ARMOR_STAND);
        crystalEntity.setVisible(false);
        crystalEntity.setGravity(false);
        crystalEntity.setBasePlate(false);
        crystalEntity.setArms(false);
        crystalEntity.setInvulnerable(true);

        // Create GLOWING Item
        ItemStack item = new ItemStack(Material.AMETHYST_CLUSTER);
        ItemMeta meta = item.getItemMeta();
        meta.addEnchant(Enchantment.UNBREAKING, 1, true); // Glow effect
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);        // Hide "Efficiency I" text
        item.setItemMeta(meta);

        crystalEntity.getEquipment().setHelmet(item);

        // --- 2. HOLOGRAM (Title) ---
        Location titleLoc = spawnLoc.clone().add(0, 1.1, 0);
        hologramTitle = createHologramLine(titleLoc, ChatColor.translateAlternateColorCodes('&', "&d&l✦ THE ORACLE ✦"));

        // --- 3. HOLOGRAM (Status) ---
        Location statusLoc = spawnLoc.clone().add(0, 0.8, 0);
        hologramStatus = createHologramLine(statusLoc, ChatColor.GRAY + "Initializing...");
    }

    private ArmorStand createHologramLine(Location loc, String text) {
        ArmorStand as = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        as.setVisible(false);
        as.setGravity(false);
        as.setMarker(true); // Tiny hitbox, players can't click it
        as.setCustomNameVisible(true);
        as.setCustomName(text);
        return as;
    }

    private void updateHolograms(int votes, int max) {
        if (hologramStatus == null || !hologramStatus.isValid()) return;

        // Dynamic Progress Bar
        String bar = getProgressBar(votes, max);
        String color = (votes >= max) ? "&a" : "&b"; // Green if ready, Blue if charging

        String text = ChatColor.translateAlternateColorCodes('&',
                "&7Votes: " + color + votes + "&8/&7" + max + " " + bar);

        hologramStatus.setCustomName(text);
    }

    private String getProgressBar(int current, int max) {
        int bars = 10;
        float percent = (float) current / max;
        int progress = (int) (bars * percent);
        if (progress > bars) progress = bars;

        StringBuilder sb = new StringBuilder("&8[");
        for (int i = 0; i < bars; i++) {
            if (i < progress) sb.append("&d|"); // Filled (Pink/Purple)
            else sb.append("&8|");             // Empty (Gray)
        }
        sb.append("&8]");
        return sb.toString();
    }

    public void removeCrystal() {
        if (crystalEntity != null) crystalEntity.remove();
        if (hologramTitle != null) hologramTitle.remove();
        if (hologramStatus != null) hologramStatus.remove();
    }

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