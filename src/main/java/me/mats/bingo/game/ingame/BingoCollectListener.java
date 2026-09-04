package me.mats.bingo.game.ingame;

import me.mats.bingo.game.BingoTeam;
import me.mats.common.game.ingame.GameItem;
import me.mats.common.game.ingame.ItemCollectListener;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class BingoCollectListener extends ItemCollectListener {

    private final BingoIngameState state;

    public BingoCollectListener(BingoIngameState state) {
        super(state.getManager());
        this.state = state;
    }

    @Override
    protected GameItem getTargetItem(Material material) {
        return state.getBingoItemsMap().get(material);
    }

    @Override
    protected void onItemCollected(Player p, Material material, GameItem item) {
        BingoItem bItem = (BingoItem) item;
        BingoTeam team = state.getManager().getTeam(p);
        team.checkForBingo(bItem.getPosition(), material.toString().toLowerCase());
    }
}
