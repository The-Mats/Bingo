package me.mats.common;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import me.mats.bingo.message.BingoMessage;
import me.mats.common.enums.Color;
import me.mats.streak.message.StreakMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

// The server-list MOTD: which games this suite hosts, and which exact Minecraft version to
// connect with. The version is spelled out fully (not just pulled from plugin.yml's api-version,
// which only supports major.minor - e.g. "1.20", not "1.20.2") because the item pools this plugin
// relies on are sensitive to the exact patch version.
public class ServerListPingListener implements Listener {

    private static final String MINECRAFT_VERSION = "1.20.2";

    // Minecraft's default font isn't monospaced, so centering the MOTD means padding with
    // spaces based on each line's actual rendered pixel width - the client gives no alignment
    // API of its own. MOTD_WIDTH/SPACE_WIDTH and the per-character widths below are the commonly
    // published metrics for the default.png font (see: gist.github.com/SupaHam/3cacae72c5aea0846cde).
    private static final int MOTD_WIDTH = 270;
    private static final int SPACE_WIDTH = 4;

    @EventHandler
    public void onServerListPing(PaperServerListPingEvent e) {
        String line1Plain = "Play BINGO and STREAK";
        Component line1 = Component.text("Play ", Color.STD_COLOR.getTextColor())
                .append(BingoMessage.BINGO.getComponent())
                .append(Component.text(" and ", Color.STD_COLOR.getTextColor()))
                .append(StreakMessage.STREAK.getComponent())
                .decorate(TextDecoration.BOLD);

        String line2Plain = "Connect using Minecraft " + MINECRAFT_VERSION;
        Component line2 = Component.text("Connect using Minecraft ", Color.SUC_COLOR.getTextColor())
                .append(Component.text(MINECRAFT_VERSION, Color.COMMAND_YELLOW.getTextColor()));

        // A neutral empty root keeps line1's own bold decoration from inheriting down onto its
        // sibling (the newline + line2) through Adventure's parent-to-child style inheritance.
        e.motd(Component.empty()
                .append(center(line1, line1Plain, true))
                .append(Component.newline())
                .append(center(line2, line2Plain, false)));
    }

    private static Component center(Component content, String plainText, boolean bold) {
        int textWidth = textWidth(plainText, bold);
        int spaceWidth = SPACE_WIDTH + (bold ? 1 : 0);
        int padding = Math.max(0, (MOTD_WIDTH - textWidth) / 2 / spaceWidth);
        Component paddingSpaces = Component.text(" ".repeat(padding));
        if (bold) {
            // The padding must actually render bold too, or it only takes up the non-bold 4px
            // per space instead of the 5px this method assumed when sizing it.
            paddingSpaces = paddingSpaces.decorate(TextDecoration.BOLD);
        }
        return paddingSpaces.append(content);
    }

    private static int textWidth(String text, boolean bold) {
        int width = 0;
        for (char c : text.toCharArray()) {
            width += charWidth(c) + (bold ? 1 : 0);
        }
        return width;
    }

    // Pixel width of one character in Minecraft's default font, non-bold. Unlisted characters
    // (the common case - most letters and digits) default to 6.
    private static int charWidth(char c) {
        return switch (c) {
            case ' ' -> SPACE_WIDTH;
            case 'i', 'l', '\'', '.', ',', '!', ':', ';', '|' -> 2;
            case 'I' -> 4;
            case 'f', 'k', 't' -> 5;
            default -> 6;
        };
    }
}
