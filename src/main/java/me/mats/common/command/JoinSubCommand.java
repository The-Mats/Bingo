package me.mats.common.command;

import me.mats.common.game.GameManager;
import me.mats.common.message.MessageBuilder;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

class JoinSubCommand<M extends GameManager<?>> implements SubCommand<M> {

    @Override
    public void execute(GameCommand<M> cmd, Player p, String[] args) {
        if (args.length != 1) {
            p.sendMessage(MessageBuilder.error("Wrong usage"));
            return;
        }
        if (cmd.getGame(p) != null) {
            p.sendMessage(MessageBuilder.error("Already playing " + cmd.gameName()));
            return;
        }
        for (M g : cmd.getRunningGames()) {
            if (args[0].equalsIgnoreCase(g.getName())) {
                g.addPlayer(p);
                return;
            }
        }
        p.sendMessage(MessageBuilder.error(cmd.gameName() + " Game doesn't exist"));
    }

    @Override
    public List<String> tabComplete(GameCommand<M> cmd, Player p, String[] args) {
        if (args.length == 1) {
            List<String> names = new ArrayList<>();
            for (M g : cmd.getRunningGames()) {
                names.add(g.getName());
            }
            return names;
        }
        return List.of();
    }

    @Override
    public boolean isAvailable(GameCommand<M> cmd, Player p) {
        return !cmd.getRunningGames().isEmpty();
    }

    @Override
    public boolean requiresOp() {
        return false;
    }
}
