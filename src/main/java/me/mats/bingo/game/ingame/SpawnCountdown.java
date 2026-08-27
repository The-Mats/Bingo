package me.mats.bingo.game.ingame;

import me.mats.bingo.game.BingoManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.*;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class SpawnCountdown {

    private BingoManager manager;

    // Keeps track of actual Countdown
    private int countdown;

    private int taskId;
    private ItemStack elytra;
    private final IngameState state;




    public SpawnCountdown(BingoManager manager, IngameState state) {
        this.manager = manager;
        this.state = state;
        elytra = new ItemStack(Material.ELYTRA);
        ItemMeta meta = elytra.getItemMeta();
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        elytra.setItemMeta(meta);
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
                // Register Listeners
                Bukkit.getPluginManager().registerEvents(state.getBingoCollectListener(), manager.getPlugin());
                Bukkit.getPluginManager().registerEvents(state.getBackpackListener(), manager.getPlugin());
                Bukkit.getPluginManager().registerEvents(state.getAbilitiesListener(), manager.getPlugin());

                HandlerList.unregisterAll(state.getAbilitiesMenuListener());

                for (Location loc : getLocations()) {
                    loc.getBlock().setType(Material.AIR);
                    loc.getBlock().getWorld().playSound(loc, Sound.BLOCK_WOOD_BREAK, SoundCategory.BLOCKS, 1F, 1F);
                    loc.getBlock().getWorld().spawnParticle(Particle.BLOCK_CRACK, loc.add(0.5,0.5,0.5), 1, 0.1, 0.1, 0.1, 1, Material.SPRUCE_FENCE.createBlockData());
                }

                for (Player p : manager.getPlayers()) {
                    p.sendTitlePart(TitlePart.TITLE, Component.text("Go!", NamedTextColor.GREEN));
                    p.playSound(p, Sound.ITEM_GOAT_HORN_SOUND_0, SoundCategory.MASTER, 1F, 1.6F);

                    p.getInventory().clear();
                    p.getInventory().setChestplate(elytra);
                    p.getInventory().setItem(8, manager.getTeam(p).getBackpackItem());
                    p.closeInventory();

                }
                state.getAbilities().setInitialAbilities();
                state.startTimer();
                Bukkit.getScheduler().cancelTask(taskId);
            }
            countdown--;

        }, 0, 20);


    }

    @NotNull
    private List<Location> getLocations() {
        int y = manager.getWorld().getSpawnLocation().getBlockY()+2;
        return List.of(new Location(manager.getWorld(), 2, y, 3), new Location(manager.getWorld(), 1, y, 3), new Location(manager.getWorld(), 1, y, 4), new Location(manager.getWorld(), 0, y, 4), new Location(manager.getWorld(), -1, y, 4), new Location(manager.getWorld(), -1, y, 3), new Location(manager.getWorld(), -2, y, 3), new Location(manager.getWorld(), -3, y, 2), new Location(manager.getWorld(), -3, y, 1), new Location(manager.getWorld(), -4, y, 1), new Location(manager.getWorld(), -4, y, 0), new Location(manager.getWorld(), -4, y, -1), new Location(manager.getWorld(), -3, y, -1), new Location(manager.getWorld(), -3, y, -2), new Location(manager.getWorld(), -2, y, -3), new Location(manager.getWorld(), -1, y, -3), new Location(manager.getWorld(), -1, y, -4), new Location(manager.getWorld(), 0, y, -4), new Location(manager.getWorld(), 1, y, -4), new Location(manager.getWorld(), 1, y, -3), new Location(manager.getWorld(), 2, y, -3), new Location(manager.getWorld(), 3, y, -2), new Location(manager.getWorld(), 3, y, -1), new Location(manager.getWorld(), 4, y, -1), new Location(manager.getWorld(), 4, y, 0), new Location(manager.getWorld(), 4, y, 1), new Location(manager.getWorld(), 3, y, 1), new Location(manager.getWorld(), 3, y, 2));
    }


}
