package me.mats.bingo.game.finished;

import me.mats.bingo.game.BingoManager;
import me.mats.bingo.game.BingoTeam;
import me.mats.common.game.finished.FinishedState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class BingoFinishedState extends FinishedState<BingoManager> {

    // Kept around only so a relogging player can have the winning field re-sent to them
    private final BingoTeam winner;
    private final List<String> winField;
    private final List<ItemStack> fieldItems;
    private final float[][] fieldPositions;

    public BingoFinishedState(BingoManager manager, BingoTeam winner, List<String> winField, List<ItemStack> fieldItems, float[][] fieldPositions) {
        super(manager, winner);
        this.winner = winner;
        this.winField = winField;
        this.fieldItems = fieldItems;
        this.fieldPositions = fieldPositions;
    }

    @Override
    public void onPlayerReconnect(Player oldPlayer, Player newPlayer) {
        winner.getAdvancement().sendFinalField(List.of(newPlayer), new ArrayList<>(), winField, fieldItems, fieldPositions);
    }
}
