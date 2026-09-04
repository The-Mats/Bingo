package me.mats.common.command;

import me.mats.common.enums.Color;
import me.mats.common.message.MessageBuilder;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class WorldTPCommand implements CommandExecutor, TabCompleter {
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


                        /*if (p.getWorld() != w) {
                            try {
                                NBTFile nbtFile = new NBTFile(new File("ServerData/PlayerData/"+p.getUniqueId()+".dat"));
                                PlayerDataNBTWriter.writePlayerData(nbtFile.getOrCreateCompound(p.getWorld().getName()), p);
                                PlayerDataNBTWriter.readPlayerData(nbtFile.getCompound(w.getName()), p);
                                nbtFile.save();

                            } catch (IOException ex) {
                                Bukkit.getLogger().warning("Error loading or saving PlayerData from"+p.getName());
                            }
                        }*/


                        p2.teleport(w.getSpawnLocation());

                        p.sendMessage(MessageBuilder.command("Teleported "+p2.getName()+" to "+w.getName(), TextColor.color(Color.SUC_COLOR.getColorCode())));
                        if (p != p2) {
                            p2.sendMessage(MessageBuilder.command("You were teleported to "+w.getName()+" by "+p.getName(), TextColor.color(Color.SUC_COLOR.getColorCode())));
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

