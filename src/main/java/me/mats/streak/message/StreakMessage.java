package me.mats.streak.message;

import me.mats.common.message.MessageBuilder;
import net.kyori.adventure.text.Component;

import java.util.List;

public enum StreakMessage {

    STREAK(MessageBuilder.buildMsg(List.of("S", "T", "R", "E", "A", "K"), StreakMessages.STREAK_GRADIENT));

    private final Component component;

    StreakMessage(Component component) {
        this.component = component;
    }

    public Component getComponent() {
        return component;
    }

}
