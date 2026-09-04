package me.mats.bingo.command;

import me.mats.bingo.Bingo;
import me.mats.bingo.game.BingoManager;
import me.mats.common.command.GameCommand;

public class BingoCommand extends GameCommand<BingoManager> {

    public BingoCommand(Bingo plugin) {
        super(plugin, BingoManager.class);
    }

    @Override
    protected String gameName() {
        return "Bingo";
    }

    @Override
    protected BingoManager createGame() {
        return new BingoManager((Bingo) plugin);
    }
}
