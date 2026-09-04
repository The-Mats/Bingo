package me.mats.common.command;

import de.tr7zw.nbtapi.NBTFile;
import me.mats.common.message.MessageBuilder;
import me.mats.common.world.PlayerDataNBTWriter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PlayerDataCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player p) {
            if (args.length != 2) {
                p.sendMessage(MessageBuilder.error("Please provide 2 Arguments"));
            } else {
                Player p2 = Bukkit.getPlayer(args[0]);

                if (p2 == null) {
                    p.sendMessage(MessageBuilder.error("Please provide a valid Player"));
                } else {
                    World w = Bukkit.getWorld(args[1]);

                    if (w == null) {
                        p.sendMessage(MessageBuilder.error("Please provide a valid World"));
                    } else {
                        try {
                            NBTFile nbtFile = new NBTFile(new File("ServerData/PlayerData/" + p2.getUniqueId() + ".dat"));
                            PlayerDataNBTWriter.writePlayerData(nbtFile.getOrCreateCompound(w.getName()), p2);
                            nbtFile.save();
                        } catch (IOException ex) {
                            Bukkit.getLogger().warning("Error saving PlayerData from" + p2.getName());
                        }
                    }
                }
            }
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
            List<String> tabComplete = new ArrayList<>();
            if (args.length == 1) {
                for(Player p : Bukkit.getOnlinePlayers()) {
                    tabComplete.add(p.getName());
                }
            } else if (args.length == 2) {
                for(World world : Bukkit.getWorlds()) {
                    tabComplete.add(world.getName());
                }
            }

            return tabComplete;
        }
}
