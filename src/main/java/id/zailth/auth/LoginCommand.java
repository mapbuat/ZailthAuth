/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.ChatColor
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package id.zailth.auth;

import id.zailth.auth.AuthDatabase;
import id.zailth.auth.PasswordUtil;
import id.zailth.auth.ZailthAuth;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LoginCommand
implements CommandExecutor {
    private final ZailthAuth plugin;
    private final AuthDatabase database;

    public LoginCommand(ZailthAuth plugin, AuthDatabase database) {
        this.plugin = plugin;
        this.database = database;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by a player.");
            return true;
        }
        Player player = (Player)sender;
        if (this.plugin.isBedrockPlayer(player)) {
            player.sendMessage(String.valueOf(ChatColor.GREEN) + "You are detected as a Bedrock player.");
            player.sendMessage(String.valueOf(ChatColor.GREEN) + "You can play immediately without logging in.");
            return true;
        }
        if (this.plugin.getAuthSession().isAuthenticated(player.getUniqueId())) {
            player.sendMessage(String.valueOf(ChatColor.YELLOW) + "You are already logged in.");
            return true;
        }
        if (args.length != 1) {
            player.sendMessage(String.valueOf(ChatColor.RED) + "Usage: /login <password>");
            return true;
        }
        String uuid = player.getUniqueId().toString();
        if (!this.database.isRegistered(uuid)) {
            player.sendMessage(String.valueOf(ChatColor.RED) + "Your account is not registered.");
            player.sendMessage(String.valueOf(ChatColor.YELLOW) + "Use /register <password> <password>");
            return true;
        }
        String password = args[0];
        String storedHash = this.database.getPassword(uuid);
        if (storedHash == null) {
            player.sendMessage(String.valueOf(ChatColor.RED) + "An error occurred while loading your account.");
            return true;
        }
        if (!PasswordUtil.verify(password, storedHash)) {
            player.sendMessage(String.valueOf(ChatColor.RED) + "Incorrect password.");
            return true;
        }
        if (this.plugin.isSessionEnabled()) {
            long sessionExpiry = System.currentTimeMillis() + this.plugin.getSessionDurationMillis();
            this.database.saveSession(uuid, sessionExpiry);
            this.plugin.getAuthSession().login(player.getUniqueId(), this.plugin.getSessionDurationMillis());
            long durationMinutes = this.plugin.getSessionDurationMillis() / 60000L;
            player.sendMessage(String.valueOf(ChatColor.GREEN) + "Login successful!");
            player.sendMessage(String.valueOf(ChatColor.GRAY) + "Your session will remain active for " + durationMinutes + " minute(s).");
        } else {
            this.plugin.getAuthSession().login(player.getUniqueId());
            player.sendMessage(String.valueOf(ChatColor.GREEN) + "Login successful!");
        }
        player.sendMessage(String.valueOf(ChatColor.GRAY) + "Welcome back, " + String.valueOf(ChatColor.WHITE) + player.getName());
        return true;
    }
}

