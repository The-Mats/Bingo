package me.mats.common.game.ingame;

import org.bukkit.inventory.meta.ItemMeta;

// A single randomly-drawn item from an ItemLists pool. Holds whatever extra metadata
// distinguishes it from other items sharing the same Material (e.g. which enchant an
// enchanted book rolled, or which potion type). Game-specific tracking (e.g. bingo's
// grid position) belongs on a subclass.
public class GameItem {
    private ItemMeta meta = null;

    public void setMeta(ItemMeta meta) {
        this.meta = meta;
    }

    public ItemMeta getMeta() {
        return meta;
    }
}
