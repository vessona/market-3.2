package com.example.admin.market.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.g4s8.amigrator.SQLiteMigrations;

public final class Sqlite extends SQLiteOpenHelper {
    //https://github.com/g4s8/Android-Migrator -- this is active usage of SQLiteMigrations
    private final SQLiteMigrations migrations;

    private final int version;

    public Sqlite(final Context ctx, final int version) {
        super(ctx, "db", null, version);
        migrations = new SQLiteMigrations(ctx);
        this.version = version;
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        migrations.apply(db, 0, version);
    }

    @Override
    public void onUpgrade(
        final SQLiteDatabase db,
        final int oldVersion,
        final int newVersion
    ) {
        migrations.apply(db, oldVersion, newVersion);
    }
}
