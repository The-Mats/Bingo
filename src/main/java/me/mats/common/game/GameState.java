package me.mats.common.game;

import org.bukkit.entity.Player;

public abstract class GameState<M extends GameManager<?>> {

    protected M manager;

    public abstract void start();
    public abstract void stop();

    // Force the game to end immediately, regardless of natural win/lose conditions
    public abstract void abort();

    public abstract void addPlayer(Player p);

    // Hook for a concrete game to re-sync anything relog-sensitive for the returning player
    // (e.g. bingo's ability lists, which are keyed off Player identity, or resending
    // whatever progress UI a relog wiped from their client)
    public void onPlayerReconnect(Player oldPlayer, Player newPlayer) {
    }

    public M getManager() {
        return manager;
    }
}
