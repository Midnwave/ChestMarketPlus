package com.blockforge.chestmarketplus.database;

import com.blockforge.chestmarketplus.api.Shop;
import com.blockforge.chestmarketplus.api.ShopType;
import com.blockforge.chestmarketplus.util.ItemUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ShopRepository {

    private final Connection connection;

    public ShopRepository(Connection connection) {
        this.connection = connection;
    }

    public Shop createShop(Shop shop) throws SQLException {
        String sql = """
            INSERT INTO shops (owner_uuid, owner_name, world, x, y, z, sign_x, sign_y, sign_z,
                               shop_type, item_data, buy_price, sell_price, max_quantity,
                               is_admin, active, created_at, expires_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, shop.getOwnerUuid().toString());
            ps.setString(2, shop.getOwnerName());
            ps.setString(3, shop.getWorld());
            ps.setInt(4, shop.getX());
            ps.setInt(5, shop.getY());
            ps.setInt(6, shop.getZ());
            ps.setInt(7, shop.getSignX());
            ps.setInt(8, shop.getSignY());
            ps.setInt(9, shop.getSignZ());
            ps.setString(10, shop.getShopType().name());
            ps.setString(11, ItemUtils.serializeItemStack(shop.getItemTemplate()));
            setNullableDouble(ps, 12, shop.getBuyPrice());
            setNullableDouble(ps, 13, shop.getSellPrice());
            ps.setInt(14, shop.getMaxQuantity());
            ps.setInt(15, shop.isAdmin() ? 1 : 0);
            ps.setInt(16, shop.isActive() ? 1 : 0);
            ps.setLong(17, shop.getCreatedAt());
            setNullableLong(ps, 18, shop.getExpiresAt());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    shop.setId(keys.getInt(1));
                }
            }
        }
        return shop;
    }

    public void updateShop(Shop shop) throws SQLException {
        String sql = """
            UPDATE shops SET owner_uuid=?, owner_name=?, shop_type=?, item_data=?,
                             buy_price=?, sell_price=?, max_quantity=?, is_admin=?,
                             active=?, expires_at=?
            WHERE id=?
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, shop.getOwnerUuid().toString());
            ps.setString(2, shop.getOwnerName());
            ps.setString(3, shop.getShopType().name());
            ps.setString(4, ItemUtils.serializeItemStack(shop.getItemTemplate()));
            setNullableDouble(ps, 5, shop.getBuyPrice());
            setNullableDouble(ps, 6, shop.getSellPrice());
            ps.setInt(7, shop.getMaxQuantity());
            ps.setInt(8, shop.isAdmin() ? 1 : 0);
            ps.setInt(9, shop.isActive() ? 1 : 0);
            setNullableLong(ps, 10, shop.getExpiresAt());
            ps.setInt(11, shop.getId());

            ps.executeUpdate();
        }
    }

    public void deleteShop(int shopId) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM shops WHERE id=?")) {
            ps.setInt(1, shopId);
            ps.executeUpdate();
        }
    }

    public Shop getShopById(int id) throws SQLException {
        String sql = "SELECT * FROM shops WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapShop(rs);
                }
            }
        }
        return null;
    }

    public Shop getShopByLocation(String world, int x, int y, int z) throws SQLException {
        String sql = "SELECT * FROM shops WHERE world=? AND x=? AND y=? AND z=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, world);
            ps.setInt(2, x);
            ps.setInt(3, y);
            ps.setInt(4, z);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapShop(rs);
                }
            }
        }
        return null;
    }

    public Shop getShopBySignLocation(String world, int x, int y, int z) throws SQLException {
        String sql = "SELECT * FROM shops WHERE world=? AND sign_x=? AND sign_y=? AND sign_z=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, world);
            ps.setInt(2, x);
            ps.setInt(3, y);
            ps.setInt(4, z);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapShop(rs);
                }
            }
        }
        return null;
    }

    public List<Shop> getAllActiveShops() throws SQLException {
        String sql = "SELECT * FROM shops WHERE active=1";
        List<Shop> shops = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                shops.add(mapShop(rs));
            }
        }
        return shops;
    }

    public List<Shop> getShopsByOwner(UUID ownerUuid) throws SQLException {
        String sql = "SELECT * FROM shops WHERE owner_uuid=?";
        List<Shop> shops = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, ownerUuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    shops.add(mapShop(rs));
                }
            }
        }
        return shops;
    }

    public int getShopCountByOwner(UUID ownerUuid) throws SQLException {
        String sql = "SELECT COUNT(*) FROM shops WHERE owner_uuid=? AND active=1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, ownerUuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public List<Shop> getExpiredShops() throws SQLException {
        long now = System.currentTimeMillis() / 1000;
        String sql = "SELECT * FROM shops WHERE active=1 AND expires_at IS NOT NULL AND expires_at < ?";
        List<Shop> shops = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, now);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    shops.add(mapShop(rs));
                }
            }
        }
        return shops;
    }

    public List<Shop> getExpiringShops(int withinDays) throws SQLException {
        long now = System.currentTimeMillis() / 1000;
        long threshold = now + (withinDays * 86400L);
        String sql = "SELECT * FROM shops WHERE active=1 AND expires_at IS NOT NULL AND expires_at > ? AND expires_at < ?";
        List<Shop> shops = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, now);
            ps.setLong(2, threshold);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    shops.add(mapShop(rs));
                }
            }
        }
        return shops;
    }

    public int getTotalShopCount() throws SQLException {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM shops")) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public int getActiveShopCount() throws SQLException {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM shops WHERE active=1")) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    private Shop mapShop(ResultSet rs) throws SQLException {
        Shop shop = new Shop();
        shop.setId(rs.getInt("id"));
        shop.setOwnerUuid(UUID.fromString(rs.getString("owner_uuid")));
        shop.setOwnerName(rs.getString("owner_name"));
        shop.setWorld(rs.getString("world"));
        shop.setX(rs.getInt("x"));
        shop.setY(rs.getInt("y"));
        shop.setZ(rs.getInt("z"));
        shop.setSignX(rs.getInt("sign_x"));
        shop.setSignY(rs.getInt("sign_y"));
        shop.setSignZ(rs.getInt("sign_z"));
        shop.setShopType(ShopType.valueOf(rs.getString("shop_type")));
        shop.setItemTemplate(ItemUtils.deserializeItemStack(rs.getString("item_data")));
        shop.setBuyPrice(getNullableDouble(rs, "buy_price"));
        shop.setSellPrice(getNullableDouble(rs, "sell_price"));
        shop.setMaxQuantity(rs.getInt("max_quantity"));
        shop.setAdmin(rs.getInt("is_admin") == 1);
        shop.setActive(rs.getInt("active") == 1);
        shop.setCreatedAt(rs.getLong("created_at"));
        shop.setExpiresAt(getNullableLong(rs, "expires_at"));
        return shop;
    }

    private void setNullableDouble(PreparedStatement ps, int index, Double value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.REAL);
        } else {
            ps.setDouble(index, value);
        }
    }

    private void setNullableLong(PreparedStatement ps, int index, Long value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.INTEGER);
        } else {
            ps.setLong(index, value);
        }
    }

    private Double getNullableDouble(ResultSet rs, String column) throws SQLException {
        double val = rs.getDouble(column);
        return rs.wasNull() ? null : val;
    }

    private Long getNullableLong(ResultSet rs, String column) throws SQLException {
        long val = rs.getLong(column);
        return rs.wasNull() ? null : val;
    }
}
