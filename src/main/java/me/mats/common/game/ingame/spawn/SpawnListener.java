package me.mats.common.game.ingame.spawn;

import me.mats.common.enums.Color;
import me.mats.common.game.GameManager;
import me.mats.common.message.Message;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.PortalType;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Generic "just launched off the spawn platform" behaviour shared by every game: elytra glide
// handling, fall-damage/inventory lockout while gliding, respawn re-equip, and portal redirects
// between this game's own main/nether/end worlds. Spawn-structure-specific block protection
// (tied to one game's exact platform geometry) is deliberately NOT here - see each game's own
// protection listener instead.
public class SpawnListener implements Listener {

    protected final GameManager<?> manager;
    private final List<Player> playersWithElytra;
    private final Map<Player,ItemStack> playerToChestplate = new HashMap<>();

    public SpawnListener(GameManager<?> manager) {
        this.manager = manager;
        playersWithElytra = new ArrayList<>(manager.getPlayers());
    }

    // Hook: should this player's chestplate be preserved across a respawn (e.g. a "keep
    // inventory" ability)? False (discard it) by default.
    protected boolean shouldKeepInventory(Player p) {
        return false;
    }

    // Hook: whatever else a concrete game wants to (re-)apply once a player respawns at spawn.
    protected void onPlayerRespawned(Player p) {
    }

    @EventHandler
    public void onReachGround(EntityToggleGlideEvent e) {
        if (e.getEntity() instanceof Player p && playersWithElytra.contains(p) && p.isGliding()) {
            playersWithElytra.remove(p);
            ItemStack item = new ItemStack(Material.AIR);
            if (playerToChestplate.get(p) != null) {
                item = playerToChestplate.remove(p);
            }
            p.getInventory().setChestplate(item);

        }

    }

    @EventHandler
    public void onInventoryChange(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        if (playersWithElytra.contains(p)) {
            e.setCancelled(true);
        }

    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent e) {
        if (playersWithElytra.contains(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player p && playersWithElytra.contains(p)) {
            e.setCancelled(true);
        }
    }

    // Respawn Listener for Elytra, Spawnpoint and effects
    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();
        if (manager.getPlayers().contains(p)) {
            e.setRespawnLocation(manager.getWorld().getSpawnLocation());
            ItemStack elytra = new ItemStack(Material.ELYTRA);
            ItemMeta meta = elytra.getItemMeta();
            if (shouldKeepInventory(p)) {
                playerToChestplate.put(p, p.getInventory().getChestplate());
            }
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            elytra.setItemMeta(meta);
            p.getInventory().setChestplate(elytra);
            playersWithElytra.add(p);

            Bukkit.getScheduler().runTask(manager.getPlugin(), new PlayerListRunnable(p, Color.OVERWORLD, manager.getBoard().getPlayerTeam(p)));
            Bukkit.getScheduler().runTask(manager.getPlugin(), () -> onPlayerRespawned(p));
        }

    }

    @EventHandler
    public void onPortalUse(EntityPortalEvent e) {
        World fromWorld = e.getFrom().getWorld();
        if (fromWorld.getEnvironment() == World.Environment.NORMAL && fromWorld == manager.getWorld()) {
            // This means the Entity is coming from the Main world of this Listener
            PortalType type = e.getPortalType();
            if (type == PortalType.ENDER) {
                e.setTo(new Location(manager.getEndWorld(), 100, 50, 0));
            } else if (type == PortalType.NETHER) {
                e.setTo(new Location(manager.getNetherWorld(), e.getFrom().getBlockX()*8, e.getFrom().getBlockY(), e.getFrom().getBlockZ()*8));
            }
        } else if (fromWorld.getEnvironment() == World.Environment.NETHER && manager.isNetherNotNull() && fromWorld == manager.getNetherWorld()) {
            e.setTo(new Location(manager.getWorld(), (double) e.getFrom().getBlockX()/8, e.getFrom().getBlockY(), (double) e.getFrom().getBlockZ()/8));
        } else if (fromWorld.getEnvironment() == World.Environment.THE_END && manager.isEndNotNull() && fromWorld == manager.getEndWorld()) {
            e.setTo(manager.getWorld().getSpawnLocation());
        }
    }

    @EventHandler
    public void onPortalUse(PlayerPortalEvent e) {
        Player p = e.getPlayer();
        World fromWorld = e.getFrom().getWorld();
        if (fromWorld.getEnvironment() == World.Environment.NORMAL && fromWorld == manager.getWorld()) {
            // This means the Entity is coming from the Main world of this Listener
            Team playerTeam = manager.getBoard().getPlayerTeam(p);


            if (e.getCause() == PlayerTeleportEvent.TeleportCause.END_PORTAL) {
                e.setTo(new Location(manager.getEndWorld(), 100, 50, 0));
                p.playerListName(playerTeam.prefix().append(Component.text(p.getName(), playerTeam.color())).append(Component.text(" ")).append(Message.O_BRACKET.getComponent()).append(Component.text("E", Color.END.getTextColor())).append(Message.C_BRACKET.getComponent()));
            } else if (e.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
                e.setTo(new Location(manager.getNetherWorld(), e.getFrom().getBlockX()*8, e.getFrom().getBlockY(), e.getFrom().getBlockZ()*8));
                p.playerListName(playerTeam.prefix().append(Component.text(p.getName(), playerTeam.color())).append(Component.text(" ")).append(Message.O_BRACKET.getComponent()).append(Component.text("N", Color.NETHER.getTextColor())).append(Message.C_BRACKET.getComponent()));
            }
        } else if (fromWorld.getEnvironment() == World.Environment.NETHER && manager.isNetherNotNull() && fromWorld == manager.getNetherWorld()) {
            e.setTo(new Location(manager.getWorld(), (double) e.getFrom().getBlockX()/8, e.getFrom().getBlockY(), (double) e.getFrom().getBlockZ()/8));
            Team playerTeam = manager.getBoard().getPlayerTeam(p);
            p.playerListName(playerTeam.prefix().append(Component.text(p.getName(), playerTeam.color())).append(Component.text(" ")).append(Message.O_BRACKET.getComponent()).append(Component.text("O", Color.OVERWORLD.getTextColor())).append(Message.C_BRACKET.getComponent()));
        } else if (fromWorld.getEnvironment() == World.Environment.THE_END && manager.isEndNotNull() && fromWorld == manager.getEndWorld()) {
            e.setTo(manager.getWorld().getSpawnLocation());
            Team playerTeam = manager.getBoard().getPlayerTeam(p);
            Bukkit.getScheduler().runTask(manager.getPlugin(), new PlayerListRunnable(p, Color.END, playerTeam));
        }
    }

    private class PlayerListRunnable implements Runnable {
        private final Player player;
        private final Color c;

        private final Team playerTeam;

        public PlayerListRunnable(Player player, Color c, Team playerTeam) {
            this.playerTeam = playerTeam;
            this.player = player;
            this.c = c;
        }

        @Override
        public void run() {
            Component dim = Component.empty();
            switch (c) {
                case OVERWORLD -> dim = Component.text("O", c.getTextColor());
                case NETHER -> dim = Component.text("N", c.getTextColor());
                case END -> dim = Component.text("E", c.getTextColor());
                default -> {}
            }
            player.playerListName(playerTeam.prefix().append(Component.text(player.getName(), playerTeam.color())).append(Component.text(" ")).append(Message.O_BRACKET.getComponent()).append(dim).append(Message.C_BRACKET.getComponent()));
        }
    }


}
