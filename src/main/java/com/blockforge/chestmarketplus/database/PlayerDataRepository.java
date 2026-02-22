package com.blockforge.chestmarketplus.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerDataRepository {

    private final Connection connection;

    public PlayerDataRepository(Connection connection) {
        this.connection = connection;
    }

    public void ensurePlayer(UUID uuid, String name) throws SQLException {
        String sql = "INSERT OR IGNORE INTO player_data (uuid, name) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, name);
            ps.executeUpdate();
        }
        String update = "UPDATE player_data SET name=? WHERE uuid=?";
        try (PreparedStatement ps = connection.prepareStatement(update)) {
            ps.setString(1, name);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        }
    }

    public double getBalance(UUID uuid) throws SQLException {
        String sql = "SELECT balance FROM player_data WHERE uuid=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("balance");
            }
        }
        return 0;
    }

    public void setBalance(UUID uuid, double balance) throws SQLException {
        String sql = "UPDATE player_data SET balance=? WHERE uuid=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDouble(1, balance);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        }
    }

    public boolean isNotifyEnabled(UUID uuid) throws SQLException {
        String sql = "SELECT notify FROM player_data WHERE uuid=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("notify") == 1;
            }
        }
        return true;
    }

    public void setNotify(UUID uuid, boolean enabled) throws SQLException {
        String sql = "UPDATE player_data SET notify=? WHERE uuid=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, enabled ? 1 : 0);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        }
    }

    public boolean isHologramsEnabled(UUID uuid) throws SQLException {
        String sql = "SELECT holograms FROM player_data WHERE uuid=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("holograms") == 1;
            }
        }
        return true;
    }

    public void setHolograms(UUID uuid, boolean enabled) throws SQLException {
        String sql = "UPDATE player_data SET holograms=? WHERE uuid=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, enabled ? 1 : 0);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        }
    }

    public boolean isFrozen(UUID uuid) throws SQLException {
        String sql = "SELECT frozen FROM player_data WHERE uuid=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("frozen") == 1;
            }
        }
        return false;
    }

    public void setFrozen(UUID uuid, boolean frozen) throws SQLException {
        String sql = "UPDATE player_data SET frozen=? WHERE uuid=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, frozen ? 1 : 0);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        }
    }

    public void addFavorite(UUID playerUuid, int shopId) throws SQLException {
        String sql = "INSERT OR IGNORE INTO favorites (player_uuid, shop_id) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerUuid.toString());
            ps.setInt(2, shopId);
            ps.executeUpdate();
        }
    }

    public void removeFavorite(UUID playerUuid, int shopId) throws SQLException {
        String sql = "DELETE FROM favorites WHERE player_uuid=? AND shop_id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerUuid.toString());
            ps.setInt(2, shopId);
            ps.executeUpdate();
        }
    }

    public boolean isFavorite(UUID playerUuid, int shopId) throws SQLException {
        String sql = "SELECT 1 FROM favorites WHERE player_uuid=? AND shop_id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerUuid.toString());
            ps.setInt(2, shopId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public List<Integer> getFavoriteShopIds(UUID playerUuid) throws SQLException {
        String sql = "SELECT shop_id FROM favorites WHERE player_uuid=?";
        List<Integer> ids = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerUuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("shop_id"));
                }
            }
        }
        return ids;
    }

    public void addFollow(UUID playerUuid, int shopId) throws SQLException {
        String sql = "INSERT OR IGNORE INTO follows (player_uuid, shop_id) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerUuid.toString());
            ps.setInt(2, shopId);
            ps.executeUpdate();
        }
    }

    public void removeFollow(UUID playerUuid, int shopId) throws SQLException {
        String sql = "DELETE FROM follows WHERE player_uuid=? AND shop_id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerUuid.toString());
            ps.setInt(2, shopId);
            ps.executeUpdate();
        }
    }

    public boolean isFollowing(UUID playerUuid, int shopId) throws SQLException {
        String sql = "SELECT 1 FROM follows WHERE player_uuid=? AND shop_id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerUuid.toString());
            ps.setInt(2, shopId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public List<UUID> getFollowers(int shopId) throws SQLException {
        String sql = "SELECT player_uuid FROM follows WHERE shop_id=?";
        List<UUID> followers = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, shopId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    followers.add(UUID.fromString(rs.getString("player_uuid")));
                }
            }
        }
        return followers;
    }

    public void addTrusted(int shopId, UUID playerUuid) throws SQLException {
        String sql = "INSERT OR IGNORE INTO shop_trusted (shop_id, player_uuid) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, shopId);
            ps.setString(2, playerUuid.toString());
            ps.executeUpdate();
        }
    }

    public void removeTrusted(int shopId, UUID playerUuid) throws SQLException {
        String sql = "DELETE FROM shop_trusted WHERE shop_id=? AND player_uuid=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, shopId);
            ps.setString(2, playerUuid.toString());
            ps.executeUpdate();
        }
    }

    public boolean isTrusted(int shopId, UUID playerUuid) throws SQLException {
        String sql = "SELECT 1 FROM shop_trusted WHERE shop_id=? AND player_uuid=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, shopId);
            ps.setString(2, playerUuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public List<UUID> getTrustedPlayers(int shopId) throws SQLException {
        String sql = "SELECT player_uuid FROM shop_trusted WHERE shop_id=?";
        List<UUID> trusted = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, shopId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    trusted.add(UUID.fromString(rs.getString("player_uuid")));
                }
            }
        }
        return trusted;
    }

    public void addPendingNotification(UUID playerUuid, String message) throws SQLException {
        String sql = "INSERT INTO pending_notifications (player_uuid, message, created_at) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerUuid.toString());
            ps.setString(2, message);
            ps.setLong(3, System.currentTimeMillis() / 1000);
            ps.executeUpdate();
        }
    }

    public List<String> getPendingNotifications(UUID playerUuid) throws SQLException {
        String sql = "SELECT message FROM pending_notifications WHERE player_uuid=? ORDER BY created_at ASC";
        List<String> messages = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerUuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    messages.add(rs.getString("message"));
                }
            }
        }
        return messages;
    }

    public void clearPendingNotifications(UUID playerUuid) throws SQLException {
        String sql = "DELETE FROM pending_notifications WHERE player_uuid=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerUuid.toString());
            ps.executeUpdate();
        }
    }

    public void setRating(UUID playerUuid, int shopId, int rating) throws SQLException {
        String sql = "INSERT OR REPLACE INTO ratings (player_uuid, shop_id, rating) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerUuid.toString());
            ps.setInt(2, shopId);
            ps.setInt(3, rating);
            ps.executeUpdate();
        }
    }

    public int getThumbsUp(int shopId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM ratings WHERE shop_id=? AND rating=1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, shopId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public int getThumbsDown(int shopId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM ratings WHERE shop_id=? AND rating=-1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, shopId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public Integer getPlayerRating(UUID playerUuid, int shopId) throws SQLException {
        String sql = "SELECT rating FROM ratings WHERE player_uuid=? AND shop_id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerUuid.toString());
            ps.setInt(2, shopId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("rating");
            }
        }
        return null;
    }
}
