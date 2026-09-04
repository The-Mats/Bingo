package me.mats.common.game.ingame;

import me.mats.common.game.GameManager;
import me.mats.common.game.GameState;
import me.mats.common.game.ingame.abilities.Abilities;

// Thin common base for the "actually playing" state: just enough (an ability roster + which
// ItemLists difficulty this round draws from) for the generic ability listeners to operate
// against any game's ingame state. Everything else about what "ingame" means (bingo's grid,
// or whatever another game tracks) is entirely up to the concrete subclass.
public abstract class IngameState<M extends GameManager<?>> extends GameState<M> {

    protected Abilities abilities = new Abilities();
    protected ItemLists.ListType setting;

    public Abilities getAbilities() {
        return abilities;
    }

    public ItemLists.ListType getSetting() {
        return setting;
    }
}
