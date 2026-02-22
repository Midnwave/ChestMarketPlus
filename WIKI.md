# ChestMarket+ Wiki

The complete guide to ChestMarket+, the most advanced free chest shop plugin for Minecraft servers running Spigot or Paper 1.21+.

## Table of Contents

1. [Getting Started](#getting-started)
2. [Creating Your First Shop](#creating-your-first-shop)
3. [Using Shops](#using-shops)
4. [Commands](#commands)
5. [Permissions](#permissions)
6. [Configuration](#configuration)
7. [Economy System](#economy-system)
8. [Holograms and Displays](#holograms-and-displays)
9. [Sign Format](#sign-format)
10. [Shop Protection](#shop-protection)
11. [Trust System](#trust-system)
12. [WorldGuard Integration](#worldguard-integration)
13. [World Restrictions](#world-restrictions)
14. [Shop Expiry](#shop-expiry)
15. [Notifications](#notifications)
16. [Discord Integration](#discord-integration)
17. [Favorites and Following](#favorites-and-following)
18. [Rating System](#rating-system)
19. [Quick Sell](#quick-sell)
20. [Item Blacklist and Whitelist](#item-blacklist-and-whitelist)
21. [Admin Tools](#admin-tools)
22. [Config GUI](#config-gui)
23. [Localization](#localization)
24. [Platform Compatibility](#platform-compatibility)
25. [Frequently Asked Questions](#frequently-asked-questions)

---

## Getting Started

### Installation

1. Download the latest ChestMarket+ jar file.
2. Place it in your server's `plugins/` folder.
3. Restart your server.
4. The plugin will generate `config.yml`, a `locale/` folder with language files, and a `data.db` SQLite database inside `plugins/ChestMarketPlus/`.

### Requirements

ChestMarket+ requires **Java 21** and **Minecraft 1.21** or newer. It works on both **Paper** and **Spigot** servers. Paper is recommended for the best experience, as it unlocks native dialog confirmations and better hologram rendering using Display Entities.

### Optional Dependencies

**Vault** is supported as an optional dependency. If Vault and an economy plugin (like EssentialsX) are installed, ChestMarket+ will automatically use them for all transactions. If Vault is not installed, a simple built in economy is used instead.

**WorldGuard** is also supported as an optional dependency. When installed, a custom `chest-shop` region flag is registered, allowing server admins to control where shops can be created on a per region basis.

---

## Creating Your First Shop

There are two ways to create a shop: using a **sign** or using a **command**.

### Method 1: Sign Based Creation

This is the most common way players create shops.

1. Place a chest where you want your shop.
2. Hold the item you want to sell in your main hand.
3. Place a sign on or next to the chest.
4. Write one of the trigger words on the first line of the sign. The default trigger words are `[Shop]`, `[ChestMarket]`, `[ChestShop]`, `[CM]`, `[CM+]`, `[Market]`, `[Buy]`, and `[Sell]`. These are case insensitive, so `[shop]` works too.
5. A GUI will open asking you to choose the shop type: **BUY** (players buy from you), **SELL** (you buy from players), or **BUY + SELL** (both directions).
6. After selecting the type, you will be prompted in chat to enter your price. Type the price and press enter. For BUY + SELL shops, you will be asked for the buy price first, then the sell price.
7. Type `cancel` at any time to abort the creation process.

Once the shop is created, the sign will be automatically formatted with your name, the item name, and the prices. A hologram will also appear above the chest showing the shop information to nearby players.

If your server has the **require crouch** option enabled in the config, you will need to be sneaking (holding shift) when you place the sign.

### Method 2: Command Based Creation

1. Place a chest with a sign attached to it.
2. Hold the item you want to sell in your main hand.
3. Look directly at the chest (within 5 blocks).
4. Run `/cm create <type> <price> [sellPrice]`

Examples:
```
/cm create buy 10.00          Creates a BUY shop at $10.00 per item
/cm create sell 5.00           Creates a SELL shop at $5.00 per item
/cm create both 10.00 5.00    Creates a BUY+SELL shop, buy at $10, sell at $5
```

### What Happens During Creation

When you create a shop, the plugin checks several things:

1. You have the `chestmarket.create` permission.
2. Your account is not frozen by an admin.
3. The world allows shop creation (not in a restricted world).
4. The WorldGuard region allows shops (if WorldGuard is installed).
5. You have not reached your maximum shop limit.
6. No shop already exists at that chest.
7. The item you are holding is not on the blacklist.
8. The price is within the configured minimum and maximum range.
9. You can afford the creation fee (if one is configured).

If all checks pass, the creation fee is deducted from your balance, the shop is saved to the database, and the hologram is spawned.

---

## Using Shops

### Buying from a Shop

Right click or left click on a BUY shop chest or sign. The plugin will immediately show you the item name, price, and current stock in chat and ask how many you want to buy. Type a number and press enter. A confirmation screen appears showing the total cost. Confirm and the items move from the shop chest to your inventory.

On Paper servers with Dialog API support, the confirmation appears as a native dialog box instead of a chest GUI.

### Selling to a Shop

Right click or left click on a SELL shop chest or sign. The plugin shows you how many matching items you have and asks how many you want to sell. Type a number and press enter, then confirm the transaction.

### BUY+SELL Shops

Shops that do both buying and selling open a GUI when you click them, allowing you to choose whether you want to buy from the shop or sell to it.

### Stock

Shops are stocked from the actual chest inventory. If a BUY shop runs out of items in its chest, it will show as "OUT OF STOCK" on the hologram and players will not be able to purchase. Shop owners need to manually restock by placing more items in the chest.

For SELL shops, if the chest becomes full, players will not be able to sell more items to it.

---

## Commands

ChestMarket+ uses `/cm` as the main command. You can also use any of these aliases: `/chestmarket`, `/cmarket`, `/chestmarketplus`, `/cshop`, `/cs`.

### Player Commands

| Command | Description |
|---------|-------------|
| `/cm help [page]` | Shows the help menu. Page 1 has player commands, page 2 has admin commands (only visible to admins). |
| `/cm create <buy\|sell\|both> <price> [sellPrice]` | Creates a new shop while looking at a chest. |
| `/cm delete` | Deletes your shop while looking at it. |
| `/cm setprice <buy\|sell> <price>` | Changes the buy or sell price on your shop. |
| `/cm transfer <player>` | Transfers shop ownership to another online player. |
| `/cm trust <player>` | Grants a player access to your shop chest. |
| `/cm untrust <player>` | Revokes a player's access to your shop chest. |
| `/cm info` | Displays detailed information about the shop you are looking at. |
| `/cm log [page]` | Opens a GUI showing the transaction history for your shop. |
| `/cm favorites` | Opens your favorites list GUI. |
| `/cm follow` | Follows a shop so you get notified when it restocks. |
| `/cm unfollow` | Stops following a shop. |
| `/cm notify [on\|off]` | Toggles whether you receive shop notifications. |
| `/cm holograms [on\|off]` | Toggles whether you can see shop holograms. |
| `/cm setitem` | Opens a GUI to change your shop's item (look at your shop first). |

### Admin Commands

| Command | Description |
|---------|-------------|
| `/cm admin delete <shopId>` | Force deletes any shop by its ID number. |
| `/cm admin edit <shopId>` | Opens the edit GUI for any shop. |
| `/cm admin setprice <shopId> <buy\|sell> <price>` | Overrides the price on any shop. |
| `/cm admin list [player]` | Lists all shops, or all shops owned by a specific player. |
| `/cm admin freeze <player>` | Prevents a player from creating or using shops. |
| `/cm admin unfreeze <player>` | Restores a frozen player's shop privileges. |
| `/cm admin tp <shopId>` | Teleports you to a shop's location. |
| `/cm admin forcerestock <shopId>` | Fills a shop's chest to maximum capacity. |
| `/cm admin info <shopId>` | Shows detailed admin level information about a shop including transaction stats and revenue. |
| `/cm admin stats` | Displays server wide economy statistics. |
| `/cm reload` | Reloads the plugin configuration and locale files. |
| `/cm config` | Opens the in game configuration editor GUI. |

### Tab Completion

All commands have full tab completion. The plugin intelligently suggests valid options based on what you have typed. Admin commands and the admin help page only appear in tab complete for players who have the relevant permissions. Price suggestions are provided when creating shops (1.00, 5.00, 10.00, 50.00, 100.00).

---

## Permissions

### Player Permissions

| Permission | Description | Default |
|-----------|-------------|---------|
| `chestmarket.use` | Allows interacting with shops (buying, selling) | Everyone |
| `chestmarket.create` | Allows creating player shops | Everyone |
| `chestmarket.create.admin` | Allows creating admin shops with infinite stock | OP only |
| `chestmarket.limit.<number>` | Sets a custom shop limit for a player (e.g. `chestmarket.limit.25` allows 25 shops) | Not set |
| `chestmarket.bypass.blacklist` | Allows selling blacklisted items | OP only |
| `chestmarket.bypass.tax` | Exempts from transaction tax | Disabled |
| `chestmarket.bypass.fee` | Exempts from shop creation fee | Disabled |
| `chestmarket.bypass.worldrestrict` | Allows shop creation in restricted worlds | OP only |
| `chestmarket.notify.update` | Receives notifications about new plugin versions | OP only |

### Admin Permissions

| Permission | Description | Default |
|-----------|-------------|---------|
| `chestmarket.admin.bypass` | Allows opening any shop chest regardless of ownership | OP only |
| `chestmarket.admin.delete` | Allows force deleting any shop | OP only |
| `chestmarket.admin.edit` | Allows editing any shop's settings | OP only |
| `chestmarket.admin.setprice` | Allows overriding any shop's price | OP only |
| `chestmarket.admin.list` | Allows listing all shops | OP only |
| `chestmarket.admin.freeze` | Allows freezing and unfreezing players | OP only |
| `chestmarket.admin.tp` | Allows teleporting to any shop | OP only |
| `chestmarket.admin.restock` | Allows force restocking any shop | OP only |
| `chestmarket.admin.info` | Allows viewing detailed shop information | OP only |
| `chestmarket.admin.stats` | Allows viewing server economy statistics | OP only |
| `chestmarket.admin.reload` | Allows reloading the plugin configuration | OP only |
| `chestmarket.admin.config` | Allows opening the in game config GUI | OP only |

---

## Configuration

The main configuration file is located at `plugins/ChestMarketPlus/config.yml`. All options have sensible defaults. The plugin includes a **config versioning system** that automatically migrates your config when new options are added in updates, so you never lose your settings.

### General Settings

```yaml
config-version: 1
prefix: "<gray>[<gold>ChestMarket+<gray>] "
language: en_US
```

**config-version** is managed automatically by the plugin. Do not change it manually.

**prefix** is the text shown before all plugin messages. It supports MiniMessage formatting. Change it to match your server's theme.

**language** determines which locale file is loaded from the `locale/` folder. Set it to the filename without the `.yml` extension (e.g. `en_US` or `es_ES`).

### Trigger Words

```yaml
shop-creation-trigger-words:
  - "[ChestMarket]"
  - "[ChestShop]"
  - "[Shop]"
  - "[CM]"
  - "[CM+]"
  - "[Market]"
  - "[Buy]"
  - "[Sell]"
```

These are the words players can write on the first line of a sign to start the shop creation process. They are case insensitive and the brackets are optional. You can add or remove entries to match your server's preferences.

### Economy

```yaml
economy:
  use-vault: true
  starting-balance: 0.0
  currency-symbol: "$"
  currency-name: "dollars"
  decimal-places: 2
```

**use-vault** controls whether the plugin tries to use Vault for economy. If Vault is not installed, the built in economy is used regardless of this setting.

**starting-balance** is the initial balance for new players when using the built in economy.

**currency-symbol** is displayed before prices (e.g. `$100.00`).

**decimal-places** controls how many decimal places are shown in prices. Can be 0, 1, or 2.

### Shops

```yaml
shops:
  default-max-shops: 10
  creation-fee: 100.0
  tax-rate: 5.0
  global-min-price: 0.01
  global-max-price: 1000000.0
```

**default-max-shops** is the base number of shops each player can own. Override per player using the `chestmarket.limit.<number>` permission.

**creation-fee** is the cost to create a shop. Set to 0 to make shop creation free. Players with `chestmarket.bypass.fee` skip this cost.

**tax-rate** is the percentage taken from each transaction. For example, a rate of 5.0 means 5% tax. Set to 0 for no tax. Players with `chestmarket.bypass.tax` are exempt.

**global-min-price** and **global-max-price** define the allowed price range for all shops.

### Expiry

```yaml
expiry:
  enabled: true
  duration-days: 30
  warn-days-before: 3
  auto-delete-expired: false
```

**enabled** turns the expiry system on or off. When disabled, shops never expire.

**duration-days** is how many days after creation a shop expires.

**warn-days-before** is how many days before expiry the owner receives a warning message.

**auto-delete-expired** controls what happens when a shop expires. If false, the shop is deactivated but can be renewed. If true, it is permanently deleted.

### Display

```yaml
display:
  enabled: true
  render-distance: 16
  item-rotation-speed: 5.0
  scrolling-text-speed: 40
  out-of-stock-text: "<red><bold>OUT OF STOCK"
```

**enabled** turns the hologram display system on or off globally.

**render-distance** is how many blocks away players can see shop holograms. Higher values use more server resources.

**item-rotation-speed** controls how fast the floating item rotates in degrees per tick. Default is 10. Set to 0 to disable rotation.

**scrolling-text-speed** controls how many ticks between each scroll step for long item names (names over 16 characters). Default is 40 ticks. Set to 0 to disable scrolling.

**out-of-stock-text** is the text shown on the hologram when a shop has no items in stock. Supports MiniMessage formatting.

### Signs

```yaml
signs:
  auto-color: true
  require-crouch: false
  buy-color: "<green>"
  sell-color: "<red>"
  both-color: "<yellow>"
```

**auto-color** determines whether signs are automatically colored based on the shop type.

**require-crouch** when set to true, players must be sneaking to create a shop via sign. This prevents accidental shop creation.

The color options control the color used for the owner name on the sign for each shop type.

### Items

```yaml
items:
  blacklist:
    - "BEDROCK"
    - "BARRIER"
    - "COMMAND_BLOCK"
  blacklist-bypass-permission: "chestmarket.bypass.blacklist"
  whitelist:
    enabled: false
    mode: "blacklist"
```

See the [Item Blacklist and Whitelist](#item-blacklist-and-whitelist) section for details.

### Protection

```yaml
protection:
  chest-protection: true
  admin-bypass-permission: "chestmarket.admin.bypass"
```

**chest-protection** when enabled prevents anyone except the shop owner and trusted players from opening the shop chest directly.

**allow-chest-peek** when enabled, non-owners can sneak and right-click a shop chest or sign to open a read-only snapshot of the chest inventory. They can see what items are inside but cannot take anything. Disabled by default. Toggle in-game via `/cm config`.

### WorldGuard

```yaml
worldguard:
  enabled: true
  flag-name: "chest-shop"
  default-value: true
```

See the [WorldGuard Integration](#worldguard-integration) section for details.

### World Restrictions

```yaml
worlds:
  mode: "blacklist"
  list: []
```

See the [World Restrictions](#world-restrictions) section for details.

### Notifications

```yaml
notifications:
  default-enabled: true
  sound:
    buy: "ENTITY_EXPERIENCE_ORB_PICKUP"
    sell: "ENTITY_VILLAGER_YES"
  particles:
    transaction-burst: true
    particle-type: "VILLAGER_HAPPY"
    count: 15
```

**default-enabled** sets whether new players have notifications enabled by default. Players can toggle this with `/cm notify`.

**sound** options control which sounds play when a transaction occurs. Use Bukkit Sound enum names.

**particles** options control the particle burst effect on transactions. Set `transaction-burst` to false to disable particles.

### Ratings

```yaml
ratings:
  enabled: true
  mode: "on"
```

**enabled** turns the rating system on or off.

**mode** can be `on` or `off`.

### Discord

```yaml
discord:
  webhook-url: ""
  admin-events: true
```

**webhook-url** is the URL of a Discord webhook to send admin event notifications. Leave empty to disable.

**admin-events** controls whether admin actions (freeze, force delete, etc.) are sent to Discord.

### Update Checker

```yaml
update-checker:
  enabled: true
  notify-in-game: true
```

**enabled** controls whether the plugin checks for new versions on startup.

**notify-in-game** controls whether players with `chestmarket.notify.update` are notified when a new version is available.

### bStats

```yaml
bstats:
  enabled: true
```

**enabled** controls whether anonymous usage statistics are sent to bStats. This helps the developer understand how the plugin is being used.

---

## Economy System

ChestMarket+ supports two economy modes.

### Vault Economy (Recommended)

When Vault is installed alongside an economy provider like EssentialsX, CMI, or CraftConomy, ChestMarket+ automatically uses it for all monetary transactions. This means player balances are shared across all plugins on your server.

### Built In Economy

If Vault is not installed, ChestMarket+ falls back to its own simple economy system backed by the SQLite database. This is mainly intended for testing or servers that do not use a traditional economy plugin. Note that the built in economy balances only persist while the server is running.

### How Transactions Work

When a player buys from a shop:
1. The buyer's balance is checked.
2. The full price is deducted from the buyer.
3. Tax is calculated (price * tax rate / 100).
4. The owner receives the price minus tax.
5. Items are moved from the chest to the buyer's inventory.

When a player sells to a shop:
1. The owner's balance is checked (they need funds to pay the seller).
2. Items are moved from the seller's inventory to the chest.
3. The seller receives the sell price.
4. Tax is deducted from the payment.

---

## Holograms and Displays

Shop holograms are floating text and item displays that appear above each shop chest, showing the item being sold, prices, stock level, and owner name.

### Paper Servers (Recommended)

On Paper 1.19.4+, the plugin uses native **Display Entities** (TextDisplay and ItemDisplay). These provide smoother rendering, better performance, and support item rotation animations.

The text appears just above the chest lid and the floating item spins above it at 2.2 blocks height. The item rotates smoothly at the configured rotation speed (default 10 degrees per tick). The text display is scaled to a readable size and faces the player from any angle.

When the item name is longer than 16 characters, the hologram scrolls the name horizontally in a loop (e.g. "Waxed Weathered Cut..." scrolls through the full name continuously). The scroll speed is controlled by `display.scrolling-text-speed`.

### Spigot Servers

On Spigot, the plugin falls back to **Armor Stands** and **dropped items**. Two invisible armor stands display the shop text and owner name, and a dropped item entity floats above with gravity disabled. This approach works but does not support smooth rotation.

### Hologram Content

The hologram shows:
```
Item Name (bold white)
B: $10.00 | S: $5.00 (green/red)
Stock: 64 (gray)
OwnerName (gray)
```

When the shop is out of stock, the hologram changes to show the configured "OUT OF STOCK" text in red, and the floating item is replaced with a barrier block on Paper.

### Per Player Visibility

Players can hide all shop holograms for themselves using `/cm holograms off`. The holograms disappear immediately without requiring a relog. Use `/cm holograms on` to show them again. The setting is saved to the database and persists across sessions.

---

## Sign Format

When a shop is created, the sign is automatically formatted:

**Line 1:** The shop owner's name, colored based on shop type (green for BUY, red for SELL, yellow for BUY+SELL).

**Line 2:** The item name, truncated to 15 characters if needed.

**Line 3:** The buy price (e.g. `B: $10.00`) or `B: N/A` if the shop does not buy.

**Line 4:** The sell price (e.g. `S: $5.00`) or `S: N/A` if the shop does not sell.

Signs that belong to existing shops cannot be edited by players. Attempting to edit a shop sign will cancel the edit.

---

## Shop Protection

When chest protection is enabled (the default), shop chests are protected in several ways:

**Break Protection:** Players cannot break a shop chest or its sign. If the shop owner tries to break the chest, they will receive a confirmation prompt asking if they want to delete the shop.

**Explosion Protection:** Shop chests and signs are protected from both entity explosions (creepers, TNT, etc.) and block explosions (beds in the nether, respawn anchors).

**Chest Access:** Only the shop owner and trusted players can directly open the shop chest inventory. Other players who right click the chest will see the shop purchase GUI instead. Admins with `chestmarket.admin.bypass` can always access any chest.

---

## Trust System

Shop owners can grant other players direct access to their shop chest using the trust system.

**Adding a trusted player:** Look at your shop and run `/cm trust <player>`. The trusted player will now be able to open the chest inventory directly, allowing them to restock or manage the shop contents.

**Removing a trusted player:** Look at your shop and run `/cm untrust <player>`.

Trusted players can access the chest but cannot change shop settings, prices, or delete the shop.

---

## WorldGuard Integration

If WorldGuard is installed and enabled in the config, ChestMarket+ registers a custom region flag called `chest-shop` (configurable name).

### How It Works

By default, the flag is set to ALLOW globally, meaning shops can be created everywhere. Server admins can deny shop creation in specific regions:

```
/rg flag <region-name> chest-shop deny
```

To re allow shops in a region:

```
/rg flag <region-name> chest-shop allow
```

The flag check is performed when a player attempts to create a shop. Existing shops in a region are not affected if the flag is changed after creation.

---

## World Restrictions

You can restrict which worlds shops can be created in using two modes.

### Blacklist Mode (Default)

In blacklist mode, shops can be created in all worlds EXCEPT those listed:

```yaml
worlds:
  mode: "blacklist"
  list:
    - "world_nether"
    - "world_the_end"
```

### Whitelist Mode

In whitelist mode, shops can ONLY be created in the listed worlds:

```yaml
worlds:
  mode: "whitelist"
  list:
    - "world"
    - "world_shops"
```

Players with `chestmarket.bypass.worldrestrict` can create shops in any world regardless of these restrictions.

---

## Shop Expiry

When the expiry system is enabled, shops have a limited lifespan.

### How It Works

Each shop is given an expiration date when created, calculated as the current time plus the configured `duration-days`. The plugin checks for expired shops every 5 minutes.

### Warnings

When a shop is within the `warn-days-before` threshold of expiring, the owner receives a warning message the next time they are online. The message includes the shop ID and remaining time.

### What Happens on Expiry

If `auto-delete-expired` is **false** (the default), the shop is deactivated. The hologram updates to show it is inactive, and players cannot buy or sell. The owner can be notified to take action.

If `auto-delete-expired` is **true**, the shop is permanently deleted from the database along with its hologram and sign formatting.

---

## Notifications

ChestMarket+ has a comprehensive notification system to keep shop owners informed about activity.

### Transaction Notifications

When a player buys from or sells to your shop, you receive an action bar message showing who, what, and how much. A sound effect plays and particles burst around the shop chest.

### Offline Notifications

If you are offline when a transaction occurs, the notification is queued in the database. When you log in, you will receive all pending notifications listed in chat.

### Restock Notifications

If you follow a shop using `/cm follow`, you will be notified when that shop is restocked. Restocking happens when someone sells items to a BUY shop through the shop transaction system, or when the shop owner manually adds items to the chest. This is useful for tracking shops that sell items you want to buy.

### Toggling Notifications

Players can disable all notifications with `/cm notify off` and re enable them with `/cm notify on`. This setting is saved per player and persists across sessions.

---

## Discord Integration

ChestMarket+ can send notifications to a Discord channel using webhooks.

### Setup

1. In your Discord server, go to a channel's settings and create a Webhook.
2. Copy the webhook URL.
3. Paste it into your config.yml under `discord.webhook-url`.
4. Reload the plugin with `/cm reload`.

### What Gets Sent

When `admin-events` is enabled, the following events are sent to Discord:

1. Player frozen by admin
2. Player unfrozen by admin
3. Shop force deleted by admin
4. Shop force restocked by admin

Each notification includes the admin's name and the target player or shop.

---

## Favorites and Following

### Favorites

You can bookmark shops you frequently use. Right click a shop to open its GUI, then click the favorite button (gold ingot). View all your favorites with `/cm favorites`, which opens a paginated GUI showing all your saved shops with their details.

### Following

Follow a shop with `/cm follow` (while looking at it) to receive notifications whenever that shop is restocked. This is especially useful for shops that sell rare or high demand items. Unfollow with `/cm unfollow`.

---

## Rating System

When ratings are enabled, players can rate shops with a thumbs up or thumbs down after making a purchase. Each player can only rate each shop once, but they can change their rating. Shop ratings help other players identify trusted and popular shops.

---

## Quick Sell

The quick sell feature allows players to sell all matching items from their inventory to a shop in one action, instead of selling one at a time. When activated, the plugin scans your inventory for all items matching the shop's item type, shows a confirmation dialog with the total count and price, and processes the entire sale at once upon confirmation.

---

## Item Blacklist and Whitelist

### Blacklist

By default, certain items are blocked from being sold in shops. The default blacklist includes:

```
BEDROCK, BARRIER, COMMAND_BLOCK, COMMAND_BLOCK_MINECART,
CHAIN_COMMAND_BLOCK, REPEATING_COMMAND_BLOCK, STRUCTURE_BLOCK,
STRUCTURE_VOID, JIGSAW, DEBUG_STICK
```

You can add or remove items from the blacklist in the config. Use Bukkit Material names.

Players with the `chestmarket.bypass.blacklist` permission can sell blacklisted items.

### Whitelist Mode

If you prefer to only allow specific items, set `items.whitelist.enabled` to true and `items.whitelist.mode` to `whitelist`. In this mode, only items listed in the blacklist field are allowed, and everything else is blocked.

---

## Admin Tools

ChestMarket+ provides a full suite of admin tools for server management.

### Viewing Shop Info

`/cm admin info <shopId>` shows everything about a shop: owner name and UUID, item type, shop type, prices, current stock, whether it is an admin shop, active status, creation and expiry timestamps, location coordinates, total transaction count, and total revenue generated.

### Server Statistics

`/cm admin stats` shows a server wide summary including total active shops, total transactions, total volume traded, and total tax collected across all shops.

### Managing Players

Use `/cm admin freeze <player>` to prevent a problematic player from creating or using shops. Their existing shops remain but they cannot interact with the system. Use `/cm admin unfreeze <player>` to restore access.

### Shop Management

`/cm admin delete <shopId>` force deletes any shop. `/cm admin forcerestock <shopId>` fills a shop's chest completely. `/cm admin tp <shopId>` teleports you to any shop for inspection. `/cm admin setprice <shopId> <buy|sell> <price>` overrides pricing.

### Listing Shops

`/cm admin list` shows the total shop count. `/cm admin list <player>` shows all shops owned by a specific player with IDs, items, locations, and active status.

---

## Config GUI

Admins with `chestmarket.admin.config` can open an in game configuration editor with `/cm config`.

### Number Settings (Click to Edit)

The top row contains number value settings. Clicking any of them closes the GUI and prompts you to type the new value in chat. Type a number and press enter. Type `cancel` to abort and return to the GUI.

| Setting | Description |
|---------|-------------|
| Creation Fee | The cost to create a shop |
| Tax Rate (%) | The transaction tax percentage |
| Max Shops | The default shop limit per player |
| Min Price | The minimum allowed shop price |
| Max Price | The maximum allowed shop price |
| Render Distance | How far away holograms are visible |

### Toggle Settings (Click to Toggle)

The middle row contains boolean settings that flip between enabled and disabled when clicked:

Shop Expiry, Holograms, Chest Protection, Sign Auto Color, Require Crouch, Notifications, Ratings, WorldGuard, Update Checker, and Chest Peek.

### Other Buttons

The Reload button saves all changes and reloads the plugin. The Close button exits the GUI.

All changes made in the Config GUI are immediately saved to `config.yml` and applied.

---

## Localization

ChestMarket+ supports multiple languages. Language files are stored in `plugins/ChestMarketPlus/locale/` as YAML files.

### Included Languages

The plugin ships with **English** (`en_US.yml`) and **Spanish** (`es_ES.yml`).

### Switching Languages

Change the `language` setting in `config.yml` to match the filename of your desired locale (without the `.yml` extension), then run `/cm reload`.

### Creating Custom Translations

Copy `en_US.yml` to a new file (e.g. `fr_FR.yml`), translate all the message values, and set `language: fr_FR` in your config. All messages support MiniMessage formatting for colors and styles.

### Available Placeholders

Messages can use these placeholders which are automatically replaced:

| Placeholder | Replaced With |
|------------|---------------|
| `{player}` | Player name |
| `{item}` | Item display name |
| `{price}` | Formatted price |
| `{quantity}` | Item quantity |
| `{action}` | "BUY" or "SELL" |
| `{count}` | A number count |
| `{time}` | Duration string |
| `{id}` | Shop ID |
| `{current}` | Current value |
| `{max}` | Maximum value |
| `{fee}` | Fee amount |
| `{min}` | Minimum price |
| `{owner}` | Shop owner name |
| `{stock}` | Stock count |
| `{type}` | Price type |
| `{version}` | Plugin version |

---

## Platform Compatibility

### Server Software

| Platform | Support Level |
|----------|--------------|
| Paper 1.21+ | Full support with all features |
| Spigot 1.21+ | Full support with visual fallbacks |

### Feature Differences

| Feature | Paper | Spigot |
|---------|-------|--------|
| Display Entities (TextDisplay, ItemDisplay) | Yes | No (uses ArmorStands) |
| Item Rotation Animation | Smooth rotation | No rotation |
| Dialog API (native confirmations) | Yes (1.21.7+) | No (uses chest GUI) |
| MiniMessage formatting | Full support | Converted to legacy colors |

### Dependencies

| Dependency | Required | Purpose |
|-----------|----------|---------|
| Java 21 | Yes | Runtime |
| Vault 1.7+ | No | Economy integration |
| WorldGuard 7.0+ | No | Region protection flags |

---

## Frequently Asked Questions

### How do I change the maximum number of shops a player can have?

The default limit is set in `config.yml` under `shops.default-max-shops`. To give specific players or groups a different limit, use the permission `chestmarket.limit.<number>`. For example, giving a player `chestmarket.limit.25` allows them to own 25 shops.

### Why are my holograms showing raw text tags like `<red>`?

This happens on Spigot servers where MiniMessage tags are not natively supported. The plugin converts these to legacy color codes, but if you see raw tags, try running `/cm reload` to refresh the displays.

### Can I use ChestMarket+ without Vault?

Yes. If Vault is not installed, the plugin uses its own built in economy. However, this is a simple system and is recommended mainly for testing. For a production server, install Vault with an economy plugin like EssentialsX.

### How do I make a shop that never runs out of stock?

Admin shops have infinite stock. This feature requires the `chestmarket.create.admin` permission. Admin shops are intended for server shops, not player shops.

### Can players edit signs on existing shops?

No. Once a shop is created, its sign is locked. Attempting to edit it will be automatically cancelled. This prevents players from tampering with shop information.

### How do I delete a shop?

Shop owners can look at their shop and run `/cm delete`, or break the shop's chest or sign (which will trigger a confirmation dialog). Admins can force delete any shop with `/cm admin delete <shopId>`.

### What happens to items in a shop chest when the shop is deleted?

The items remain in the chest. Deleting a shop only removes the shop data, hologram, and sign formatting. The physical chest and its contents are untouched.

### How do I restrict shops to certain areas?

Use WorldGuard regions with the `chest-shop` flag set to deny, or use the world restriction system in config.yml to blacklist or whitelist entire worlds.

### Does the plugin support double chests?

Yes. ChestMarket+ fully supports double chests. When a shop is created on a chest that is part of a double chest, clicking either half of the double chest will correctly open the shop. The full combined inventory is used for stock counting.

### How often does the expiry check run?

The expiry system checks for expired shops every 5 minutes (6000 ticks). This is not configurable to prevent performance issues from too frequent checks.

### Can I change the item my shop sells after creation?

Yes. Look at your shop chest or sign and run `/cm setitem`. A GUI opens with the current shop item in the center slot. Drag any item from your inventory into the slot to replace it, then click Confirm. Click Cancel or close the GUI to abort — your original item is safely returned.

### Can players peek inside a shop chest without buying?

Yes, if the server admin has enabled `protection.allow-chest-peek` in the config (or via `/cm config`). When enabled, non-owners can **sneak and right-click** a shop chest or sign to open a read-only view of the chest contents. They cannot take items from this view.

### Why does my sign look blank after I try to break it?

If a player attempts to break a shop sign, the break is cancelled and the sign text is automatically restored on the next server tick. This requires no action from the player — the text reappears instantly.
