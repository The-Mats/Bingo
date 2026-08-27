package me.mats.bingo.customInventory;

import me.mats.bingo.game.BingoTeam;
import me.mats.bingo.game.ingame.BingoIngameState;
import me.mats.common.customInventory.AbilitiesInventory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import static me.mats.common.message.MessageBuilder.roman;

import java.util.ArrayList;
import java.util.List;

// Adds bingo's two exclusive abilities (Thief, Gapper) to the generic abilities shop.
public class BingoAbilitiesInventory extends AbilitiesInventory {

    public BingoAbilitiesInventory(BingoIngameState state, BingoTeam bingoTeam) {
        super(bingoTeam, state.getAbilities(), state.getExtraAbilityPoints());

        ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
        ItemMeta meta = helmet.getItemMeta();

        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
        meta.displayName(Component.text("Thief", NamedTextColor.BLUE).decoration(TextDecoration.ITALIC, false).decorate(TextDecoration.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(roman("Level: 0", NamedTextColor.YELLOW));
        lore.add(Component.text(""));
        lore.add(roman("Snatch the Win with the help of other teams", NamedTextColor.GRAY));
        lore.add(roman(" - If only one Item is missing for a Bingo and another Team has this you win", NamedTextColor.GREEN));
        lore.add(roman(" - Invisibility", NamedTextColor.GREEN));
        lore.add(Component.text(""));
        lore.add(roman("LEFT CLICK", NamedTextColor.YELLOW).append(roman(" to gain Ability", NamedTextColor.GRAY)));
        lore.add(roman("RIGHT CLICK", NamedTextColor.YELLOW).append(roman(" to lose Ability", NamedTextColor.GRAY)));
        meta.lore(lore);

        helmet.setItemMeta(meta);
        inventory.setItem(8, helmet);

        ItemStack gap = new ItemStack(Material.GOLDEN_APPLE);
        meta = gap.getItemMeta();

        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.displayName(Component.text("Gapper", NamedTextColor.BLUE).decoration(TextDecoration.ITALIC, false).decorate(TextDecoration.BOLD));
        lore = new ArrayList<>();
        lore.add(roman("Level: 0", NamedTextColor.YELLOW));
        lore.add(Component.text(""));
        lore.add(roman("Use the full Powers of the Enchanted Golden Apple", NamedTextColor.GRAY));
        lore.add(roman(" - If you eat an Enchanted Golden Apple you're biggest Bingo Line gets another item", NamedTextColor.GREEN));
        lore.add(roman(" - Regeneration II", NamedTextColor.GREEN));
        lore.add(Component.text(""));
        lore.add(roman("LEFT CLICK", NamedTextColor.YELLOW).append(roman(" to gain Ability", NamedTextColor.GRAY)));
        lore.add(roman("RIGHT CLICK", NamedTextColor.YELLOW).append(roman(" to lose Ability", NamedTextColor.GRAY)));
        meta.lore(lore);

        gap.setItemMeta(meta);
        inventory.setItem(9, gap);
    }
}
