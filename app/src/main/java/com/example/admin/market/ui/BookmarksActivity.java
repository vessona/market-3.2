package com.example.admin.market.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.example.admin.market.R;
import com.example.admin.market.ui.bookmarks.BookmarksFragment;

public class BookmarksActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarks);
        getSupportFragmentManager()
            .beginTransaction()
            .add(R.id.root, BookmarksFragment.create())
            .commit();
    }

    public static void start(final Context ctx) {
        ctx.startActivity(new Intent(ctx, BookmarksActivity.class));
    }
}
