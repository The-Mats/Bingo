package me.mats.bingo.message;

import me.mats.common.message.Message;
import me.mats.common.message.MessageBuilder;
import net.kyori.adventure.text.Component;

import java.util.List;

// Bingo's own branded chat/UI elements, built on top of the generic me.mats.common.message pieces.
public enum BingoMessage {

    BINGO(MessageBuilder.buildMsg(List.of("B","I","N","G","O"), BingoMessages.BINGO_GRADIENT)),

    BINGO_PREFIX(Message.O_BRACKET.getComponent().append(BINGO.getComponent()).append(Message.C_BRACKET.getComponent()));

    private final Component component;

    BingoMessage(Component component) {
        this.component = component;
    }

    public Component getComponent() {
        return component;
    }

}
