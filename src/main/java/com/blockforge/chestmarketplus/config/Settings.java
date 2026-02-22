package com.blockforge.chestmarketplus.config;

import org.bukkit.Sound;
import org.bukkit.Particle;

import java.util.List;

public class Settings {

    private int configVersion;
    private String prefix;
    private String language;
    private List<String> triggerWords;

    private boolean useVault;
    private double startingBalance;
    private String currencySymbol;
    private String currencyName;
    private int decimalPlaces;

    private int defaultMaxShops;
    private double creationFee;
    private double taxRate;
    private double globalMinPrice;
    private double globalMaxPrice;
    private int chatInputTimeout;
    private boolean allowAllQuantity;
    private boolean partialSellWhenLowFunds;

    private boolean expiryEnabled;
    private int expiryDurationDays;
    private int expiryWarnDaysBefore;
    private boolean autoDeleteExpired;

    private boolean displayEnabled;
    private int renderDistance;
    private double itemRotationSpeed;
    private int scrollingTextSpeed;
    private String outOfStockText;

    private boolean signAutoColor;
    private boolean requireCrouchForSign;
    private String buyColor;
    private String sellColor;
    private String bothColor;

    private List<String> itemBlacklist;
    private String blacklistBypassPermission;
    private boolean whitelistEnabled;
    private String whitelistMode;

    private boolean chestProtection;
    private boolean allowChestPeek;
    private String adminBypassPermission;

    private boolean worldGuardEnabled;
    private String worldGuardFlagName;
    private boolean worldGuardDefaultValue;

    private String worldRestrictionMode;
    private List<String> worldRestrictionList;

    private boolean notificationsDefaultEnabled;
    private String buySoundName;
    private String sellSoundName;
    private boolean transactionBurst;
    private String particleTypeName;
    private int particleCount;

    private boolean ratingsEnabled;
    private String ratingsMode;

    private String discordWebhookUrl;
    private boolean discordAdminEvents;

    private boolean updateCheckerEnabled;
    private boolean updateNotifyInGame;

    private boolean bstatsEnabled;

    public int getConfigVersion() { return configVersion; }
    public void setConfigVersion(int configVersion) { this.configVersion = configVersion; }

