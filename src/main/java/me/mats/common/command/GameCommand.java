package me.mats.common.command;

import me.mats.common.enums.Color;
import me.mats.common.game.GameManager;
import me.mats.common.game.ingame.ItemLists;
import me.mats.common.game.ingame.RegeneratableField;
import me.mats.common.game.waiting.AbilityConfig;
import me.mats.common.game.waiting.FieldConfig;
import me.mats.common.message.MessageBuilder;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

// Generic "/<game> create|join|leave|end|field|ability" command shared by every team game.
// create/join/leave/end always work against any GameManager. "field size/type" and "field new"
// only show up for a game whose waiting/ingame state opts in via FieldConfig/RegeneratableField;
// "ability points/time" only for one that opts in via AbilityConfig - a game implementing none
// of those still gets a fully working create/join/leave/end command for free, nothing is forced.
public abstract class GameCommand<M extends GameManager<?>> implements CommandExecutor, TabCompleter {

    protected final JavaPlugin plugin;
    private final Class<M> gameClass;

    protected GameCommand(JavaPlugin plugin, Class<M> gameClass) {
        this.plugin = plugin;
        this.gameClass = gameClass;
    }

    // Hook: this game's display name used in generic messages ("Bingo", "Streak").
    protected abstract String gameName();

    // Hook: construct and start a brand-new game of this type.
    protected abstract M createGame();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("create")) {
            p.sendMessage(MessageBuilder.command("Starting " + gameName() + "...", TextColor.color(Color.SUC_COLOR.getColorCode())));
            createGame();

        } else if (args.length == 2 && args[0].equalsIgnoreCase("join")) {
            if (GameManager.getGameOfType(p, gameClass) == null) {
                M found = null;
                for (M g : GameManager.getGamesOfType(gameClass)) {
                    if (args[1].equalsIgnoreCase(g.getName())) {
                        found = g;
                        break;
                    }
                }
                if (found != null) {
                    found.addPlayer(p);
                } else {
                    p.sendMessage(MessageBuilder.error(gameName() + " Game doesn't exist"));
                }
            } else {
                p.sendMessage(MessageBuilder.error("Already playing " + gameName()));
            }

        } else if (args.length == 1 && args[0].equalsIgnoreCase("leave")) {
            M game = GameManager.getGameOfType(p, gameClass);
            if (game != null) {
                game.removePlayer(p);
                p.sendMessage(game.brandedStatus("You left the " + gameName()));
            } else {
                p.sendMessage(MessageBuilder.error("You're not playing " + gameName()));
            }

        } else if (args.length == 1 && args[0].equalsIgnoreCase("end")) {
            M game = GameManager.getGameOfType(p, gameClass);
            if (game != null) {
                game.sendChat(game.brandedStatus("The " + gameName() + " was ended"));
                game.getGameState().abort();
            } else {
                p.sendMessage(MessageBuilder.error("You're not playing " + gameName()));
            }

        } else if (args.length >= 2 && args[0].equalsIgnoreCase("field")) {
            handleField(p, args);

        } else if (args.length >= 3 && args[0].equalsIgnoreCase("ability")) {
            handleAbility(p, args);

        } else {
            p.sendMessage(MessageBuilder.error("Wrong usage"));
        }
        return true;
    }

    private void handleField(Player p, String[] args) {
        M game = GameManager.getGameOfType(p, gameClass);
        if (game == null) {
            p.sendMessage(MessageBuilder.error("You're not playing " + gameName()));
            return;
        }
        if (game.getGameState() instanceof FieldConfig fc) {
            if (args[1].equalsIgnoreCase("size") && args.length == 3) {
                try {
                    int size = Integer.parseInt(args[2]);
                    fc.setSize(size);
                    p.sendMessage(game.brandedStatus("Changed the Size to §e" + size));
                } catch (NumberFormatException e) {
                    p.sendMessage(MessageBuilder.error("Please enter a valid Size"));
                }
            } else if (args[1].equalsIgnoreCase("type") && args.length == 3) {
                try {
                    ItemLists.ListType type = ItemLists.ListType.valueOf(args[2].toUpperCase());
                    fc.setSetting(type);
                    p.sendMessage(game.brandedStatus("Changed the Type to §e" + type));
                } catch (IllegalArgumentException e) {
                    p.sendMessage(MessageBuilder.error("Please enter a valid Type"));
                }
            } else {
                p.sendMessage(MessageBuilder.error("Wrong usage"));
            }
        } else if (game.getGameState() instanceof RegeneratableField rf) {
            if (args[1].equalsIgnoreCase("new") && args.length == 2) {
                rf.regenerateField();
                p.sendMessage(game.brandedStatus("Created a new " + gameName() + " Field"));
            } else {
                p.sendMessage(MessageBuilder.error("Wrong usage"));
            }
        } else {
            p.sendMessage(MessageBuilder.error("The " + gameName() + " is in the wrong State"));
        }
    }

    private void handleAbility(Player p, String[] args) {
        M game = GameManager.getGameOfType(p, gameClass);
        if (game == null) {
            p.sendMessage(MessageBuilder.error("You're not playing " + gameName()));
            return;
        }
        if (game.getGameState() instanceof AbilityConfig ac) {
            if (args[1].equalsIgnoreCase("points") && args.length == 3) {
                try {
                    int points = Integer.parseInt(args[2]);
                    if (points >= 0) {
                        ac.setExtraAbilityPoints(points);
                        p.sendMessage(game.brandedStatus("Changed the extra Ability points to §e" + points));
                    } else {
                        p.sendMessage(MessageBuilder.error("Please enter a positive Number"));
                    }
                } catch (NumberFormatException e) {
                    p.sendMessage(MessageBuilder.error("Please enter a valid Number"));
                }
            } else if (args[1].equalsIgnoreCase("time") && args.length == 3) {
                try {
                    int time = Integer.parseInt(args[2]);
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
        } else {
            p.sendMessage(MessageBuilder.error("The " + gameName() + " is in the wrong State"));
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            return null;
        }
        M game = GameManager.getGameOfType(p, gameClass);

        List<String> tabComplete = new ArrayList<>();
        if (args.length == 1) {
            tabComplete.add("create");
            if (!GameManager.getGamesOfType(gameClass).isEmpty()) {
                tabComplete.add("join");
            }
            if (game != null) {
                tabComplete.add("leave");
                tabComplete.add("end");
                if (game.getGameState() instanceof FieldConfig || game.getGameState() instanceof RegeneratableField) {
                    tabComplete.add("field");
                }
                if (game.getGameState() instanceof AbilityConfig) {
                    tabComplete.add("ability");
                }
            }

        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("join")) {
                for (M g : GameManager.getGamesOfType(gameClass)) {
                    tabComplete.add(g.getName());
                }
            } else if (args[0].equalsIgnoreCase("field") && game != null) {
                if (game.getGameState() instanceof FieldConfig) {
                    tabComplete.add("size");
                    tabComplete.add("type");
                }
                if (game.getGameState() instanceof RegeneratableField) {
                    tabComplete.add("new");
                }
            } else if (args[0].equalsIgnoreCase("ability") && game != null && game.getGameState() instanceof AbilityConfig) {
                tabComplete.add("points");
                tabComplete.add("time");
            }

        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("field") && args[1].equalsIgnoreCase("type") && game != null && game.getGameState() instanceof FieldConfig) {
                for (ItemLists.ListType type : ItemLists.ListType.values()) {
                    tabComplete.add(type.name());
                }
            }
        }
        return tabComplete;
    }
}
