package id.zailth.auth;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RegisterCommand implements CommandExecutor {
    private final ZailthAuth plugin;
    private final AuthDatabase database;

    public RegisterCommand(ZailthAuth plugin, AuthDatabase database) {
        this.plugin = plugin;
        this.database = database;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by a player.");
            return true;
        }

        if (args.length != 2) {
            player.sendMessage(ChatColor.RED
                    + "Usage: /register <password> <password>");
            return true;
        }

        String uuid = player.getUniqueId().toString();

        if (this.database.isRegistered(uuid)) {
            player.sendMessage(ChatColor.RED
                    + "Your account is already registered.");

            if (this.plugin.isBedrockPlayer(player)
                    && this.plugin.getAuthSession()
                    .isAuthenticated(player.getUniqueId())) {
                player.sendMessage(ChatColor.GREEN
                        + "You are already logged in automatically.");
            } else {
                player.sendMessage(ChatColor.YELLOW
                        + "Use /login <password>");
            }
            return true;
        }

        String password = args[0];
        String confirmation = args[1];

        if (!password.equals(confirmation)) {
            player.sendMessage(ChatColor.RED
                    + "The passwords do not match.");
            return true;
        }

        if (password.length() < 6) {
            player.sendMessage(ChatColor.RED
                    + "Password must be at least 6 characters long.");
            return true;
        }

        String hashedPassword = PasswordUtil.hash(password);
        boolean registered = this.database.register(
                uuid, player.getName(), hashedPassword
        );

        if (!registered) {
            player.sendMessage(ChatColor.RED
                    + "Registration failed. Please try again later.");
            return true;
        }

        player.sendMessage(ChatColor.GREEN
                + "Registration successful!");

        if (this.plugin.isBedrockPlayer(player)) {
            this.plugin.getAuthSession().login(player.getUniqueId());
            player.sendMessage(ChatColor.AQUA
                    + "Bedrock account registered successfully.");
            player.sendMessage(ChatColor.GREEN
                    + "You are now logged in and can play.");
        } else {
            player.sendMessage(ChatColor.YELLOW
                    + "Please log in using /login <password>.");
        }

        return true;
    }
}
