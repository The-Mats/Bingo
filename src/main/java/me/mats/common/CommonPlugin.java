package me.mats.common;

import me.mats.common.command.PlayerDataCommand;
import me.mats.common.command.WaitingCountdownCommand;
import me.mats.common.command.WorldTPCommand;
import me.mats.common.customInventory.CustomInventoryListener;
import me.mats.common.game.waiting.WaitingWorld;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

// Shared plugin bootstrap: the generic listeners/commands every game-hosting plugin needs
// (lobby scoreboard/chat, custom-inventory click routing, worldtp/playerdata/countdown) plus
// the shared "kick everyone, free pooled worlds" shutdown. A concrete plugin (Bingo, ...)
// fills in its own game command(s) and its own game worlds to clean up.
public abstract class CommonPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("Starting Plugin...");

        getServer().getPluginManager().registerEvents(new GeneralListener(this), this);
        getServer().getPluginManager().registerEvents(new CustomInventoryListener(), this);

        WorldTPCommand worldTPCommand = new WorldTPCommand();
        Objects.requireNonNull(getCommand("worldtp")).setExecutor(worldTPCommand);
        Objects.requireNonNull(getCommand("worldtp")).setTabCompleter(worldTPCommand);

        PlayerDataCommand playerDataCommand = new PlayerDataCommand();
        Objects.requireNonNull(getCommand("playerdata")).setExecutor(playerDataCommand);
        Objects.requireNonNull(getCommand("playerdata")).setTabCompleter(playerDataCommand);

        WaitingCountdownCommand waitingCountdownCommand = new WaitingCountdownCommand();
        Objects.requireNonNull(getCommand("countdown")).setExecutor(waitingCountdownCommand);
        Objects.requireNonNull(getCommand("countdown")).setTabCompleter(waitingCountdownCommand);

        registerGameCommands();
    }

    // Hook: register whatever game-specific command(s) this plugin adds (e.g. /bingo).
    protected abstract void registerGameCommands();

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

        cleanupGameWorlds();

        GeneralListener.getStandardTeam().unregister();
        GeneralListener.getAdminTeam().unregister();
    }

    // Hook: delete whatever game worlds this plugin's game(s) left behind.
    protected abstract void cleanupGameWorlds();
}
