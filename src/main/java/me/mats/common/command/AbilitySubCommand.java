package me.mats.common.command;

import me.mats.common.game.GameManager;
import me.mats.common.game.waiting.AbilityConfig;
import me.mats.common.message.MessageBuilder;
import org.bukkit.entity.Player;

import java.util.List;

class AbilitySubCommand<M extends GameManager<?>> implements SubCommand<M> {

    @Override
    public void execute(GameCommand<M> cmd, Player p, String[] args) {
        M game = cmd.getGame(p);
        if (game == null) {
            p.sendMessage(MessageBuilder.error("You're not playing " + cmd.gameName()));
            return;
        }
        if (!(game.getGameState() instanceof AbilityConfig ac)) {
            p.sendMessage(MessageBuilder.error("The " + cmd.gameName() + " is in the wrong State"));
            return;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("points")) {
            try {
                int points = Integer.parseInt(args[1]);
                if (points >= 0) {
                    ac.setExtraAbilityPoints(points);
                    p.sendMessage(game.brandedStatus("Changed the extra Ability points to §e" + points));
                } else {
                    p.sendMessage(MessageBuilder.error("Please enter a positive Number"));
                }
            } catch (NumberFormatException e) {
                p.sendMessage(MessageBuilder.error("Please enter a valid Number"));
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("time")) {
            try {
                int time = Integer.parseInt(args[1]);
                if (time > 10) {
                    ac.setSpawnTime(time);
                    p.sendMessage(game.brandedStatus("Changed the spawn time to §e" + time));
                } else {
                    p.sendMessage(MessageBuilder.error("Please enter a Number bigger than 10"));
                }
            } catch (NumberFormatException e) {
                p.sendMessage(MessageBuilder.error("Please enter a valid Number"));
            }
        } else {
            p.sendMessage(MessageBuilder.error("Wrong usage"));
        }
    }

    @Override
    public List<String> tabComplete(GameCommand<M> cmd, Player p, String[] args) {
        if (args.length == 1) {
            return List.of("points", "time");
        }
        return List.of();
    }

    @Override
    public boolean isAvailable(GameCommand<M> cmd, Player p) {
        M game = cmd.getGame(p);
        return game != null && game.getGameState() instanceof AbilityConfig;
    }
}
