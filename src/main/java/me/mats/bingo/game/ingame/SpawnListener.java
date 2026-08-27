package me.mats.bingo.game.ingame;



import de.tr7zw.nbtinjector.javassist.tools.web.Viewer;
import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import me.mats.bingo.Bingo;
import me.mats.common.enums.Color;
import me.mats.bingo.game.BingoManager;
import me.mats.bingo.game.BingoTeam;
import me.mats.common.message.Message;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static me.mats.common.enums.Color.*;

public class SpawnListener implements Listener {

    private final IngameState state;
    private List<Player> playersWithElytra;
    private Map<Player,ItemStack> playerToChestplate = new HashMap<>();

    public SpawnListener(IngameState state) {
        this.state = state;
        playersWithElytra = new ArrayList<>();
        playersWithElytra.addAll(state.getManager().getPlayers());
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
        if (state.getManager().getPlayers().contains(p)) {
            e.setRespawnLocation(state.getManager().getWorld().getSpawnLocation());
            ItemStack elytra = new ItemStack(Material.ELYTRA);
            ItemMeta meta = elytra.getItemMeta();
            if (state.getAbilities().getKeepInventoryAbilityList().contains(p)) {
                playerToChestplate.put(p, p.getInventory().getChestplate());
            }
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            elytra.setItemMeta(meta);
            p.getInventory().setChestplate(elytra);
            playersWithElytra.add(p);

            Bukkit.getScheduler().runTask(Bingo.getInstance(), new PlayerListRunnable(p, OVERWORLD, state.getManager().getBoard().getPlayerTeam(p)));
            Bukkit.getScheduler().runTask(state.getManager().getPlugin(), () -> {state.getAbilities().setAbilities(p);});
        }

    }

    // Spawn Protection Listeners
    @EventHandler
    public void onBreakBlock(BlockBreakEvent e) {
        Player p = e.getPlayer();
        Block b = e.getBlock();
        if (state.getManager().getPlayers().contains(p)) {
            if (b.getX() <= 15 && b.getX() >= -15 && b.getZ() <= 15 && b.getZ() >= -15 && b.getY() >= state.getManager().getWorld().getSpawnLocation().getY()-5) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        Block b = e.getBlockPlaced();
        if (state.getManager().getPlayers().contains(p)) {
            if (b.getX() <= 15 && b.getX() >= -15 && b.getZ() <= 15 && b.getZ() >= -15 && b.getY() >= state.getManager().getWorld().getSpawnLocation().getY() - 5) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPortalUse(EntityPortalEvent e) {
        World fromWorld = e.getFrom().getWorld();
        BingoManager manager = state.getManager();
        if (fromWorld.getEnvironment() == World.Environment.NORMAL && fromWorld == manager.getWorld()) {
            // This means the Entity is coming from the Main Bingo World of this Listener
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
        BingoManager manager = state.getManager();
        if (fromWorld.getEnvironment() == World.Environment.NORMAL && fromWorld == manager.getWorld()) {
            // This means the Entity is coming from the Main Bingo World of this Listener
            Team playerTeam = manager.getBoard().getPlayerTeam(p);


            if (e.getCause() == PlayerTeleportEvent.TeleportCause.END_PORTAL) {
                e.setTo(new Location(manager.getEndWorld(), 100, 50, 0));
                p.playerListName(playerTeam.prefix().append(Component.text(p.getName(), playerTeam.color())).append(Component.text(" ")).append(Message.O_BRACKET.getComponent()).append(Component.text("E", END.getTextColor())).append(Message.C_BRACKET.getComponent()));
            } else if (e.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
                e.setTo(new Location(manager.getNetherWorld(), e.getFrom().getBlockX()*8, e.getFrom().getBlockY(), e.getFrom().getBlockZ()*8));
                p.playerListName(playerTeam.prefix().append(Component.text(p.getName(), playerTeam.color())).append(Component.text(" ")).append(Message.O_BRACKET.getComponent()).append(Component.text("N", NETHER.getTextColor())).append(Message.C_BRACKET.getComponent()));
            }
        } else if (fromWorld.getEnvironment() == World.Environment.NETHER && manager.isNetherNotNull() && fromWorld == manager.getNetherWorld()) {
            e.setTo(new Location(manager.getWorld(), (double) e.getFrom().getBlockX()/8, e.getFrom().getBlockY(), (double) e.getFrom().getBlockZ()/8));
            Team playerTeam = manager.getBoard().getPlayerTeam(p);
            p.playerListName(playerTeam.prefix().append(Component.text(p.getName(), playerTeam.color())).append(Component.text(" ")).append(Message.O_BRACKET.getComponent()).append(Component.text("O", OVERWORLD.getTextColor())).append(Message.C_BRACKET.getComponent()));
        } else if (fromWorld.getEnvironment() == World.Environment.THE_END && manager.isEndNotNull() && fromWorld == manager.getEndWorld()) {
            e.setTo(manager.getWorld().getSpawnLocation());
            Team playerTeam = manager.getBoard().getPlayerTeam(p);
            Bukkit.getScheduler().runTask(Bingo.getInstance(), new PlayerListRunnable(p, END, playerTeam));
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
            }
            player.playerListName(playerTeam.prefix().append(Component.text(player.getName(), playerTeam.color())).append(Component.text(" ")).append(Message.O_BRACKET.getComponent()).append(dim).append(Message.C_BRACKET.getComponent()));
        }
    }


}
