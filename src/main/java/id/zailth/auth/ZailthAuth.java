/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.entity.Player
 *  org.bukkit.event.Listener
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.plugin.java.JavaPlugin
 *  org.geysermc.floodgate.api.FloodgateApi
 */
package id.zailth.auth;

import id.zailth.auth.AuthCommand;
import id.zailth.auth.AuthDatabase;
import id.zailth.auth.AuthListener;
import id.zailth.auth.AuthProtectionListener;
import id.zailth.auth.LoginCommand;
import id.zailth.auth.RegisterCommand;
import id.zailth.auth.session.AuthSession;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.geysermc.floodgate.api.FloodgateApi;

public class ZailthAuth
extends JavaPlugin {
    private Connection database;
    private AuthDatabase authDatabase;
    private AuthSession authSession;
    private boolean sessionEnabled;
    private long sessionDurationMillis;

    public void onEnable() {
        this.getLogger().info("Starting ZailthAuth...");
        this.saveDefaultConfig();
        this.loadSessionConfig();
        if (!this.setupDatabase()) {
            this.getLogger().severe("Failed to connect to database!");
            this.getServer().getPluginManager().disablePlugin((Plugin)this);
            return;
        }
        this.authDatabase = new AuthDatabase(this);
        this.authSession = new AuthSession();
        if (this.getCommand("register") != null) {
            this.getCommand("register").setExecutor((CommandExecutor)new RegisterCommand(this, this.authDatabase));
        }
        if (this.getCommand("login") != null) {
            this.getCommand("login").setExecutor((CommandExecutor)new LoginCommand(this, this.authDatabase));
        }
        if (this.getCommand("auth") != null) {
            this.getCommand("auth").setExecutor((CommandExecutor)new AuthCommand(this, this.authDatabase));
        }
        this.getServer().getPluginManager().registerEvents((Listener)new AuthListener(this, this.authDatabase), (Plugin)this);
        this.getServer().getPluginManager().registerEvents((Listener)new AuthProtectionListener(this), (Plugin)this);
        this.getLogger().info("SQLite database connected.");
        this.getLogger().info("Authentication session system initialized.");
        this.getLogger().info("Session enabled: " + this.sessionEnabled);
        this.getLogger().info("Session duration: " + this.sessionDurationMillis / 60000L + " minutes.");
        this.getLogger().info("ZailthAuth enabled successfully.");
    }

    public void onDisable() {
        if (this.authSession != null) {
            this.authSession.clear();
        }
        this.closeDatabase();
        this.getLogger().info("ZailthAuth disabled.");
    }

    private void loadSessionConfig() {
        this.sessionEnabled = this.getConfig().getBoolean("session.enabled", true);
        long durationMinutes = this.getConfig().getLong("session.duration-minutes", 30L);
        if (durationMinutes < 1L) {
            durationMinutes = 30L;
        }
        this.sessionDurationMillis = durationMinutes * 60000L;
    }

    private boolean setupDatabase() {
        try {
            if (!this.getDataFolder().exists()) {
                this.getDataFolder().mkdirs();
            }
            File databaseFile = new File(this.getDataFolder(), "auth.db");
            this.database = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
            this.database.createStatement().executeUpdate("    CREATE TABLE IF NOT EXISTS users (\n        uuid TEXT PRIMARY KEY,\n        username TEXT NOT NULL,\n        password TEXT NOT NULL,\n        registered_at INTEGER NOT NULL\n    )\n");
            return true;
        }
        catch (SQLException e) {
            this.getLogger().severe("Database error: " + e.getMessage());
            return false;
        }
    }

    private void closeDatabase() {
        if (this.database != null) {
            try {
                if (!this.database.isClosed()) {
                    this.database.close();
                }
            }
            catch (SQLException e) {
                this.getLogger().warning("Failed to close database: " + e.getMessage());
            }
        }
    }

    public boolean isBedrockPlayer(Player player) {
        return FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId());
    }

    public Connection getDatabase() {
        return this.database;
    }

    public AuthDatabase getAuthDatabase() {
        return this.authDatabase;
    }

    public AuthSession getAuthSession() {
        return this.authSession;
    }

    public boolean isSessionEnabled() {
        return this.sessionEnabled;
    }

    public long getSessionDurationMillis() {
        return this.sessionDurationMillis;
    }
}

