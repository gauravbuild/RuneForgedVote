package me.gauravbuilds.runeforgedvote.managers;

import me.gauravbuilds.runeforgedvote.RuneForgedVote;
import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VoteManager {

    private final RuneForgedVote plugin;
    private int globalVotes;
    private final int VOTES_FOR_PARTY;
    private final Random random = new Random();

    public VoteManager(RuneForgedVote plugin) {
        this.plugin = plugin;
        this.globalVotes = plugin.getConfig().getInt("global-votes", 0);
        this.VOTES_FOR_PARTY = plugin.getConfig().getInt("votes-needed", 50);
    }

    public void handleVote(String playerName, String serviceName) {
        // 1. Update Counter
        globalVotes++;
        plugin.getConfig().set("global-votes", globalVotes);
        plugin.saveConfig();

        // 2. Handle Player Rewards
        Player player = Bukkit.getPlayer(playerName);
        if (player != null) {
            giveIndividualRewards(player);

            // Visuals for the voter
            player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1f, 1f);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.vote-received", "&eStar Ignited!")));

            // Notify Pillar Manager to strike lightning!
            // (We will code this next, but here is where we call it)
            plugin.getPillarManager().ignitePillar(serviceName);
        }

        // 3. Broadcast Progress
        String broadcast = plugin.getConfig().getString("messages.broadcast", "&f%player% &7voted!");
        broadcast = broadcast.replace("%player%", playerName)
                .replace("%current%", String.valueOf(globalVotes))
                .replace("%max%", String.valueOf(VOTES_FOR_PARTY));
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', broadcast));

        // 4. Check for Party
        if (globalVotes >= VOTES_FOR_PARTY) {
            startAstralAlignment();
        }
    }

    private void giveIndividualRewards(Player player) {
        // 1. Money (Hook to Vault/Console)
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "eco give " + player.getName() + " 500");

        // 2. Stardust Item (Custom Nether Star)
        ItemStack stardust = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = stardust.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&d&l✧ Stardust ✧"));
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "A fragment of a fallen star.");
        lore.add(ChatColor.GRAY + "Used for Rune Forging.");
        meta.setLore(lore);
        stardust.setItemMeta(meta);

        player.getInventory().addItem(stardust);
    }

    private void startAstralAlignment() {
        // RESET COUNTER
        globalVotes = 0;
        plugin.getConfig().set("global-votes", 0);
        plugin.saveConfig();

        // ANNOUNCE
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.party-start")));
        Bukkit.broadcastMessage("");

        // EVENT LOGIC
        final long originalTime = Bukkit.getWorlds().get(0).getTime(); // Remember old time

        // 1. Set Midnight (Visual)
        for (World world : Bukkit.getWorlds()) {
            world.setTime(18000);
        }

        // 2. The Meteor Shower (Looping Task)
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= 200) { // Lasts 10 seconds (200 ticks)
                    // END EVENT
                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.party-end")));
                    Bukkit.getWorlds().get(0).setTime(originalTime); // Restore time
                    this.cancel();
                    return;
                }

                // Every 10 ticks (0.5s), spawn meteors near players
                if (ticks % 10 == 0) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        spawnMeteor(p.getLocation());
                    }
                }

                ticks += 10;
            }
        }.runTaskTimer(plugin, 0L, 10L);

        // 3. Give Rewards to EVERYONE
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.5f, 0.8f);
            // Give Crate Key
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "crate give key cosmic " + p.getName() + " 1");
            p.sendMessage(ChatColor.GREEN + "You received a Cosmic Key!");
        }
    }

    private void spawnMeteor(Location center) {
        // Spawn a firework high above the player
        Location spawnLoc = center.clone().add(random.nextInt(10) - 5, 20, random.nextInt(10) - 5);

        Firework fw = center.getWorld().spawn(spawnLoc, Firework.class);
        FireworkMeta meta = fw.getFireworkMeta();

        // Make it look like a star (Ball, White/Purple)
        meta.addEffect(FireworkEffect.builder()
                .with(FireworkEffect.Type.BALL_LARGE)
                .withColor(Color.PURPLE, Color.WHITE)
                .withFade(Color.BLUE)
                .trail(true)
                .build());

        meta.setPower(0); // Explodes quickly (or hits ground if we calculated velocity, but this is simple)
        fw.setFireworkMeta(meta);

        // Push it DOWN (Meteor Effect)
        fw.setVelocity(new org.bukkit.util.Vector(0, -2, 0));
    }

    public int getGlobalVotes() {
        return globalVotes;
    }

    public int getVotesNeeded() {
        return VOTES_FOR_PARTY;
    }
}