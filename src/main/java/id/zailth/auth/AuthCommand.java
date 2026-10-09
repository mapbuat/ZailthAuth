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
import id.zailth.auth.ZailthAuth;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AuthCommand
implements CommandExecutor {
    private static final String ADMIN_PERMISSION = "zailthauth.admin";
    private final ZailthAuth plugin;
    private final AuthDatabase database;

    public AuthCommand(ZailthAuth plugin, AuthDatabase database) {
        this.plugin = plugin;
        this.database = database;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player;
        if (sender instanceof Player && !(player = (Player)sender).hasPermission(ADMIN_PERMISSION)) {
            player.sendMessage(String.valueOf(ChatColor.RED) + "You do not have permission to use this command.");
            return true;
        }
        if (args.length != 2 || !args[0].equalsIgnoreCase("reset")) {
            sender.sendMessage(String.valueOf(ChatColor.YELLOW) + "Usage: /auth reset <player>");
            return true;
        }
        String username = args[1];
        if (!this.database.resetAccount(username)) {
            sender.sendMessage(String.valueOf(ChatColor.RED) + "No registered account found for player: " + username);
            return true;
        }
        Player onlinePlayer = this.plugin.getServer().getPlayerExact(username);
        if (onlinePlayer != null) {
            this.plugin.getAuthSession().logout(onlinePlayer.getUniqueId());
        }
        sender.sendMessage(String.valueOf(ChatColor.GREEN) + "Authentication account reset successfully for " + username + ".");
        if (onlinePlayer != null) {
            onlinePlayer.sendMessage(String.valueOf(ChatColor.YELLOW) + "Your authentication account has been reset.");
            onlinePlayer.sendMessage(String.valueOf(ChatColor.YELLOW) + "Please register again using " + String.valueOf(ChatColor.WHITE) + "/register <password> <password>");
        }
        return true;
    }
}

