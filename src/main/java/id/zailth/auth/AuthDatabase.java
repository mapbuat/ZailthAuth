package id.zailth.auth;

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
        String sql = "ALTER TABLE users ADD COLUMN session_expires_at INTEGER DEFAULT 0";
        try (PreparedStatement statement = this.plugin.getDatabase().prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            String message = e.getMessage();
            if (message == null || !message.toLowerCase().contains("duplicate column")) {
                this.plugin.getLogger().warning("Could not create session column: " + e.getMessage());
            }
        }
    }

    public boolean isRegistered(String uuid) {
        String sql = "SELECT uuid FROM users WHERE uuid = ?";
        try (PreparedStatement statement = this.plugin.getDatabase().prepareStatement(sql)) {
            statement.setString(1, uuid);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        } catch (SQLException e) {
            this.plugin.getLogger().severe("Failed to check account: " + e.getMessage());
            return false;
        }
    }

    public boolean register(String uuid, String username, String password) {
        String sql = "INSERT INTO users (uuid, username, password, registered_at, session_expires_at) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = this.plugin.getDatabase().prepareStatement(sql)) {
            statement.setString(1, uuid);
            statement.setString(2, username);
            statement.setString(3, password);
            statement.setLong(4, System.currentTimeMillis());
            statement.setLong(5, 0L);
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            this.plugin.getLogger().severe("Failed to create account: " + e.getMessage());
            return false;
        }
    }

    public String getPassword(String uuid) {
        String sql = "SELECT password FROM users WHERE uuid = ?";
        try (PreparedStatement statement = this.plugin.getDatabase().prepareStatement(sql)) {
            statement.setString(1, uuid);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getString("password");
                }
            }
        } catch (SQLException e) {
            this.plugin.getLogger().severe("Failed to retrieve password: " + e.getMessage());
        }
        return null;
    }

    public boolean saveSession(String uuid, long expiresAt) {
        String sql = "UPDATE users SET session_expires_at = ? WHERE uuid = ?";
        try (PreparedStatement statement = this.plugin.getDatabase().prepareStatement(sql)) {
            statement.setLong(1, expiresAt);
            statement.setString(2, uuid);
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            this.plugin.getLogger().warning("Failed to save session: " + e.getMessage());
            return false;
        }
    }

    public long getSessionExpiry(String uuid) {
        String sql = "SELECT session_expires_at FROM users WHERE uuid = ?";
        try (PreparedStatement statement = this.plugin.getDatabase().prepareStatement(sql)) {
            statement.setString(1, uuid);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getLong("session_expires_at");
                }
            }
        } catch (SQLException e) {
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
