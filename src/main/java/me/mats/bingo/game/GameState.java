package me.mats.bingo.game;

import org.bukkit.entity.Player;

public abstract class GameState {

    protected BingoManager manager;

    public abstract void start();
    public abstract void stop();

    // Force the game to end immediately, regardless of natural win/lose conditions
    public abstract void abort();

    public abstract void addPlayer(Player p);

    // Re-sends whatever advancement UI this state is responsible for to a single player,
    // e.g. after a relog wiped their client-side state
    public abstract void resendAdvancements(Player p);

    public BingoManager getManager() {
        return manager;
    }
}
