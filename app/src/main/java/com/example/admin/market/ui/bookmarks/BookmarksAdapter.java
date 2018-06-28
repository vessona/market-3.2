package com.example.admin.market.ui.bookmarks;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.example.admin.market.bookmarks.Bookmark;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


final class BookmarksAdapter extends BaseAdapter {

    /**
     * Called when checked set changed.
     */
    public interface OnCheckedChangeListener {

        void checkedChanged(Set<String> set);
    }

    public interface OnItemClickListener {

        void onClickListener(Bookmark bookmark);
    }

    /**
     * Bookmark list.
     */
    private final List<Bookmark> bookmarks = new ArrayList<>();

    /**
     * Checked bookmarks.
     */
    private final Set<String> checked = new HashSet<>();

    /**
     * Listen for checked items changes.
     */
    private OnCheckedChangeListener onCheckedChangeListener;

    /**
     * Listen for click events.
     */
    private OnItemClickListener onItemClickListener;

    /**
     * True if all items are selected.
     */
    private boolean selectedAll;


    @Override
    public int getCount() {
        return bookmarks.size();
    }

    @Override
    public Object getItem(final int position) {
        return bookmarks.get(position);
    }

    @Override
    public long getItemId(final int position) {
        return bookmarks.get(position).getMarketId().hashCode();
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        final BookmarkView view = new BookmarkView(parent.getContext());
        final Bookmark bookmark = bookmarks.get(position);
        view.changeTitle(bookmark.getTitle());
        view.setChecked(this.checked.contains(bookmarks.get(position).getMarketId()));
        view.setOnCheckedListener(checked -> onChecked(bookmark.getMarketId(), checked));
        view.setOnClickListener(itemView -> onItemClick(bookmark));
        return view;
    }

    /**
     * Called when item clicked.
     */
    private void onItemClick(final Bookmark bookmark) {
        if (onItemClickListener != null) {
            onItemClickListener.onClickListener(bookmark);
        }
    }

    /**
     * Called when item checked.
     */
    private void onChecked(final String id, final boolean checked) {
        selectedAll = false;
        if (checked) {
            this.checked.add(id);
        } else {
            this.checked.remove(id);
        }
        if (onCheckedChangeListener != null) {
            onCheckedChangeListener.checkedChanged(this.checked);
        }
    }

    /**
     * Call to update bookmarks.
     */
    void update(final List<Bookmark> bookmarks) {
        this.bookmarks.clear();
        this.checked.clear();
        this.bookmarks.addAll(bookmarks);
        // update list items
        notifyDataSetChanged();
    }

    /**
     * Select all bookmarks.
     */
    void selectAll() {
        checked.clear();
        if (!selectedAll) {
            for (final Bookmark bookmark : bookmarks) {
                checked.add(bookmark.getMarketId());
            }
            if (!checked.isEmpty()) {
                selectedAll = true;
            }
        } else {
            // clear selection
            selectedAll = false;
        }
        // update list items
        notifyDataSetChanged();

        if (onCheckedChangeListener != null) {
            // notify checked items changed
            onCheckedChangeListener.checkedChanged(checked);
        }
    }


    /**
     * Set checked change listener
     */
    void setOnCheckedChangeListener(OnCheckedChangeListener onCheckedChangeListener) {
        this.onCheckedChangeListener = onCheckedChangeListener;
    }

    /**
     * Set item clicks listener
     */
    void setOnItemClickListener(final OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}