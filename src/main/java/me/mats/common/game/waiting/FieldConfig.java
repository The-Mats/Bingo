package me.mats.common.game.waiting;

import me.mats.common.game.ingame.ItemLists;

// Opt-in: a waiting state whose game has a "field" (item board/pool) configurable by size and
// difficulty/type before the game starts. Implement this only if the generic "field size/type"
// commands should apply - a game with no such concept simply doesn't implement it.
public interface FieldConfig {

    ItemLists.ListType getSetting();

    void setSetting(ItemLists.ListType setting);

    int getSize();

    void setSize(int size);
}
