package com.blockforge.chestmarketplus.database;

import com.blockforge.chestmarketplus.ChestMarketPlus;

import java.io.File;
import java.sql.*;
import java.util.logging.Level;

public class DatabaseManager {

    private final ChestMarketPlus plugin;
    private Connection connection;
    private ShopRepository shopRepository;
    private TransactionRepository transactionRepository;
    private PlayerDataRepository playerDataRepository;

    private static final int SCHEMA_VERSION = 1;

    public DatabaseManager(ChestMarketPlus plugin) {
        this.plugin = plugin;
    }

    public boolean initialize() {
        try {
            File dbFile = new File(plugin.getDataFolder(), "chestmarket.db");
            if (!dbFile.getParentFile().exists()) {
                dbFile.getParentFile().mkdirs();
            }

            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());

            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA journal_mode=WAL");
                stmt.execute("PRAGMA foreign_keys=ON");
                stmt.execute("PRAGMA busy_timeout=5000");
            }

            createTables();
            migrateIfNeeded();

            shopRepository = new ShopRepository(connection);
            transactionRepository = new TransactionRepository(connection);
            playerDataRepository = new PlayerDataRepository(connection);

            return true;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize SQLite database", e);
            return false;
        }
    }

    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS schema_version (
                    version INTEGER PRIMARY KEY
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS shops (
                    id          INTEGER PRIMARY KEY AUTOINCREMENT,
                    owner_uuid  TEXT NOT NULL,
                    owner_name  TEXT NOT NULL,
                    world       TEXT NOT NULL,
                    x           INTEGER NOT NULL,
                    y           INTEGER NOT NULL,
                    z           INTEGER NOT NULL,
                    sign_x      INTEGER NOT NULL,
                    sign_y      INTEGER NOT NULL,
                    sign_z      INTEGER NOT NULL,
                    shop_type   TEXT NOT NULL,
                    item_data   TEXT NOT NULL,
                    buy_price   REAL,
                    sell_price  REAL,
                    max_quantity INTEGER DEFAULT 0,
                    is_admin    INTEGER DEFAULT 0,
                    active      INTEGER DEFAULT 1,
                    created_at  INTEGER NOT NULL,
                    expires_at  INTEGER,
                    UNIQUE(world, x, y, z)
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS transactions (
                    id          INTEGER PRIMARY KEY AUTOINCREMENT,
                    shop_id     INTEGER NOT NULL REFERENCES shops(id) ON DELETE CASCADE,
                    buyer_uuid  TEXT NOT NULL,
                    buyer_name  TEXT NOT NULL,
                    action      TEXT NOT NULL,
                    item_type   TEXT NOT NULL,
                    quantity    INTEGER NOT NULL,
                    price_total REAL NOT NULL,
                    tax_amount  REAL DEFAULT 0,
                    created_at  INTEGER NOT NULL
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS player_data (
                    uuid        TEXT PRIMARY KEY,
                    name        TEXT NOT NULL,
                    balance     REAL DEFAULT 0,
                    notify      INTEGER DEFAULT 1,
                    holograms   INTEGER DEFAULT 1,
                    frozen      INTEGER DEFAULT 0
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS shop_trusted (
                    shop_id     INTEGER NOT NULL REFERENCES shops(id) ON DELETE CASCADE,
                    player_uuid TEXT NOT NULL,
                    PRIMARY KEY (shop_id, player_uuid)
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS favorites (
                    player_uuid TEXT NOT NULL,
                    shop_id     INTEGER NOT NULL REFERENCES shops(id) ON DELETE CASCADE,
                    PRIMARY KEY (player_uuid, shop_id)
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS follows (
                    player_uuid TEXT NOT NULL,
                    shop_id     INTEGER NOT NULL REFERENCES shops(id) ON DELETE CASCADE,
                    PRIMARY KEY (player_uuid, shop_id)
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS pending_notifications (
                    id          INTEGER PRIMARY KEY AUTOINCREMENT,
                    player_uuid TEXT NOT NULL,
                    message     TEXT NOT NULL,
                    created_at  INTEGER NOT NULL
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS ratings (
                    player_uuid TEXT NOT NULL,
                    shop_id     INTEGER NOT NULL REFERENCES shops(id) ON DELETE CASCADE,
                    rating      INTEGER NOT NULL,
                    PRIMARY KEY (player_uuid, shop_id)
                )
            """);

            stmt.execute("CREATE INDEX IF NOT EXISTS idx_shops_owner ON shops(owner_uuid)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_shops_world ON shops(world)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_shops_active ON shops(active)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_transactions_shop ON transactions(shop_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_transactions_buyer ON transactions(buyer_uuid)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_pending_player ON pending_notifications(player_uuid)");
        }
    }

    private void migrateIfNeeded() throws SQLException {
        int currentVersion = 0;
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT MAX(version) FROM schema_version")) {
            if (rs.next()) {
                currentVersion = rs.getInt(1);
            }
        } catch (SQLException e) {
            // table might be empty
        }

        if (currentVersion < SCHEMA_VERSION) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT OR REPLACE INTO schema_version (version) VALUES (?)")) {
                ps.setInt(1, SCHEMA_VERSION);
                ps.executeUpdate();
            }
            plugin.getLogger().info("Database schema updated to version " + SCHEMA_VERSION);
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public ShopRepository getShopRepository() {
        return shopRepository;
    }

    public TransactionRepository getTransactionRepository() {
        return transactionRepository;
    }

    public PlayerDataRepository getPlayerDataRepository() {
        return playerDataRepository;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Error closing database connection", e);
        }
    }
}
