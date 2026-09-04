package me.mats.common.message;

import me.mats.common.enums.Color;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.List;

// Generic message-building helpers shared by every game. A game that wants its own
// branded prefix (bingo's "[BINGO]", etc.) adds a small class of its own that composes
// these instead of duplicating them.
public class MessageBuilder {

    // Colors the whole text as a single color, sampled from the given control colors at phase (0-1).
    // Calling this again each tick with an advancing phase makes the text's color cycle over time.
    public static Component pulse(String text, List<Integer> colors, float phase) {
        return Component.text(text, TextColor.color(interpolateColor(colors, phase))).decorate(TextDecoration.BOLD);
    }

    private static int interpolateColor(List<Integer> colors, float t) {
        int segments = colors.size() - 1;
        float scaled = t * segments;
        int idx = Math.min((int) scaled, segments - 1);
        float localT = scaled - idx;

        int c1 = colors.get(idx);
        int c2 = colors.get(idx + 1);

        int r = Math.round(((c1 >> 16) & 0xFF) * (1 - localT) + ((c2 >> 16) & 0xFF) * localT);
        int g = Math.round(((c1 >> 8) & 0xFF) * (1 - localT) + ((c2 >> 8) & 0xFF) * localT);
        int b = Math.round((c1 & 0xFF) * (1 - localT) + (c2 & 0xFF) * localT);

        return (r << 16) | (g << 8) | b;
    }

    public static Component roman(String msg, TextColor c) {
       return Component.text(msg, c).decoration(TextDecoration.ITALIC, false);
    }

    public static Component error(String msg) {
        return Message.ERROR_PREFIX.getComponent().append(Component.text(msg, TextColor.color(Color.STD_COLOR.getColorCode())));
    }

    public static Component command(String msg, TextColor c) {
        return Message.COMMAND_PREFIX.getComponent().append(Component.text(msg, c));
    }

    public static Component buildMsg(List<String> sList, List<Integer> iList) {
        Component res = Component.empty();
        if (sList.size() == iList.size()) {
            for (int i = 0; i < sList.size(); i++) {
                res = res.append(Component.text(sList.get(i),TextColor.color(iList.get(i))));
            }
            return res;
        }
        return null;
    }

    public static String capitalize(String string) {
        String res = Character.toString(Character.toUpperCase(string.charAt(0)));
        boolean cap = false;

        for (int i = 1; i < string.length(); i++) {
            if (cap) {
                // Capitalize
                res = res.concat(Character.toString(Character.toUpperCase(string.charAt(i))));
                cap = false;
            } else {
                res = res.concat(Character.toString(string.charAt(i)));

                // Check if new Word
                if (string.charAt(i) == ' ' && i+1 < string.length()) {
                    cap = true;
                }
            }
        }

        return res;
    }
}
