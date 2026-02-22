package com.blockforge.chestmarketplus.database;

import com.blockforge.chestmarketplus.api.ShopTransaction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TransactionRepository {

    private final Connection connection;

    public TransactionRepository(Connection connection) {
        this.connection = connection;
    }

    public void logTransaction(ShopTransaction tx) throws SQLException {
        String sql = """
            INSERT INTO transactions (shop_id, buyer_uuid, buyer_name, action, item_type,
                                      quantity, price_total, tax_amount, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, tx.getShopId());
            ps.setString(2, tx.getBuyerUuid().toString());
            ps.setString(3, tx.getBuyerName());
            ps.setString(4, tx.getAction());
            ps.setString(5, tx.getItemType());
            ps.setInt(6, tx.getQuantity());
            ps.setDouble(7, tx.getPriceTotal());
            ps.setDouble(8, tx.getTaxAmount());
            ps.setLong(9, tx.getCreatedAt());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    tx.setId(keys.getInt(1));
                }
            }
        }
    }

    public List<ShopTransaction> getTransactionsByShop(int shopId, int limit, int offset) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE shop_id=? ORDER BY created_at DESC LIMIT ? OFFSET ?";
        List<ShopTransaction> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, shopId);
            ps.setInt(2, limit);
            ps.setInt(3, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapTransaction(rs));
                }
            }
        }
        return list;
    }

    public List<ShopTransaction> getTransactionsByPlayer(UUID playerUuid, int limit) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE buyer_uuid=? ORDER BY created_at DESC LIMIT ?";
        List<ShopTransaction> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerUuid.toString());
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapTransaction(rs));
                }
            }
        }
        return list;
    }

    public int getTransactionCountByShop(int shopId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM transactions WHERE shop_id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, shopId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public double getTotalRevenueByShop(int shopId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(price_total), 0) FROM transactions WHERE shop_id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, shopId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        }
        return 0;
    }

    public int getTotalTransactionCount() throws SQLException {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM transactions")) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public double getTotalTaxCollected() throws SQLException {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COALESCE(SUM(tax_amount), 0) FROM transactions")) {
            if (rs.next()) return rs.getDouble(1);
        }
        return 0;
    }

    public double getTotalVolumeTraded() throws SQLException {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COALESCE(SUM(price_total), 0) FROM transactions")) {
            if (rs.next()) return rs.getDouble(1);
        }
        return 0;
    }

    private ShopTransaction mapTransaction(ResultSet rs) throws SQLException {
        ShopTransaction tx = new ShopTransaction();
        tx.setId(rs.getInt("id"));
        tx.setShopId(rs.getInt("shop_id"));
        tx.setBuyerUuid(UUID.fromString(rs.getString("buyer_uuid")));
        tx.setBuyerName(rs.getString("buyer_name"));
        tx.setAction(rs.getString("action"));
        tx.setItemType(rs.getString("item_type"));
        tx.setQuantity(rs.getInt("quantity"));
        tx.setPriceTotal(rs.getDouble("price_total"));
        tx.setTaxAmount(rs.getDouble("tax_amount"));
        tx.setCreatedAt(rs.getLong("created_at"));
        return tx;
    }
}
