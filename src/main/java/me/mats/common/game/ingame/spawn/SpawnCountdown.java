package me.mats.common.game.ingame.spawn;

import me.mats.common.game.GameManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.Duration;
import java.util.List;

// Generic "countdown to release" shared by every game: title/subtitle tick-down, then equips
// everyone with an elytra + their team backpack and lets them go. What exactly clears from the
// spawn platform (spawn-structure-specific geometry) and what happens once players are released
// (registering that game's own ingame listeners, starting its own timers, etc.) are hooks.
public class SpawnCountdown {

    protected final GameManager<?> manager;

    // Keeps track of actual Countdown
    private int countdown;

    private int taskId;
    private final ItemStack elytra;

    public SpawnCountdown(GameManager<?> manager) {
        this.manager = manager;
        elytra = new ItemStack(Material.ELYTRA);
        ItemMeta meta = elytra.getItemMeta();
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        elytra.setItemMeta(meta);
    }

    // Hook: blocks to clear away from the spawn platform once the countdown finishes, so
    // players can glide off it. Tied to one game's spawn structure - empty (nothing cleared)
    // by default.
    protected List<Location> getPlatformClearLocations() {
        return List.of();
    }

    // Hook: whatever a concrete game wants to do once players are released (register its own
    // ingame listeners, start its own timers, etc).
    protected void onPlayersReleased() {
    }

    public void cancel() {
        Bukkit.getScheduler().cancelTask(taskId);
    }

    public void start(int countdownTime) {
        countdown = countdownTime;

        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(manager.getPlugin(), () -> {
            if (countdown == countdownTime) {
                for (Player p : manager.getPlayers()) {
                    p.showTitle(Title.title(Component.text(""), Component.text("§7"+countdown), Title.Times.times(Duration.ofMillis(200), Duration.ofSeconds(countdownTime), Duration.ofMillis(200))));
                }
            } else if (countdown > 10) {
                for (Player p : manager.getPlayers()) {
                    p.sendTitlePart(TitlePart.SUBTITLE, Component.text("§7"+countdown));
                    p.playSound(p, Sound.BLOCK_STONE_BUTTON_CLICK_ON, SoundCategory.MASTER, 0.2F, 2F);
                }
            } else if (countdown > 0) {

                for (Player p : manager.getPlayers()) {
                    if (countdown == 10) {
                        p.sendTitlePart(TitlePart.SUBTITLE, Component.text(""));
                        p.sendTitlePart(TitlePart.TIMES, Title.Times.times(Duration.ofMillis(200), Duration.ofSeconds(1), Duration.ofMillis(400)));
                    }
                    p.sendTitlePart(TitlePart.TITLE, Component.text("§a"+countdown));
                    p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BELL, SoundCategory.MASTER, 1F, 0.79F - 0.03F * countdown);
                }

            } else if (countdown == 0) {
                for (Location loc : getPlatformClearLocations()) {
                    loc.getBlock().setType(Material.AIR);
                    loc.getBlock().getWorld().playSound(loc, Sound.BLOCK_WOOD_BREAK, SoundCategory.BLOCKS, 1F, 1F);
                    loc.getBlock().getWorld().spawnParticle(Particle.BLOCK_CRACK, loc.add(0.5,0.5,0.5), 1, 0.1, 0.1, 0.1, 1, Material.SPRUCE_FENCE.createBlockData());
                }

                for (Player p : manager.getPlayers()) {
                    p.sendTitlePart(TitlePart.TITLE, Component.text("Go!", NamedTextColor.GREEN));
                    p.playSound(p, Sound.ITEM_GOAT_HORN_SOUND_0, SoundCategory.MASTER, 1F, 1.6F);

                    p.getInventory().clear();
                    p.getInventory().setChestplate(elytra);
                    p.getInventory().setItem(8, manager.getTeam(p).getBackpackItem().clone());
                    p.closeInventory();

                }
                onPlayersReleased();
                Bukkit.getScheduler().cancelTask(taskId);
            }
            countdown--;

        }, 0, 20);


    }


}
