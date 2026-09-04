package me.mats.common.command;

import me.mats.common.game.GameManager;
import me.mats.common.message.MessageBuilder;
import org.bukkit.entity.Player;

class EndSubCommand<M extends GameManager<?>> implements SubCommand<M> {

    @Override
    public void execute(GameCommand<M> cmd, Player p, String[] args) {
        if (args.length != 0) {
            p.sendMessage(MessageBuilder.error("Wrong usage"));
            return;
        }
        M game = cmd.getGame(p);
        if (game == null) {
            p.sendMessage(MessageBuilder.error("You're not playing " + cmd.gameName()));
            return;
        }
        game.sendChat(game.brandedStatus("The " + cmd.gameName() + " was ended"));
        game.getGameState().abort();
    }

    @Override
    public boolean isAvailable(GameCommand<M> cmd, Player p) {
        return cmd.getGame(p) != null;
    }
}
