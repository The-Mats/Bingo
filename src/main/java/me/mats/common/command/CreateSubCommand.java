package me.mats.common.command;

import me.mats.common.enums.Color;
import me.mats.common.game.GameManager;
import me.mats.common.message.MessageBuilder;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;

class CreateSubCommand<M extends GameManager<?>> implements SubCommand<M> {

    @Override
    public void execute(GameCommand<M> cmd, Player p, String[] args) {
        if (args.length != 0) {
            p.sendMessage(MessageBuilder.error("Wrong usage"));
            return;
        }
        p.sendMessage(MessageBuilder.command("Starting " + cmd.gameName() + "...", TextColor.color(Color.SUC_COLOR.getColorCode())));
        cmd.createGame();
    }
}
