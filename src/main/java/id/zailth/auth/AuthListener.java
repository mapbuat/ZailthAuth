package id.zailth.auth;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class AuthListener implements Listener {
    private final ZailthAuth plugin;
    private final AuthDatabase database;

    public AuthListener(ZailthAuth plugin, AuthDatabase database) {
        this.plugin = plugin;
        this.database = database;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();

        // Clear any in-memory authentication state when joining.
        this.plugin.getAuthSession().logout(player.getUniqueId());

        // Bedrock AUTO: only registered Floodgate identities may auto-login.
        if (this.plugin.isBedrockPlayer(player)) {
            if (!this.database.isRegistered(uuid)) {
                player.sendMessage(ChatColor.YELLOW + "Your Bedrock account is not registered yet.");
                player.sendMessage(ChatColor.YELLOW + "Register first using " +
                        ChatColor.WHITE + "/register <password> <password>");
                return;
            }

            this.plugin.getAuthSession().login(player.getUniqueId());
            player.sendMessage(ChatColor.GREEN + "Bedrock account detected and verified.");
            player.sendMessage(ChatColor.GREEN + "You are automatically logged in.");
            return;
        }

        // Java players must have a registered account.
        if (!this.database.isRegistered(uuid)) {
            player.sendMessage(ChatColor.YELLOW + "Your account is not registered.");
            player.sendMessage(ChatColor.YELLOW + "Use " + ChatColor.WHITE +
                    "/register <password> <password>");
            return;
        }

        // Restore a valid saved session for Java players.
        if (this.plugin.isSessionEnabled()) {
            long sessionExpiry = this.database.getSessionExpiry(uuid);
            long now = System.currentTimeMillis();

            if (sessionExpiry > now) {
                long remainingMillis = sessionExpiry - now;
                long remainingMinutes = remainingMillis / 60000L;

                this.plugin.getAuthSession().login(
                        player.getUniqueId(), remainingMillis
                );

                player.sendMessage(ChatColor.GREEN + "Your saved session is still active.");
                player.sendMessage(ChatColor.GRAY + "You are automatically logged in.");

                if (remainingMinutes > 0L) {
                    player.sendMessage(ChatColor.GRAY + "Session remaining: " +
                            remainingMinutes + " minute(s).");
                }
                return;
            }

            this.database.clearSession(uuid);
        }

        player.sendMessage(ChatColor.YELLOW + "Your account is already registered.");
        player.sendMessage(ChatColor.YELLOW + "Please use " +
                ChatColor.WHITE + "/login <password>");
    }
}
