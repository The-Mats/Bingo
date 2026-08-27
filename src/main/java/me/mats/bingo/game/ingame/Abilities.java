package me.mats.bingo.game.ingame;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

import static me.mats.bingo.message.MessageBuilder.roman;

public class Abilities {

    private final List<Player> movementAbilityList = new ArrayList<>();
    private final List<Player> looterAbilityList = new ArrayList<>();
    private final List<Player> looterAbilityList2 = new ArrayList<>();
    private final List<Player> minerAbilityList = new ArrayList<>();
    private final List<Player> minerAbilityList2 = new ArrayList<>();
    private final List<Player> keepInventoryAbilityList = new ArrayList<>();
    private final List<Player> luckyDiamondsAbilityList = new ArrayList<>();
    private final List<Player> easterBunnyAbilityList = new ArrayList<>();
    private final List<Player> timeWizardAbilityList = new ArrayList<>();
    private final List<Player> teleporterAbilityList = new ArrayList<>();
    private final List<Player> thiefAbilityList = new ArrayList<>();
    private final List<Player> gapperAbilityList = new ArrayList<>();

    // Swaps a stale Player reference (e.g. from before a relog) for the current one in every ability list
    public void replacePlayer(Player oldPlayer, Player newPlayer) {
        for (List<Player> list : List.of(movementAbilityList, looterAbilityList, looterAbilityList2, minerAbilityList, minerAbilityList2,
                keepInventoryAbilityList, luckyDiamondsAbilityList, easterBunnyAbilityList, timeWizardAbilityList, teleporterAbilityList,
                thiefAbilityList, gapperAbilityList)) {
            int idx = list.indexOf(oldPlayer);
            if (idx != -1) {
                list.set(idx, newPlayer);
            }
        }
    }

    public List<Player> getOtherList(Material mat) {
        return switch (mat) {
            case GOLDEN_SWORD -> looterAbilityList2;
            case GOLDEN_PICKAXE -> minerAbilityList2;
            default -> null;
        };
    }

    public List<Player> getCorrespondingList(Material mat) {
        return switch (mat) {
            case FEATHER -> movementAbilityList;
            case GOLDEN_SWORD -> looterAbilityList;
            case GOLDEN_PICKAXE -> minerAbilityList;
            case CHEST_MINECART -> keepInventoryAbilityList;
            case DIAMOND -> luckyDiamondsAbilityList;
            case TURTLE_EGG -> easterBunnyAbilityList;
            case CLOCK -> timeWizardAbilityList;
            case COMPASS -> teleporterAbilityList;
            case LEATHER_HELMET -> thiefAbilityList;
            case GOLDEN_APPLE -> gapperAbilityList;
            default -> null;
        };
    }

    public void setInitialAbilities() {
        for (Player p : movementAbilityList) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, PotionEffect.INFINITE_DURATION, 1));
            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, 1));
            p.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, PotionEffect.INFINITE_DURATION, 0));
        }
        for (Player p : looterAbilityList) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, PotionEffect.INFINITE_DURATION, 0));
        }
        for (Player p : looterAbilityList2) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, PotionEffect.INFINITE_DURATION, 1));
        }
        for (Player p : minerAbilityList) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, PotionEffect.INFINITE_DURATION, 0));
        }
        for (Player p : minerAbilityList2) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, PotionEffect.INFINITE_DURATION, 0));
        }
        for (Player p : timeWizardAbilityList) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, PotionEffect.INFINITE_DURATION, 4));
        }
        for (Player p : thiefAbilityList) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, PotionEffect.INFINITE_DURATION, 0));
        }
        for (Player p : gapperAbilityList) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, PotionEffect.INFINITE_DURATION, 1));
        }

        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.displayName(roman("Teleporter", TextColor.color(0xFCCB00)).decoration(TextDecoration.BOLD, true));
        List<Component> lore = new ArrayList<>();
        lore.add(roman("CLICK", NamedTextColor.YELLOW).append(roman(" to teleport to Teammates", NamedTextColor.GRAY)));
        meta.lore(lore);
        compass.setItemMeta(meta);

        for (Player p : teleporterAbilityList) {
            p.getInventory().setItem(7, compass);
        }
    }

    public void setAbilities(Player p) {
        if (movementAbilityList.contains(p)) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, PotionEffect.INFINITE_DURATION, 1));
            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, 1));
            p.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, PotionEffect.INFINITE_DURATION, 0));
        }
        if (looterAbilityList.contains(p)) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, PotionEffect.INFINITE_DURATION, 0));
        }
        if (looterAbilityList2.contains(p)) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, PotionEffect.INFINITE_DURATION, 1));
        }
        if (minerAbilityList.contains(p)) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, PotionEffect.INFINITE_DURATION, 0));
        }
        if (minerAbilityList2.contains(p)) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, PotionEffect.INFINITE_DURATION, 0));
        }
        if (timeWizardAbilityList.contains(p)) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, PotionEffect.INFINITE_DURATION, 4));
        }
        if (thiefAbilityList.contains(p)) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, PotionEffect.INFINITE_DURATION, 0));
        }
        if (gapperAbilityList.contains(p)) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, PotionEffect.INFINITE_DURATION, 1));
        }
    }

    public List<Player> getMovementAbilityList() {
        return movementAbilityList;
    }

    public List<Player> getLooterAbilityList() {
        return looterAbilityList;
    }

    public List<Player> getMinerAbilityList() {
        return minerAbilityList;
    }

    public List<Player> getKeepInventoryAbilityList() {
        return keepInventoryAbilityList;
    }

    public List<Player> getLuckyDiamondsAbilityList() {
        return luckyDiamondsAbilityList;
    }

    public List<Player> getEasterBunnyAbilityList() {
        return easterBunnyAbilityList;
    }

    public List<Player> getTimeWizardAbilityList() {
        return timeWizardAbilityList;
    }

    public List<Player> getTeleporterAbilityList() {
        return teleporterAbilityList;
    }

    public List<Player> getGapperAbilityList() {
        return gapperAbilityList;
    }

    public List<Player> getThiefAbilityList() {
        return thiefAbilityList;
    }

    public List<Player> getLooterAbilityList2() {
        return looterAbilityList2;
    }

    public List<Player> getMinerAbilityList2() {
        return minerAbilityList2;
    }
}
