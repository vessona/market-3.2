package com.example.admin.market.ui.bookmarks;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.example.admin.market.R;

/**
 * Bookmark item list view.
 */
final class BookmarkView extends FrameLayout {

    /**
     * Called when bookmark checked
     */
    interface OnCheckListener {
        void onChecked(boolean checked);
    }

    /**
     * Bookmark title.
     */
    private final TextView titleView;

    private final CheckBox checkBox;

    private OnCheckListener checkListener;

    /**
     * Create new bookmark view.
     *
     * @param context The context
     */
    BookmarkView(final Context context) {
        super(context);
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        View.inflate(getContext(), R.layout.item_bookmark, this);
        this.titleView = findViewById(R.id.bookmark_title);
        this.checkBox = findViewById(R.id.bookmark_checkbox);
        this.checkBox.setOnCheckedChangeListener((view, checked) -> {
            if (checkListener != null) {
                checkListener.onChecked(checked);
            }
        });
    }

    /**
     * Change a title.
     *
     */
    void changeTitle(String title) {
        this.titleView.setText(title);
    }

    /**
     * Set checkbox.
     */
    void setChecked(boolean checked) {
        this.checkBox.setChecked(checked);
    }

    void setOnCheckedListener(OnCheckListener listener) {
        checkListener = listener;
    }
}
