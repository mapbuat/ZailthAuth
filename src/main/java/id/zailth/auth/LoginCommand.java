package id.zailth.auth;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LoginCommand implements CommandExecutor {
    private final ZailthAuth plugin;
    private final AuthDatabase database;

    public LoginCommand(ZailthAuth plugin, AuthDatabase database) {
        this.plugin = plugin;
        this.database = database;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by a player.");
            return true;
        }

        // Bedrock players with registered Floodgate identities are handled on join.
        if (this.plugin.isBedrockPlayer(player)) {
            if (this.database.isRegistered(player.getUniqueId().toString())
                    && this.plugin.getAuthSession().isAuthenticated(player.getUniqueId())) {
                player.sendMessage(ChatColor.GREEN + "You are already logged in automatically.");
            } else {
                player.sendMessage(ChatColor.YELLOW + "Register first with /register <password> <password> if you are new.");
            }
            return true;
        }

        if (this.plugin.getAuthSession().isAuthenticated(player.getUniqueId())) {
            player.sendMessage(ChatColor.YELLOW + "You are already logged in.");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /login <password>");
            return true;
        }

        String uuid = player.getUniqueId().toString();

        if (!this.database.isRegistered(uuid)) {
            player.sendMessage(ChatColor.RED + "Your account is not registered.");
            player.sendMessage(ChatColor.YELLOW + "Use /register <password> <password>");
            return true;
        }

        String storedHash = this.database.getPassword(uuid);
        if (storedHash == null) {
            player.sendMessage(ChatColor.RED + "An error occurred while loading your account.");
            return true;
        }

        if (!PasswordUtil.verify(args[0], storedHash)) {
            player.sendMessage(ChatColor.RED + "Incorrect password.");
            return true;
        }

        if (this.plugin.isSessionEnabled()) {
            long durationMillis = this.plugin.getSessionDurationMillis();
            long sessionExpiry = System.currentTimeMillis() + durationMillis;

            if (!this.database.saveSession(uuid, sessionExpiry)) {
                player.sendMessage(ChatColor.RED + "Login could not be saved. Please try again.");
                return true;
            }

            this.plugin.getAuthSession().login(player.getUniqueId(), durationMillis);
            long durationMinutes = durationMillis / 60000L;

            player.sendMessage(ChatColor.GREEN + "Login successful!");
            player.sendMessage(ChatColor.GRAY + "Your session will remain active for "
                    + durationMinutes + " minute(s).");
        } else {
            this.plugin.getAuthSession().login(player.getUniqueId());
            player.sendMessage(ChatColor.GREEN + "Login successful!");
        }

        player.sendMessage(ChatColor.GRAY + "Welcome back, "
                + ChatColor.WHITE + player.getName());

        return true;
    }
}
