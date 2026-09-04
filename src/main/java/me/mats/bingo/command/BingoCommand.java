package me.mats.bingo.command;

import me.mats.bingo.game.BingoManager;
import me.mats.common.command.GameCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class BingoCommand extends GameCommand<BingoManager> {

    public BingoCommand(JavaPlugin plugin) {
        super(plugin, BingoManager.class);
    }

    @Override
    protected String gameName() {
        return "Bingo";
    }

    @Override
    protected BingoManager createGame() {
        return new BingoManager(plugin);
    }
}
