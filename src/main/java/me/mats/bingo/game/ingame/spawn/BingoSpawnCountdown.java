package me.mats.bingo.game.ingame.spawn;

import me.mats.bingo.game.ingame.BingoIngameState;
import me.mats.common.game.ingame.spawn.SpawnCountdown;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BingoSpawnCountdown extends SpawnCountdown {

    private final BingoIngameState state;

    public BingoSpawnCountdown(BingoIngameState state) {
        super(state.getManager());
        this.state = state;
    }

    @Override
    @NotNull
    protected List<Location> getPlatformClearLocations() {
        int y = manager.getWorld().getSpawnLocation().getBlockY()+2;
        return List.of(new Location(manager.getWorld(), 2, y, 3), new Location(manager.getWorld(), 1, y, 3), new Location(manager.getWorld(), 1, y, 4), new Location(manager.getWorld(), 0, y, 4), new Location(manager.getWorld(), -1, y, 4), new Location(manager.getWorld(), -1, y, 3), new Location(manager.getWorld(), -2, y, 3), new Location(manager.getWorld(), -3, y, 2), new Location(manager.getWorld(), -3, y, 1), new Location(manager.getWorld(), -4, y, 1), new Location(manager.getWorld(), -4, y, 0), new Location(manager.getWorld(), -4, y, -1), new Location(manager.getWorld(), -3, y, -1), new Location(manager.getWorld(), -3, y, -2), new Location(manager.getWorld(), -2, y, -3), new Location(manager.getWorld(), -1, y, -3), new Location(manager.getWorld(), -1, y, -4), new Location(manager.getWorld(), 0, y, -4), new Location(manager.getWorld(), 1, y, -4), new Location(manager.getWorld(), 1, y, -3), new Location(manager.getWorld(), 2, y, -3), new Location(manager.getWorld(), 3, y, -2), new Location(manager.getWorld(), 3, y, -1), new Location(manager.getWorld(), 4, y, -1), new Location(manager.getWorld(), 4, y, 0), new Location(manager.getWorld(), 4, y, 1), new Location(manager.getWorld(), 3, y, 1), new Location(manager.getWorld(), 3, y, 2));
    }

    @Override
    protected void onPlayersReleased() {
        Bukkit.getPluginManager().registerEvents(state.getBingoCollectListener(), state.getManager().getPlugin());
        Bukkit.getPluginManager().registerEvents(state.getBackpackListener(), state.getManager().getPlugin());
        Bukkit.getPluginManager().registerEvents(state.getAbilitiesListener(), state.getManager().getPlugin());

        HandlerList.unregisterAll(state.getAbilitiesMenuListener());

        state.getAbilities().setInitialAbilities();
        state.startTimer();
    }
}
