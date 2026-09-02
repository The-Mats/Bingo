package me.mats.common.game.ingame.abilities;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

import static me.mats.common.message.MessageBuilder.roman;

// The generic ability roster shared by every game: movement, looting, mining, keep-inventory,
// lucky diamonds, easter bunny, time wizard and teleporter. A concrete game can add its own
// extra abilities on top (see BingoAbilities' Thief/Gapper) by overriding replacePlayer,
// getCorrespondingList, setInitialAbilities and setAbilities - calling super first/last as
// appropriate - and adding its own lists + getters.
public class Abilities {

    protected final List<Player> movementAbilityList = new ArrayList<>();
    protected final List<Player> looterAbilityList = new ArrayList<>();
    protected final List<Player> looterAbilityList2 = new ArrayList<>();
    protected final List<Player> minerAbilityList = new ArrayList<>();
    protected final List<Player> minerAbilityList2 = new ArrayList<>();
    protected final List<Player> keepInventoryAbilityList = new ArrayList<>();
    protected final List<Player> luckyDiamondsAbilityList = new ArrayList<>();
    protected final List<Player> easterBunnyAbilityList = new ArrayList<>();
    protected final List<Player> timeWizardAbilityList = new ArrayList<>();
    protected final List<Player> teleporterAbilityList = new ArrayList<>();
    protected final List<Player> endermanAbilityList = new ArrayList<>();
    protected final List<Player> stalkerAbilityList = new ArrayList<>();
    protected final List<Player> sniperAbilityList = new ArrayList<>();
    protected final List<Player> pyroAbilityList = new ArrayList<>();

    // Swaps a stale Player reference (e.g. from before a relog) for the current one in every
    // ability list. A subclass adding its own lists should override and call super first.
    public void replacePlayer(Player oldPlayer, Player newPlayer) {
        for (List<Player> list : List.of(movementAbilityList, looterAbilityList, looterAbilityList2, minerAbilityList, minerAbilityList2,
                keepInventoryAbilityList, luckyDiamondsAbilityList, easterBunnyAbilityList, timeWizardAbilityList, teleporterAbilityList)) {
            int idx = list.indexOf(oldPlayer);
            if (idx != -1) {
                list.set(idx, newPlayer);
            }
        }
    }

    // The "level 2" upgrade list for an ability's item Material, if it has one. Null otherwise.
    public List<Player> getOtherList(Material mat) {
        return switch (mat) {
            case GOLDEN_SWORD -> looterAbilityList2;
            case GOLDEN_PICKAXE -> minerAbilityList2;
            default -> null;
        };
    }

    // The ability list an item Material in the abilities GUI corresponds to. Null if it isn't
    // one of this game's abilities. A subclass adding its own abilities should override and
    // fall back to super for anything it doesn't recognize.
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
            case ENDER_PEARL -> endermanAbilityList;
            case SPYGLASS -> stalkerAbilityList;
            case BOW -> sniperAbilityList;
            case FIRE_CHARGE -> pyroAbilityList;
            default -> null;
        };
    }

    // Applies every currently-held ability's persistent effects to everyone who has it. Called
    // once players are released from the spawn countdown. A subclass adding its own abilities
    // should override and call super too.
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
        for (Player p : stalkerAbilityList) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, PotionEffect.INFINITE_DURATION, 0));

            ItemStack spyglass = new ItemStack(Material.SPYGLASS);
            ItemMeta spyglassMeta = spyglass.getItemMeta();
            spyglassMeta.setUnbreakable(true);
            spyglassMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            spyglass.setItemMeta(spyglassMeta);
            p.getInventory().setItem(0, spyglass);

            p.getInventory().setItem(1, new ItemStack(Material.WANDERING_TRADER_SPAWN_EGG, 5));
        }
        for (Player p : sniperAbilityList) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, PotionEffect.INFINITE_DURATION, 0));

            ItemStack bow = new ItemStack(Material.BOW);
            ItemMeta bowMeta = bow.getItemMeta();
            bowMeta.setUnbreakable(true);
            bowMeta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
            bowMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            bow.setItemMeta(bowMeta);
            p.getInventory().addItem(bow);

            ItemStack arrow = new ItemStack(Material.ARROW);
            ItemMeta arrowMeta = arrow.getItemMeta();
            arrowMeta.setUnbreakable(true);
            arrowMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            arrow.setItemMeta(arrowMeta);
            p.getInventory().addItem(arrow);
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
        for (Player p : pyroAbilityList) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, PotionEffect.INFINITE_DURATION, 0));
        }
    }

    // Re-applies whatever abilities a single player currently holds (e.g. after a respawn).
    // A subclass adding its own abilities should override and call super too.
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
        if (stalkerAbilityList.contains(p)) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, PotionEffect.INFINITE_DURATION, 0));
        }
        if (sniperAbilityList.contains(p)) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, PotionEffect.INFINITE_DURATION, 0));
        }
        if (pyroAbilityList.contains(p)) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, PotionEffect.INFINITE_DURATION, 0));
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

    public List<Player> getLooterAbilityList2() {
        return looterAbilityList2;
    }

    public List<Player> getMinerAbilityList2() {
        return minerAbilityList2;
    }

    public List<Player> getEndermanAbilityList() { return endermanAbilityList; }

    public List<Player> getStalkerAbilityList() { return stalkerAbilityList; }

    public List<Player> getSniperAbilityList() { return sniperAbilityList; }

    public List<Player> getPyroAbilityList() { return pyroAbilityList; }
}
