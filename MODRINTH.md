# ChestMarket+

**The most advanced free chest shop plugin for Minecraft 1.21+**

ChestMarket+ lets players create feature rich shops using signs and chests. It supports both Paper and Spigot servers, includes floating 3D item displays with holograms, an in game GUI for shop creation and management, full economy integration through Vault, and a comprehensive admin toolkit.

## Features

### Shop System
Create shops by placing a sign on a chest and selecting your shop type through an intuitive GUI. Choose between BUY shops (players purchase from you), SELL shops (you purchase from players), or BUY+SELL shops that do both. Prices are entered through a simple chat prompt, and the sign is automatically formatted with your name, item, and prices.

### Floating Holograms
Every shop gets a floating hologram above its chest showing the item name, buy and sell prices, current stock level, and owner name. On Paper servers, the item actually floats and rotates above the shop using Display Entities. On Spigot servers, armor stands are used as a visual fallback. Players can toggle holograms on or off for themselves.

### Economy Integration
Works seamlessly with Vault and any economy plugin (EssentialsX, CMI, CraftConomy, etc.). Includes configurable shop creation fees, per transaction tax rates, and price range limits. If Vault is not installed, a built in economy provider is available.

### Full GUI Experience
Everything is done through clean inventory GUIs. Shop creation wizard, buy and sell confirmations, quantity selectors, transaction logs, favorites list, and an admin config editor. On Paper 1.21.7+, native Dialog API confirmations are used for a more polished experience.

### Protection
Shop chests are fully protected from unauthorized access, block breaking, and explosions. Only shop owners and trusted players can access the chest inventory. WorldGuard integration adds a custom region flag for per area control.

### Admin Toolkit
A complete set of admin commands for managing the shop economy: force delete shops, teleport to any shop, force restock, freeze problematic players, view detailed shop info and revenue statistics, and edit live config through an in game GUI.

### Notifications
Shop owners receive instant notifications when someone buys or sells, complete with sound effects and particle bursts. Offline notifications are queued and delivered on login. Players can follow shops to get notified when they restock.

### And More
Shop expiry system with configurable duration and warnings, favorites and shop following, thumbs up/down ratings, Discord webhook for admin events, item blacklists, world restrictions, multi language support, and full tab completion on every command.

---

## Quick Start Guide

### For Players

**Creating a Shop**
1. Hold the item you want to sell in your hand.
2. Place a sign on or next to a chest.
3. Write `[Shop]` on the first line of the sign.
4. Select BUY, SELL, or BUY+SELL in the GUI that opens.
5. Type your price in chat when prompted.
6. Your shop is ready!

**Buying from a Shop**
Right click on a shop chest to open the shop GUI. Click the buy button, confirm the purchase, and the items are yours.

**Selling to a Shop**
Right click on a SELL or BUY+SELL shop chest, click the sell button, and confirm. Items move from your inventory to the shop and you get paid.

**Useful Commands**
```
/cm help              View all available commands
/cm info              View details about a shop you're looking at
/cm favorites         Open your bookmarked shops
/cm notify off        Disable transaction notifications
/cm holograms off     Hide shop holograms
```

### For Server Admins

**Installation**
Drop the jar into your `plugins/` folder and restart. The plugin generates all config files automatically.

**Economy Setup**
Install Vault and an economy plugin (like EssentialsX) for the best experience. ChestMarket+ detects Vault automatically.

**Configuring**
Edit `plugins/ChestMarketPlus/config.yml` or use the in game config GUI with `/cm config`. Key settings include:

```yaml
shops:
  default-max-shops: 10     # How many shops each player can own
  creation-fee: 100.0       # Cost to create a shop (0 = free)
  tax-rate: 5.0             # Transaction tax percentage

display:
  enabled: true             # Show floating holograms
  render-distance: 16       # How far away holograms are visible

expiry:
  enabled: true             # Shops expire after a set time
  duration-days: 30         # Days until expiry
```

**Managing Shops**
```
/cm admin list              See total shop count
/cm admin list <player>     See all shops by a player
/cm admin info <id>         Detailed info including revenue
/cm admin delete <id>       Force remove a shop
/cm admin freeze <player>   Block a player from using shops
/cm admin stats             Server wide economy stats
/cm reload                  Reload config and locale files
```

---

## Commands

| Command | What It Does |
|---------|-------------|
| `/cm help [page]` | Show help (page 2 = admin commands) |
| `/cm create <type> <price>` | Create a shop via command |
| `/cm delete` | Delete your shop |
| `/cm setprice <buy\|sell> <price>` | Change a shop's price |
| `/cm transfer <player>` | Give your shop to someone |
| `/cm trust <player>` | Let someone access your chest |
| `/cm untrust <player>` | Revoke chest access |
| `/cm info` | View shop details |
| `/cm log` | View transaction history |
| `/cm favorites` | Open saved shops list |
| `/cm follow` / `unfollow` | Get restock notifications |
| `/cm notify [on\|off]` | Toggle notifications |
| `/cm holograms [on\|off]` | Toggle hologram visibility |
| `/cm admin delete <id>` | Force delete any shop |
| `/cm admin list [player]` | List shops |
| `/cm admin freeze <player>` | Freeze a player |
| `/cm admin tp <id>` | Teleport to a shop |
| `/cm admin stats` | Economy statistics |
| `/cm reload` | Reload configuration |
| `/cm config` | Open config GUI |

Command aliases: `/chestmarket`, `/cshop`, `/cs`, `/shop`, `/market`

---

## Permissions

**Player Permissions**

| Permission | Description |
|-----------|-------------|
| `chestmarket.use` | Use shops (default: true) |
| `chestmarket.create` | Create shops (default: true) |
| `chestmarket.limit.<n>` | Custom shop limit |
| `chestmarket.bypass.blacklist` | Ignore item blacklist |
| `chestmarket.bypass.tax` | Skip transaction tax |
| `chestmarket.bypass.fee` | Skip creation fee |

**Admin Permissions**

| Permission | Description |
|-----------|-------------|
| `chestmarket.admin.delete` | Force delete shops |
| `chestmarket.admin.edit` | Edit any shop |
| `chestmarket.admin.list` | List all shops |
| `chestmarket.admin.freeze` | Freeze players |
| `chestmarket.admin.tp` | Teleport to shops |
| `chestmarket.admin.restock` | Force restock |
| `chestmarket.admin.stats` | View stats |
| `chestmarket.admin.reload` | Reload config |
| `chestmarket.admin.config` | Config GUI |
| `chestmarket.admin.bypass` | Bypass chest protection |

---

## Compatibility

| | Supported |
|---|-----------|
| **Server Software** | Paper 1.21+, Spigot 1.21+ |
| **Java Version** | Java 21+ |
| **Economy** | Vault (optional, any provider) |
| **Region Protection** | WorldGuard 7.0+ (optional) |

Paper is recommended for the best experience with native Display Entities and Dialog API support.

---

## Support

Found a bug or have a feature request? Open an issue on the GitHub repository.
