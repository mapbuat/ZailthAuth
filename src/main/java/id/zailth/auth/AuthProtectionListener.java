/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.ChatColor
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.block.BlockBreakEvent
 *  org.bukkit.event.block.BlockPlaceEvent
 *  org.bukkit.event.entity.EntityDamageByEntityEvent
 *  org.bukkit.event.player.PlayerCommandPreprocessEvent
 *  org.bukkit.event.player.PlayerDropItemEvent
 *  org.bukkit.event.player.PlayerInteractEvent
 *  org.bukkit.event.player.PlayerMoveEvent
 *  org.bukkit.event.player.PlayerQuitEvent
 */
package id.zailth.auth;

import id.zailth.auth.ZailthAuth;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class AuthProtectionListener
implements Listener {
    private final ZailthAuth plugin;

    public AuthProtectionListener(ZailthAuth plugin) {
        this.plugin = plugin;
    }

    private boolean canPlay(Player player) {
        if (this.plugin.isBedrockPlayer(player)) {
            return true;
        }
        return this.plugin.getAuthSession().isAuthenticated(player.getUniqueId());
    }

    private void sendLoginMessage(Player player) {
        player.sendMessage(String.valueOf(ChatColor.RED) + "You must log in before you can play.");
        player.sendMessage(String.valueOf(ChatColor.YELLOW) + "Use /login <password>");
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!(this.canPlay(player) || event.getTo() == null || event.getFrom().getBlockX() == event.getTo().getBlockX() && event.getFrom().getBlockY() == event.getTo().getBlockY() && event.getFrom().getBlockZ() == event.getTo().getBlockZ())) {
            event.setTo(event.getFrom());
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!this.canPlay(player)) {
            event.setCancelled(true);
            this.sendLoginMessage(player);
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (!this.canPlay(player)) {
            event.setCancelled(true);
            this.sendLoginMessage(player);
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!this.canPlay(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (!this.canPlay(player)) {
            event.setCancelled(true);
            this.sendLoginMessage(player);
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onDamage(EntityDamageByEntityEvent event) {
        Entity entity = event.getDamager();
        if (!(entity instanceof Player)) {
            return;
        }
        Player player = (Player)entity;
        if (!this.canPlay(player)) {
            event.setCancelled(true);
            this.sendLoginMessage(player);
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (this.canPlay(player)) {
            return;
        }
        String message = event.getMessage().toLowerCase();
        if (message.startsWith("/login ") || message.equals("/login") || message.startsWith("/register ") || message.equals("/register")) {
            return;
        }
        event.setCancelled(true);
        this.sendLoginMessage(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        this.plugin.getAuthSession().logout(event.getPlayer().getUniqueId());
    }
}

