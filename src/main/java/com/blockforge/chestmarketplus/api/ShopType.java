package com.blockforge.chestmarketplus.api;

public enum ShopType {
    BUY,
    SELL,
    BUY_SELL;

    public boolean canBuy() {
        return this == BUY || this == BUY_SELL;
    }

    public boolean canSell() {
        return this == SELL || this == BUY_SELL;
    }

    public static ShopType fromString(String s) {
        if (s == null) return BUY_SELL;
        return switch (s.toUpperCase().replace("-", "_").replace(" ", "_")) {
            case "BUY" -> BUY;
            case "SELL" -> SELL;
            case "BOTH", "BUY_SELL", "BUYSELL" -> BUY_SELL;
            default -> BUY_SELL;
        };
    }
}
