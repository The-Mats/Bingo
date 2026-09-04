package me.mats.common.message;

import me.mats.common.enums.Color;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;

// Generic chat/UI chrome shared by every game. Brand-specific wordmarks/prefixes
// (e.g. bingo's "BINGO" logo) belong on that game's own message class instead.
public enum Message {

    O_BRACKET(Component.text("[", NamedTextColor.DARK_GRAY)),
    C_BRACKET(Component.text("] ", NamedTextColor.DARK_GRAY)),

    ERROR_PREFIX(O_BRACKET.getComponent().append(Component.text("ERROR", Color.ERROR_RED.getTextColor())).append(C_BRACKET.getComponent())),
    COMMAND_PREFIX(O_BRACKET.getComponent().append(Component.text("COMMAND", Color.COMMAND_YELLOW.getTextColor())).append(C_BRACKET.getComponent())),

    // Server-wide lobby chrome (not any one game's branding)
    PLAYERS(MessageBuilder.buildMsg(List.of("P","L","A","Y","E","R","S"), List.of(0xf2f270, 0xc3f891, 0x9cf9b6, 0x85f6d7, 0x88f0ed, 0x9ee7f6 , 0xb8def2)));

    private final Component component;

    Message(Component component) {
        this.component = component;
    }

    public Component getComponent() {
        return component;
    }

}
