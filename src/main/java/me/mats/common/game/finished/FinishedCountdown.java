package me.mats.common.game.finished;

import me.mats.common.game.GameManager;
import me.mats.common.game.GameState;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class FinishedCountdown {
    private final GameManager<?> manager;
    private final GameState<?> state;

    // Keeps track of actual Countdown
    private float countdown;

    private int taskId;


    public FinishedCountdown(GameManager<?> manager, GameState<?> state) {
        this.manager = manager;
        this.state = state;

    }

    public void cancel() {
        Bukkit.getScheduler().cancelTask(taskId);
    }

    public void start(int countdownTime) {
        countdown = countdownTime;

        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(manager.getPlugin(), () ->  {
            if (countdown == countdownTime) {
                for (Player p : manager.getPlayers()) {
                    p.clearActivePotionEffects();
                    p.getInventory().clear();
                }

            } else if (countdown > 0) {
                for (Player p : manager.getPlayers()) {
                    p.setExp(countdown / countdownTime);
                }

            } else if (countdown < 0.05F) {
                state.stop();
                Bukkit.getScheduler().cancelTask(taskId);
            }
            countdown = countdown - 0.05F;
        },0,1);

    }




}
