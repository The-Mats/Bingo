package me.mats.bingo.game.ingame.abilities;

import me.mats.bingo.customInventory.BingoAbilitiesInventory;
import me.mats.bingo.game.BingoTeam;
import me.mats.bingo.game.ingame.BingoIngameState;
import me.mats.common.customInventory.CustomInventoryManager;
import me.mats.common.game.ingame.abilities.AbilitiesMenuListener;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class BingoAbilitiesMenuListener extends AbilitiesMenuListener {

    private final BingoIngameState bingoState;
    private final Map<BingoTeam, BingoAbilitiesInventory> bingoTeamToGUI;

    public BingoAbilitiesMenuListener(BingoIngameState state) {
        super(state);
        this.bingoState = state;
        bingoTeamToGUI = new HashMap<>(state.getManager().getTeams().size());
        for (BingoTeam bT : state.getManager().getTeams()) {
            bingoTeamToGUI.put(bT, new BingoAbilitiesInventory(state, bT));
        }
    }

    @Override
    protected void openAbilitiesGui(Player p) {
        CustomInventoryManager.openInventory(p, bingoTeamToGUI.get(bingoState.getManager().getTeam(p)));
    }
}
