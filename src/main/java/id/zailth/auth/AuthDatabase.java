/*
 * Decompiled with CFR 0.152.
 */
package id.zailth.auth;

import id.zailth.auth.ZailthAuth;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthDatabase {
    private final ZailthAuth plugin;

    public AuthDatabase(ZailthAuth plugin) {
        this.plugin = plugin;
        this.createSessionColumn();
    }

    private void createSessionColumn() {
        block8: {
            String sql = "ALTER TABLE users\nADD COLUMN session_expires_at INTEGER DEFAULT 0\n";
            try (PreparedStatement statement = this.plugin.getDatabase().prepareStatement(sql);){
                statement.executeUpdate();
            }
            catch (SQLException e) {
                if (e.getMessage().toLowerCase().contains("duplicate column")) break block8;
                this.plugin.getLogger().warning("Could not create session column: " + e.getMessage());
            }
        }
    }

    /*
     * Enabled aggressive exception aggregation
     */
    public boolean isRegistered(String uuid) {
        String sql = "SELECT uuid FROM users WHERE uuid = ?";
        try (PreparedStatement statement = this.plugin.getDatabase().prepareStatement(sql);){
            boolean bl;
            block14: {
                statement.setString(1, uuid);
                ResultSet result = statement.executeQuery();
                try {
                    bl = result.next();
                    if (result == null) break block14;
                }
                catch (Throwable throwable) {
                    if (result != null) {
                        try {
                            result.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                result.close();
            }
            return bl;
        }
        catch (SQLException e) {
            this.plugin.getLogger().severe("Failed to check account: " + e.getMessage());
            return false;
        }
    }

    public void register(String uuid, String username, String password) {
        String sql = "INSERT INTO users\n(uuid, username, password, registered_at, session_expires_at)\nVALUES (?, ?, ?, ?, ?)\n";
        try (PreparedStatement statement = this.plugin.getDatabase().prepareStatement(sql);){
            statement.setString(1, uuid);
            statement.setString(2, username);
            statement.setString(3, password);
            statement.setLong(4, System.currentTimeMillis());
            statement.setLong(5, 0L);
            statement.executeUpdate();
        }
        catch (SQLException e) {
            this.plugin.getLogger().severe("Failed to create account: " + e.getMessage());
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public String getPassword(String uuid) {
        String sql = "SELECT password FROM users WHERE uuid = ?";
        try (PreparedStatement statement = this.plugin.getDatabase().prepareStatement(sql);){
            statement.setString(1, uuid);
            try (ResultSet result = statement.executeQuery();){
                if (!result.next()) return null;
                String string = result.getString("password");
                return string;
            }
        }
        catch (SQLException e) {
            this.plugin.getLogger().severe("Failed to retrieve password: " + e.getMessage());
        }
        return null;
    }

    public void saveSession(String uuid, long expiresAt) {
        String sql = "UPDATE users\nSET session_expires_at = ?\nWHERE uuid = ?\n";
        try (PreparedStatement statement = this.plugin.getDatabase().prepareStatement(sql);){
            statement.setLong(1, expiresAt);
            statement.setString(2, uuid);
            statement.executeUpdate();
        }
        catch (SQLException e) {
            this.plugin.getLogger().warning("Failed to save session: " + e.getMessage());
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public long getSessionExpiry(String uuid) {
        String sql = "SELECT session_expires_at\nFROM users\nWHERE uuid = ?\n";
        try (PreparedStatement statement = this.plugin.getDatabase().prepareStatement(sql);){
            statement.setString(1, uuid);
            try (ResultSet result = statement.executeQuery();){
                if (!result.next()) return 0L;
                long l = result.getLong("session_expires_at");
                return l;
            }
        }
        catch (SQLException e) {
            this.plugin.getLogger().warning("Failed to retrieve session: " + e.getMessage());
        }
        return 0L;
    }

    public void clearSession(String uuid) {
        this.saveSession(uuid, 0L);
    }

    public boolean resetAccount(String username) {
        String sql = "DELETE FROM users WHERE username = ?";
        try (PreparedStatement statement = this.plugin.getDatabase().prepareStatement(sql)) {
            statement.setString(1, username);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            this.plugin.getLogger().severe("Failed to reset account: " + e.getMessage());
            return false;
        }
    }
}

