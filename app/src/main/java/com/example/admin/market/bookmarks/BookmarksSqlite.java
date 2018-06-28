package com.example.admin.market.bookmarks;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.admin.market.R;
import com.example.admin.market.sqlite.Sqlite;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public final class BookmarksSqlite implements Bookmarks {

    private static volatile BookmarksSqlite instance; //thread safety (https://stackoverflow.com/questions/3519664/difference-between-volatile-and-synchronized-in-java)
    public static BookmarksSqlite getInstance(Context ctx) {
        if (instance == null) {
            synchronized (BookmarksSqlite.class) {
                if (instance == null) {
                    instance = new BookmarksSqlite(
                        new Sqlite(ctx.getApplicationContext(),
                            ctx.getResources().getInteger(R.integer.db_version)
                        )
                    );
                }
            }
        }
        return instance;
    }


    private static final String TABLE = "bookmarks";


    private static final String COL_MARKET = "market_id";

    private static final String COL_TITLE = "title";

    private final SQLiteOpenHelper dbHelper;


    private BookmarksSqlite(final SQLiteOpenHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    @Override
    public void bookmark( final String marketId, final String title) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.insertWithOnConflict(
                TABLE,
                null,
                contentValues(marketId, title),
                SQLiteDatabase.CONFLICT_REPLACE
            );
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void delete(final Set<String> toDelete) {
        final SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.beginTransaction();
        try {
            for (String id : toDelete) {
                database.delete(
                    TABLE,
                    String.format("%s = ?", COL_MARKET),
                    new String[]{id}
                );
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    @Override
    public List<Bookmark> bookmarks() {
        // use database for read bookmarks
        final SQLiteDatabase database = dbHelper.getReadableDatabase();
        // select bookmarks ordered by title
        try (final Cursor query = database.query( //https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html
            TABLE,
            new String[]{COL_MARKET, COL_TITLE},
            null,
            null,
            null,
            null,
            COL_TITLE// order by title
        )) {
            if (!query.moveToFirst()) {
                return Collections.emptyList();
            }
            final int idMarket = query.getColumnIndex(COL_MARKET);
            final int idTitle = query.getColumnIndex(COL_TITLE);
            final List<Bookmark> bookmarks = new ArrayList<>(query.getCount());
            do {
                bookmarks.add(
                    new Bookmark(
                        query.getString(idMarket),
                        query.getString(idTitle)
                    )
                );
            } while (query.moveToNext());
            return bookmarks;
        }
    }


    private static ContentValues contentValues(final String marketId, final String title) {
        final ContentValues values = new ContentValues(2);
        values.put(COL_MARKET, marketId);
        values.put(COL_TITLE, title);
        return values;
    }
}
