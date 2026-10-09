/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.ChatColor
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.player.PlayerJoinEvent
 */
package id.zailth.auth;

import id.zailth.auth.AuthDatabase;
import id.zailth.auth.ZailthAuth;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class AuthListener
implements Listener {
    private final ZailthAuth plugin;
    private final AuthDatabase database;

    public AuthListener(ZailthAuth plugin, AuthDatabase database) {
        this.plugin = plugin;
        this.database = database;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (this.plugin.isBedrockPlayer(player)) {
            this.plugin.getAuthSession().login(player.getUniqueId());
            player.sendMessage(String.valueOf(ChatColor.GREEN) + "You are detected as a Bedrock player.");
            player.sendMessage(String.valueOf(ChatColor.GREEN) + "You can play immediately without logging in.");
            return;
        }
        String uuid = player.getUniqueId().toString();
        if (!this.database.isRegistered(uuid)) {
            this.plugin.getAuthSession().logout(player.getUniqueId());
            player.sendMessage(String.valueOf(ChatColor.YELLOW) + "Your account is not registered.");
            player.sendMessage(String.valueOf(ChatColor.YELLOW) + "Use " + String.valueOf(ChatColor.WHITE) + "/register <password> <password>");
            return;
        }
        if (this.plugin.isSessionEnabled()) {
            long sessionExpiry = this.database.getSessionExpiry(uuid);
            if (sessionExpiry > System.currentTimeMillis()) {
                long remainingMinutes = (sessionExpiry - System.currentTimeMillis()) / 60000L;
                this.plugin.getAuthSession().login(player.getUniqueId(), sessionExpiry - System.currentTimeMillis());
                player.sendMessage(String.valueOf(ChatColor.GREEN) + "Your saved session is still active.");
                player.sendMessage(String.valueOf(ChatColor.GRAY) + "You are automatically logged in.");
                if (remainingMinutes > 0L) {
                    player.sendMessage(String.valueOf(ChatColor.GRAY) + "Session remaining: " + remainingMinutes + " minute(s).");
                }
                return;
            }
            this.database.clearSession(uuid);
        }
        this.plugin.getAuthSession().logout(player.getUniqueId());
        player.sendMessage(String.valueOf(ChatColor.YELLOW) + "Your account is already registered.");
        player.sendMessage(String.valueOf(ChatColor.YELLOW) + "Please use " + String.valueOf(ChatColor.WHITE) + "/login <password>");
    }
}

