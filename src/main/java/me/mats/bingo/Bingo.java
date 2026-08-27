package me.mats.bingo;

import me.mats.bingo.command.BingoCommand;
import me.mats.bingo.command.WaitingCountdownCommand;
import me.mats.bingo.game.BingoManager;
import me.mats.bingo.command.PlayerDataCommand;
import me.mats.bingo.command.WorldTPCommand;
import me.mats.bingo.customInventory.CustomInventoryListener;
import me.mats.bingo.game.waiting.WaitingWorld;
import me.mats.bingo.world.WorldManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.Objects;

public final class Bingo extends JavaPlugin {


    @Override
    public void onEnable() {
        // Create a world
        WorldManager wm = new WorldManager(this);

        // Plugin startup logic
        getLogger().info("Starting Plugin...");

        getServer().getPluginManager().registerEvents(new GeneralListener(), this);
        getServer().getPluginManager().registerEvents(new CustomInventoryListener(), this);


        // WorldTP command
        WorldTPCommand worldTPCommand = new WorldTPCommand();
        Objects.requireNonNull(getCommand("worldtp")).setExecutor(worldTPCommand);
        Objects.requireNonNull(getCommand("worldtp")).setTabCompleter(worldTPCommand);

        // Bingo command
        BingoCommand bingoCommand = new BingoCommand(this);
        Objects.requireNonNull(getCommand("bingo")).setExecutor(bingoCommand);
        Objects.requireNonNull(getCommand("bingo")).setTabCompleter(bingoCommand);

        // Player Data command
        PlayerDataCommand playerDataCommand = new PlayerDataCommand();
        Objects.requireNonNull(getCommand("playerdata")).setExecutor(playerDataCommand);
        Objects.requireNonNull(getCommand("playerdata")).setTabCompleter(playerDataCommand);

        // WaitingCountdown command
        WaitingCountdownCommand waitingCountdownCommand = new WaitingCountdownCommand();
        Objects.requireNonNull(getCommand("countdown")).setExecutor(waitingCountdownCommand);
        Objects.requireNonNull(getCommand("countdown")).setTabCompleter(waitingCountdownCommand);
    }

    @Override
    public void onDisable() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.teleport(Bukkit.getWorld("world").getSpawnLocation());
            p.kick(Component.text("Restart"));
        }

        for (WaitingWorld ww : WaitingWorld.getWorlds()) {
            ww.freeAll();
        }
        WaitingWorld.setWorlds(null);

        // Unload Worlds
        // Plugin shutdown logic

        // Unload and delete all leftover Bingo worlds
        for (World w : BingoManager.getWorlds()) {
            WorldManager.deleteWorld(w);
        }

        GeneralListener.getStandardTeam().unregister();
        GeneralListener.getAdminTeam().unregister();
    }

    public void startBingo() {
        getLogger().info("Starting the Bingo!");
        BingoManager bingoManager = new BingoManager(this);
    }

    public static Bingo getInstance() {
        return getPlugin(Bingo.class);
    }

}
