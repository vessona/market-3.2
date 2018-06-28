package com.example.admin.market.bookmarks;

import java.util.List;
import java.util.Set;


public interface Bookmarks {

    void bookmark(String marketId, String title);
    void delete(Set<String> toDelete);
    List<Bookmark> bookmarks();

}
