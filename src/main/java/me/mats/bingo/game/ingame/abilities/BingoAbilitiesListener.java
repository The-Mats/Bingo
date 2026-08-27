package me.mats.bingo.game.ingame.abilities;

import me.mats.bingo.game.ingame.BingoIngameState;
import me.mats.common.customInventory.TeleportInventory;
import me.mats.common.customInventory.CustomInventoryManager;
import me.mats.common.game.ingame.abilities.AbilitiesListener;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemConsumeEvent;

// Bingo's one exclusive ability handler on top of the generic set: Gapper (eating an enchanted
// golden apple extends your team's longest bingo line). Also wires the Teleporter ability's
// "open" hook to bingo's own TeleportInventory GUI.
public class BingoAbilitiesListener extends AbilitiesListener {

    private final BingoIngameState bingoState;

    public BingoAbilitiesListener(BingoIngameState state) {
        super(state);
        this.bingoState = state;
    }

    @Override
    protected void openTeleporter(Player p) {
        CustomInventoryManager.openInventory(p, new TeleportInventory(bingoState.getManager(), p));
    }

    @EventHandler
    public void onGappleConsume(PlayerItemConsumeEvent e) {
        Player p = e.getPlayer();
        if (e.getItem().getType() == Material.ENCHANTED_GOLDEN_APPLE && bingoState.getAbilities().getGapperAbilityList().contains(p)) {
            bingoState.getManager().getTeam(p).extendLongestBingoLine();
        }
    }
}
