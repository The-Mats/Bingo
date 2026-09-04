package me.mats.common;

import org.bukkit.plugin.java.JavaPlugin;

// One game mode's plugin-bootstrap glue: wiring its own command(s) up on enable, and deleting
// its own leftover worlds on disable. GameSuite (hosting several game modes: Bingo, Streak, ...)
// just holds one of these per game instead of growing its enable/disable hooks per game.
public interface GameModule {

    void register(JavaPlugin plugin);

    void cleanupWorlds();
}
