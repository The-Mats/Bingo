package me.mats.bingo.message;

import me.mats.bingo.enums.Color;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;

public enum Message {

    BINGO(MessageBuilder.buildMsg(List.of("B","I","N","G","O"), MessageBuilder.BINGO_GRADIENT)),
    PLAYERS(MessageBuilder.buildMsg(List.of("P","L","A","Y","E","R","S"), List.of(0xf2f270, 0xc3f891, 0x9cf9b6, 0x85f6d7, 0x88f0ed, 0x9ee7f6 , 0xb8def2))),

    O_BRACKET(Component.text("[", NamedTextColor.DARK_GRAY)),
    C_BRACKET(Component.text("] ", NamedTextColor.DARK_GRAY)),

    ERROR_PREFIX(O_BRACKET.getComponent().append(Component.text("ERROR", Color.ERROR_RED.getTextColor())).append(C_BRACKET.getComponent())),
    COMMAND_PREFIX(O_BRACKET.getComponent().append(Component.text("COMMAND", Color.COMMAND_YELLOW.getTextColor())).append(C_BRACKET.getComponent())),

    BINGO_PREFIX(O_BRACKET.getComponent().append(BINGO.getComponent()).append(C_BRACKET.getComponent()));



    private Component component;

    Message(Component component) {
        this.component = component;
    }


    public Component getComponent() {
        return component;
    }


}
