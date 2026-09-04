package me.mats.bingo;

import me.mats.bingo.command.BingoCommand;
import me.mats.bingo.game.BingoManager;
import me.mats.common.CommonPlugin;
import me.mats.common.world.WorldManager;
import org.bukkit.World;
import java.util.Objects;

public final class Bingo extends CommonPlugin {

    @Override
    protected void registerGameCommands() {
        BingoCommand bingoCommand = new BingoCommand(this);
        Objects.requireNonNull(getCommand("bingo")).setExecutor(bingoCommand);
        Objects.requireNonNull(getCommand("bingo")).setTabCompleter(bingoCommand);
    }

    @Override
    protected void cleanupGameWorlds() {
        for (World w : BingoManager.getWorlds()) {
            WorldManager.deleteWorld(w);
        }
    }

    public void startBingo() {
        getLogger().info("Starting the Bingo!");
        BingoManager bingoManager = new BingoManager(this);
    }

    public static Bingo getInstance() {
        return getPlugin(Bingo.class);
    }

}
