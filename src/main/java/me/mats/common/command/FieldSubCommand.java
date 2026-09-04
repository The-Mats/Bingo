package me.mats.common.command;

import me.mats.common.game.GameManager;
import me.mats.common.game.ingame.ItemLists;
import me.mats.common.game.ingame.RegeneratableField;
import me.mats.common.game.waiting.FieldConfig;
import me.mats.common.message.MessageBuilder;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

class FieldSubCommand<M extends GameManager<?>> implements SubCommand<M> {

    @Override
    public void execute(GameCommand<M> cmd, Player p, String[] args) {
        M game = cmd.getGame(p);
        if (game == null) {
            p.sendMessage(MessageBuilder.error("You're not playing " + cmd.gameName()));
            return;
        }
        if (game.getGameState() instanceof FieldConfig fc) {
            if (args.length == 2 && args[0].equalsIgnoreCase("size")) {
                try {
                    int size = Integer.parseInt(args[1]);
                    fc.setSize(size);
                    p.sendMessage(game.brandedStatus("Changed the Size to §e" + size));
                } catch (NumberFormatException e) {
                    p.sendMessage(MessageBuilder.error("Please enter a valid Size"));
                }
            } else if (args.length == 2 && args[0].equalsIgnoreCase("type")) {
                try {
                    ItemLists.ListType type = ItemLists.ListType.valueOf(args[1].toUpperCase());
                    fc.setSetting(type);
                    p.sendMessage(game.brandedStatus("Changed the Type to §e" + type));
                } catch (IllegalArgumentException e) {
                    p.sendMessage(MessageBuilder.error("Please enter a valid Type"));
                }
            } else {
                p.sendMessage(MessageBuilder.error("Wrong usage"));
            }
        } else if (game.getGameState() instanceof RegeneratableField rf) {
            if (args.length == 1 && args[0].equalsIgnoreCase("new")) {
                rf.regenerateField();
                p.sendMessage(game.brandedStatus("Created a new " + cmd.gameName() + " Field"));
            } else {
                p.sendMessage(MessageBuilder.error("Wrong usage"));
            }
        } else {
            p.sendMessage(MessageBuilder.error("The " + cmd.gameName() + " is in the wrong State"));
        }
    }

    @Override
    public List<String> tabComplete(GameCommand<M> cmd, Player p, String[] args) {
        M game = cmd.getGame(p);
        if (game == null) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        if (args.length == 1) {
            if (game.getGameState() instanceof FieldConfig) {
                result.add("size");
                result.add("type");
            }
            if (game.getGameState() instanceof RegeneratableField) {
                result.add("new");
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("type") && game.getGameState() instanceof FieldConfig) {
            for (ItemLists.ListType type : ItemLists.ListType.values()) {
                result.add(type.name());
            }
        }
        return result;
    }

    @Override
    public boolean isAvailable(GameCommand<M> cmd, Player p) {
        M game = cmd.getGame(p);
        return game != null && (game.getGameState() instanceof FieldConfig || game.getGameState() instanceof RegeneratableField);
    }
}
