package me.mats.bingo;

import me.mats.bingo.command.BingoCommand;
import me.mats.bingo.game.BingoManager;
import me.mats.common.GameModule;
import me.mats.common.world.WorldManager;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class BingoModule implements GameModule {

    @Override
    public void register(JavaPlugin plugin) {
        BingoCommand bingoCommand = new BingoCommand(plugin);
        Objects.requireNonNull(plugin.getCommand("bingo")).setExecutor(bingoCommand);
        Objects.requireNonNull(plugin.getCommand("bingo")).setTabCompleter(bingoCommand);
    }

    @Override
    public void cleanupWorlds() {
        for (World w : BingoManager.getWorlds()) {
            WorldManager.deleteWorld(w);
        }
    }
}
