package me.mats.bingo.game.ingame.abilities;

import me.mats.common.game.ingame.abilities.Abilities;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

// Bingo's two exclusive abilities on top of the generic roster: Thief (steal a win off another
// team's near-complete line) and Gapper (extend your longest line on an enchanted golden apple).
// Both manipulate bingo's field/win logic directly, so they stay here rather than in common.
public class BingoAbilities extends Abilities {

    private final List<Player> thiefAbilityList = new ArrayList<>();
    private final List<Player> gapperAbilityList = new ArrayList<>();

    @Override
    public void replacePlayer(Player oldPlayer, Player newPlayer) {
        super.replacePlayer(oldPlayer, newPlayer);
        for (List<Player> list : List.of(thiefAbilityList, gapperAbilityList)) {
            int idx = list.indexOf(oldPlayer);
            if (idx != -1) {
                list.set(idx, newPlayer);
            }
        }
    }

    @Override
    public List<Player> getCorrespondingList(Material mat) {
        return switch (mat) {
            case LEATHER_HELMET -> thiefAbilityList;
            case GOLDEN_APPLE -> gapperAbilityList;
            default -> super.getCorrespondingList(mat);
        };
    }

    @Override
    public void setInitialAbilities() {
        super.setInitialAbilities();
        for (Player p : thiefAbilityList) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, PotionEffect.INFINITE_DURATION, 0));
        }
        for (Player p : gapperAbilityList) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, PotionEffect.INFINITE_DURATION, 1));
        }
    }

    @Override
    public void setAbilities(Player p) {
        super.setAbilities(p);
        if (thiefAbilityList.contains(p)) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, PotionEffect.INFINITE_DURATION, 0));
        }
        if (gapperAbilityList.contains(p)) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, PotionEffect.INFINITE_DURATION, 1));
        }
    }

    public List<Player> getThiefAbilityList() {
        return thiefAbilityList;
    }

    public List<Player> getGapperAbilityList() {
        return gapperAbilityList;
    }
}
