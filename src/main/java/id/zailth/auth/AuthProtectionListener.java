package id.zailth.auth;

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

public class AuthProtectionListener implements Listener {
    private final ZailthAuth plugin;

    public AuthProtectionListener(ZailthAuth plugin) {
        this.plugin = plugin;
    }

    private boolean canPlay(Player player) {
        return this.plugin.getAuthSession()
                .isAuthenticated(player.getUniqueId());
    }

    private void sendLoginMessage(Player player) {
        player.sendMessage(ChatColor.RED
                + "You must log in or register before you can play.");
        if (this.plugin.isBedrockPlayer(player)
                && !this.plugin.getAuthSession()
                .isAuthenticated(player.getUniqueId())) {
            player.sendMessage(ChatColor.YELLOW
                    + "New Bedrock player? Use /register <password> <password>");
        } else {
            player.sendMessage(ChatColor.YELLOW
                    + "Use /login <password>");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (canPlay(player) || event.getTo() == null) {
            return;
        }

        if (event.getFrom().getBlockX() != event.getTo().getBlockX()
                || event.getFrom().getBlockY() != event.getTo().getBlockY()
                || event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
            event.setTo(event.getFrom());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!canPlay(event.getPlayer())) {
            event.setCancelled(true);
            sendLoginMessage(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!canPlay(event.getPlayer())) {
            event.setCancelled(true);
            sendLoginMessage(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (!canPlay(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDropItem(PlayerDropItemEvent event) {
        if (!canPlay(event.getPlayer())) {
            event.setCancelled(true);
            sendLoginMessage(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();

        if (damager instanceof Player player && !canPlay(player)) {
            event.setCancelled(true);
            sendLoginMessage(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        if (canPlay(player)) {
            return;
        }

        String message = event.getMessage().toLowerCase();

        if (message.equals("/login")
                || message.startsWith("/login ")
                || message.equals("/register")
                || message.startsWith("/register ")) {
            return;
        }

        event.setCancelled(true);
        sendLoginMessage(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        this.plugin.getAuthSession()
                .logout(event.getPlayer().getUniqueId());
    }
}
