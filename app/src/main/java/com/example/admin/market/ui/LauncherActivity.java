package com.example.admin.market.ui;

import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.example.admin.market.R;

public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        findViewById(R.id.btn_search).setOnClickListener(v -> MainActivity.start(this));
        findViewById(R.id.btn_bookmarks).setOnClickListener(v -> BookmarksActivity.start(this));

        ////////////
        Typeface iconFont = FontManager.getTypeface(getApplicationContext(), FontManager.FONTAWESOME);
        FontManager.markAsIconContainer(findViewById(R.id.icons_container), iconFont);
        ////////////
    }
}
