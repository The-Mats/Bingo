package me.mats.common.enums;


import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public enum Color {
    // Standard Colors
    WHITE(0xFFFFFF),
    ERROR_RED(0xA82224),
    COMMAND_YELLOW(0xE9EF09),
    STD_COLOR(0x8A8A8A),
    SUC_COLOR(0x2BF200),

    // Team Colors
    T_RED(0xf64152),
    T_BLUE(0x03a9f4),
    T_GREEN(0x2d862e),
    T_YELLOW(0xfccb00),
    T_ORANGE(0xfe9200),
    T_CYAN(0x4DD0E1),
    T_LIME(0x8be14d),
    T_PURPLE(0x9940BF),
    T_PINK(0xf387aa),
    PLAYER(0x4fcb2e),
    ADMIN(0xec4444),
    OVERWORLD(0x549365),
    NETHER(0xa43e3e),
    END(0xf4eec2);

     private final int colorCode;

     Color(int colorCode) {
         this.colorCode = colorCode;
     }

    public int getColorCode() {
        return colorCode;
    }

    // Get the Enum by giving String
    public static Color getColorByName(String color) {
         try {
             return Color.valueOf(color.toUpperCase());
         } catch (IllegalArgumentException e) {
             return Color.WHITE;
         }
    }

    public TextColor getTextColor() {
         return TextColor.color(colorCode);
    }

}
