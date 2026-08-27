package me.mats.bingo.message;

import me.mats.common.enums.Color;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import java.util.List;

// Bingo's own branded message-building helpers, built on top of the generic
// me.mats.common.message.MessageBuilder.
public class BingoMessages {

    // The blue-to-cyan palette used by the "BINGO" logo (see BingoMessage.BINGO)
    public static final List<Integer> BINGO_GRADIENT = List.of(0x0077b6, 0x0096c7, 0x00b4d8, 0x48cae4, 0x90e0ef);

    public static Component bingo(String msg) {
        return BingoMessage.BINGO_PREFIX.getComponent().append(Component.text(msg, Color.STD_COLOR.getTextColor()));
    }

    public static Component bingo(String msg, TextColor color) {
        return BingoMessage.BINGO_PREFIX.getComponent().append(Component.text(msg, color));
    }

    public static Component bingo(String msg, Color c) {
        return BingoMessage.BINGO_PREFIX.getComponent().append(Component.text(msg, c.getTextColor()));
    }

    public static Component bingo(Component c) {
        return BingoMessage.BINGO_PREFIX.getComponent().append(c);
    }
}
