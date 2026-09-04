package me.mats.common.game.waiting;

import me.mats.common.game.GameManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

import java.time.Duration;

public class WaitingCountdown {

    private final GameManager<?> manager;

    // Keeps track of actual Countdown
    private int countdown;

    // ID of Countdown Scheduler
    private int taskID = -2;

    // The number to divide by when determining current percentage
    private int maxTime;

    public WaitingCountdown(GameManager<?> manager) {
        this.manager = manager;
    }

    public void start(int countdownTime, boolean changeMaxTime) {
        if (changeMaxTime)
            maxTime = countdownTime;
        countdown = countdownTime;
        taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(manager.getPlugin(), new Runnable() {
            @Override
            public void run() {
                if (countdown == 0) {
                    for (Player p : manager.getPlayers()) {
                        p.sendMessage(manager.brandedStatus("§eGame starting..."));
                        p.playSound(p, Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.MASTER,1F, 1F);
                        p.setLevel(0);
                        p.setExp(0);
                    }
                    stop();
                    manager.getGameState().stop();

                } else if (countdown <= 5) {
                    for (Player p : manager.getPlayers()) {
                        if (countdown == 1) {
                            p.sendMessage(manager.brandedStatus("§7The game starts in §e" + countdown + "§7 second"));
                        } else {
                            p.sendMessage(manager.brandedStatus("§7The game starts in §e" + countdown + "§7 seconds"));
                        }
                        p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BELL, SoundCategory.MASTER, 1F, 0.79F - 0.03F * countdown);
                        if (countdown == 5) {
                            final Title title = Title.title(manager.getBrandWordmark(), Component.text("§e"+countdown), Title.Times.times(Duration.ofMillis(50), Duration.ofMillis(5000), Duration.ofMillis(50)));
                            p.showTitle(title);
                        } else {
                            p.sendTitlePart(TitlePart.SUBTITLE, Component.text("§e"+countdown));
                        }
                    }
                } else if (countdown % 10 == 0) {
                    for (Player p : manager.getPlayers()) {
                        p.sendMessage(manager.brandedStatus("§7The game starts in §e" + countdown + "§7 seconds"));
                        p.playSound(p, Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, 1F, 0.5F);
                    }
                }
                for (Player p : manager.getPlayers()) {
                    p.setLevel(countdown);
                    p.setExp((float) countdown / maxTime);
                }
                countdown--;
            }
        }, 0, 20);
    }

    // Pause the Countdown
    public void stop() {
        Bukkit.getScheduler().cancelTask(taskID);
        taskID = -2;
        }

    public int getMaxTime() {
        return maxTime;
    }

    // Check if paused
    public boolean isCounting() {
        return (taskID != -2);
    }

    // Get remaining seconds
    public int getRemainingTime() {
        return countdown;
    }

    // Set remaining seconds
    public void setRemainingTime(int sec) {
        if (sec > maxTime) {
            maxTime = sec;
        }
        countdown = sec;
        for (Player p : manager.getPlayers()) {
            p.setLevel(sec);
            p.setExp((float) sec / maxTime);
        }
    }



}
