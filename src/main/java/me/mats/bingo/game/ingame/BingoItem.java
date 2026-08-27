package me.mats.bingo.game.ingame;

import me.mats.common.game.ingame.GameItem;

public class BingoItem extends GameItem {
    private int[] position;

    public int[] getPosition() {
        return position;
    }

    public void setPosition(int[] position) {
        this.position = position;
    }
}
