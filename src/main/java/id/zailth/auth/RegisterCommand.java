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

public class RegisterCommand
implements CommandExecutor {
    private final ZailthAuth plugin;
    private final AuthDatabase database;

    public RegisterCommand(ZailthAuth plugin, AuthDatabase database) {
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
            player.sendMessage(String.valueOf(ChatColor.GREEN) + "You can play immediately without registering.");
            return true;
        }
        if (args.length != 2) {
            player.sendMessage(String.valueOf(ChatColor.RED) + "Usage: /register <password> <password>");
            return true;
        }
        String uuid = player.getUniqueId().toString();
        if (this.database.isRegistered(uuid)) {
            player.sendMessage(String.valueOf(ChatColor.RED) + "Your account is already registered.");
            player.sendMessage(String.valueOf(ChatColor.YELLOW) + "Use /login <password>");
            return true;
        }
        String password = args[0];
        String confirmation = args[1];
        if (!password.equals(confirmation)) {
            player.sendMessage(String.valueOf(ChatColor.RED) + "The passwords do not match.");
            return true;
        }
        if (password.length() < 6) {
            player.sendMessage(String.valueOf(ChatColor.RED) + "Password must be at least 6 characters long.");
            return true;
        }
        String hashedPassword = PasswordUtil.hash(password);
        this.database.register(uuid, player.getName(), hashedPassword);
        player.sendMessage(String.valueOf(ChatColor.GREEN) + "Registration successful!");
        player.sendMessage(String.valueOf(ChatColor.YELLOW) + "You can now log in using /login <password>.");
        return true;
    }
}