    public String getPrefix() { return prefix; }
    public void setPrefix(String prefix) { this.prefix = prefix; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public List<String> getTriggerWords() { return triggerWords; }
    public void setTriggerWords(List<String> triggerWords) { this.triggerWords = triggerWords; }

    public boolean isUseVault() { return useVault; }
    public void setUseVault(boolean useVault) { this.useVault = useVault; }

    public double getStartingBalance() { return startingBalance; }
    public void setStartingBalance(double startingBalance) { this.startingBalance = startingBalance; }

    public String getCurrencySymbol() { return currencySymbol; }
    public void setCurrencySymbol(String currencySymbol) { this.currencySymbol = currencySymbol; }

    public String getCurrencyName() { return currencyName; }
    public void setCurrencyName(String currencyName) { this.currencyName = currencyName; }

    public int getDecimalPlaces() { return decimalPlaces; }
    public void setDecimalPlaces(int decimalPlaces) { this.decimalPlaces = decimalPlaces; }

    public int getDefaultMaxShops() { return defaultMaxShops; }
    public void setDefaultMaxShops(int defaultMaxShops) { this.defaultMaxShops = defaultMaxShops; }

    public double getCreationFee() { return creationFee; }
    public void setCreationFee(double creationFee) { this.creationFee = creationFee; }

    public double getTaxRate() { return taxRate; }
    public void setTaxRate(double taxRate) { this.taxRate = taxRate; }

    public double getGlobalMinPrice() { return globalMinPrice; }
    public void setGlobalMinPrice(double globalMinPrice) { this.globalMinPrice = globalMinPrice; }

    public double getGlobalMaxPrice() { return globalMaxPrice; }
    public void setGlobalMaxPrice(double globalMaxPrice) { this.globalMaxPrice = globalMaxPrice; }

    public int getChatInputTimeout() { return chatInputTimeout; }
    public void setChatInputTimeout(int chatInputTimeout) { this.chatInputTimeout = chatInputTimeout; }

    public boolean isAllowAllQuantity() { return allowAllQuantity; }
    public void setAllowAllQuantity(boolean allowAllQuantity) { this.allowAllQuantity = allowAllQuantity; }

    public boolean isPartialSellWhenLowFunds() { return partialSellWhenLowFunds; }
    public void setPartialSellWhenLowFunds(boolean partialSellWhenLowFunds) { this.partialSellWhenLowFunds = partialSellWhenLowFunds; }

    public boolean isExpiryEnabled() { return expiryEnabled; }
    public void setExpiryEnabled(boolean expiryEnabled) { this.expiryEnabled = expiryEnabled; }

    public int getExpiryDurationDays() { return expiryDurationDays; }
    public void setExpiryDurationDays(int expiryDurationDays) { this.expiryDurationDays = expiryDurationDays; }

    public int getExpiryWarnDaysBefore() { return expiryWarnDaysBefore; }
    public void setExpiryWarnDaysBefore(int expiryWarnDaysBefore) { this.expiryWarnDaysBefore = expiryWarnDaysBefore; }

    public boolean isAutoDeleteExpired() { return autoDeleteExpired; }
    public void setAutoDeleteExpired(boolean autoDeleteExpired) { this.autoDeleteExpired = autoDeleteExpired; }

    public boolean isDisplayEnabled() { return displayEnabled; }
    public void setDisplayEnabled(boolean displayEnabled) { this.displayEnabled = displayEnabled; }

    public int getRenderDistance() { return renderDistance; }
    public void setRenderDistance(int renderDistance) { this.renderDistance = renderDistance; }

    public double getItemRotationSpeed() { return itemRotationSpeed; }
    public void setItemRotationSpeed(double itemRotationSpeed) { this.itemRotationSpeed = itemRotationSpeed; }

    public int getScrollingTextSpeed() { return scrollingTextSpeed; }
    public void setScrollingTextSpeed(int scrollingTextSpeed) { this.scrollingTextSpeed = scrollingTextSpeed; }

    public String getOutOfStockText() { return outOfStockText; }
    public void setOutOfStockText(String outOfStockText) { this.outOfStockText = outOfStockText; }

    public boolean isSignAutoColor() { return signAutoColor; }
    public void setSignAutoColor(boolean signAutoColor) { this.signAutoColor = signAutoColor; }

    public boolean isRequireCrouchForSign() { return requireCrouchForSign; }
    public void setRequireCrouchForSign(boolean requireCrouchForSign) { this.requireCrouchForSign = requireCrouchForSign; }

    public String getBuyColor() { return buyColor; }
    public void setBuyColor(String buyColor) { this.buyColor = buyColor; }

    public String getSellColor() { return sellColor; }
    public void setSellColor(String sellColor) { this.sellColor = sellColor; }

    public String getBothColor() { return bothColor; }
    public void setBothColor(String bothColor) { this.bothColor = bothColor; }

    public List<String> getItemBlacklist() { return itemBlacklist; }
    public void setItemBlacklist(List<String> itemBlacklist) { this.itemBlacklist = itemBlacklist; }

    public String getBlacklistBypassPermission() { return blacklistBypassPermission; }
    public void setBlacklistBypassPermission(String p) { this.blacklistBypassPermission = p; }

    public boolean isWhitelistEnabled() { return whitelistEnabled; }
    public void setWhitelistEnabled(boolean whitelistEnabled) { this.whitelistEnabled = whitelistEnabled; }

    public String getWhitelistMode() { return whitelistMode; }
    public void setWhitelistMode(String whitelistMode) { this.whitelistMode = whitelistMode; }

    public boolean isChestProtection() { return chestProtection; }
    public void setChestProtection(boolean chestProtection) { this.chestProtection = chestProtection; }

    public boolean isAllowChestPeek() { return allowChestPeek; }
    public void setAllowChestPeek(boolean allowChestPeek) { this.allowChestPeek = allowChestPeek; }

    public String getAdminBypassPermission() { return adminBypassPermission; }
    public void setAdminBypassPermission(String adminBypassPermission) { this.adminBypassPermission = adminBypassPermission; }

    public boolean isWorldGuardEnabled() { return worldGuardEnabled; }
    public void setWorldGuardEnabled(boolean worldGuardEnabled) { this.worldGuardEnabled = worldGuardEnabled; }

    public String getWorldGuardFlagName() { return worldGuardFlagName; }
    public void setWorldGuardFlagName(String worldGuardFlagName) { this.worldGuardFlagName = worldGuardFlagName; }

    public boolean isWorldGuardDefaultValue() { return worldGuardDefaultValue; }
    public void setWorldGuardDefaultValue(boolean worldGuardDefaultValue) { this.worldGuardDefaultValue = worldGuardDefaultValue; }

    public String getWorldRestrictionMode() { return worldRestrictionMode; }
    public void setWorldRestrictionMode(String worldRestrictionMode) { this.worldRestrictionMode = worldRestrictionMode; }

    public List<String> getWorldRestrictionList() { return worldRestrictionList; }
    public void setWorldRestrictionList(List<String> worldRestrictionList) { this.worldRestrictionList = worldRestrictionList; }

    public boolean isNotificationsDefaultEnabled() { return notificationsDefaultEnabled; }
    public void setNotificationsDefaultEnabled(boolean n) { this.notificationsDefaultEnabled = n; }

    public String getBuySoundName() { return buySoundName; }
    public void setBuySoundName(String buySoundName) { this.buySoundName = buySoundName; }

    public String getSellSoundName() { return sellSoundName; }
    public void setSellSoundName(String sellSoundName) { this.sellSoundName = sellSoundName; }

    public boolean isTransactionBurst() { return transactionBurst; }
    public void setTransactionBurst(boolean transactionBurst) { this.transactionBurst = transactionBurst; }

    public String getParticleTypeName() { return particleTypeName; }
    public void setParticleTypeName(String particleTypeName) { this.particleTypeName = particleTypeName; }

    public int getParticleCount() { return particleCount; }
    public void setParticleCount(int particleCount) { this.particleCount = particleCount; }

    public boolean isRatingsEnabled() { return ratingsEnabled; }
    public void setRatingsEnabled(boolean ratingsEnabled) { this.ratingsEnabled = ratingsEnabled; }

    public String getRatingsMode() { return ratingsMode; }
    public void setRatingsMode(String ratingsMode) { this.ratingsMode = ratingsMode; }

    public String getDiscordWebhookUrl() { return discordWebhookUrl; }
    public void setDiscordWebhookUrl(String discordWebhookUrl) { this.discordWebhookUrl = discordWebhookUrl; }

    public boolean isDiscordAdminEvents() { return discordAdminEvents; }
    public void setDiscordAdminEvents(boolean discordAdminEvents) { this.discordAdminEvents = discordAdminEvents; }

    public boolean isUpdateCheckerEnabled() { return updateCheckerEnabled; }
    public void setUpdateCheckerEnabled(boolean updateCheckerEnabled) { this.updateCheckerEnabled = updateCheckerEnabled; }

    public boolean isUpdateNotifyInGame() { return updateNotifyInGame; }
    public void setUpdateNotifyInGame(boolean updateNotifyInGame) { this.updateNotifyInGame = updateNotifyInGame; }

    public boolean isBstatsEnabled() { return bstatsEnabled; }
    public void setBstatsEnabled(boolean bstatsEnabled) { this.bstatsEnabled = bstatsEnabled; }

    public String formatPrice(double price) {
        // %,f adds comma grouping separators (e.g. 1,000.00)
        return currencySymbol + String.format("%,." + decimalPlaces + "f", price);
    }
}
