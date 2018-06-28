package com.example.admin.market.bookmarks;


public final class Bookmark {

    private final String marketId;

    private final String title;

    public Bookmark(final String marketId, final String title) {
        this.marketId = marketId;
        this.title = title;
    }

    public String getMarketId() {
        return marketId;
    }
    public String getTitle() {
        return title;
    }
}
