package me.mats.bingo.game.ingame.spawn;

import me.mats.bingo.game.ingame.BingoIngameState;
import me.mats.common.game.ingame.spawn.SpawnListener;
import org.bukkit.entity.Player;

public class BingoSpawnListener extends SpawnListener {

    private final BingoIngameState state;

    public BingoSpawnListener(BingoIngameState state) {
        super(state.getManager());
        this.state = state;
    }

    @Override
    protected boolean shouldKeepInventory(Player p) {
        return state.getAbilities().getKeepInventoryAbilityList().contains(p);
    }

    @Override
    protected void onPlayerRespawned(Player p) {
        state.getAbilities().setAbilities(p);
        state.getAbilities().grantRespawnItems(p);
    }
}
