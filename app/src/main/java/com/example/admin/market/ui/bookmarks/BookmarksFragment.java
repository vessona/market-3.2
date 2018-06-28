package com.example.admin.market.ui.bookmarks;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.example.admin.market.net.DownloadService;
import com.example.admin.market.entities.Market;
import com.example.admin.market.R;
import com.example.admin.market.bookmarks.Bookmark;
import com.example.admin.market.bookmarks.Bookmarks;
import com.example.admin.market.bookmarks.BookmarksSqlite;
import com.example.admin.market.net.MarketRequest;
import com.example.admin.market.ui.MarketsActivity;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public final class BookmarksFragment extends Fragment {
    private ListView bookmarksView;
    private TextView placeholderView;
    private BookmarksAdapter adapter;
    private Button selectAllButton;
    private Button deleteButton;
    private Set<String> checkedBookmarks = Collections.emptySet();
    private Bookmarks bookmarks; //bookmarks storage
    private String currentBookmarkId; // Current selected bookmark id.

    @Override
    public View onCreateView(final LayoutInflater inflater,
                              final ViewGroup container,
                             final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_bookmarks, container, false);
        this.bookmarksView = view.findViewById(R.id.bookmarks_list);
        this.adapter = new BookmarksAdapter();
        this.bookmarksView.setAdapter(this.adapter);
        this.placeholderView = view.findViewById(R.id.bookmarks_placeholder);
        this.deleteButton = view.findViewById(R.id.bookmarks_btn_delete);
        this.selectAllButton = view.findViewById(R.id.bookmarks_btn_all);
        deleteButton.setEnabled(false);
        return view;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.bookmarks = BookmarksSqlite.getInstance(getActivity().getApplicationContext());
        if (savedInstanceState != null) {
            currentBookmarkId = savedInstanceState.getString("currentBookmarkId");
        }
    }

    @SuppressWarnings("unchecked")
    @SuppressLint("StaticFieldLeak")
    @Override
    public void onResume() {
        super.onResume();

        startLoadBookmarksFromDatabase();
        selectAllButton.setOnClickListener(view -> adapter.selectAll());

        deleteButton.setOnClickListener(view ->
            new AsyncTask<Set<String>, Void, Set<String>>() {
                @Override
                protected Set<String> doInBackground(final Set<String>[] sets) {
                    bookmarks.delete(sets[0]);
                    return sets[0];
                }

                @Override
                protected void onPostExecute(final Set<String> sets) {
                    startLoadBookmarksFromDatabase();
                    onDelete(sets);
                }
            }.execute(checkedBookmarks));

        // subscribe for checkbox events
        adapter.setOnCheckedChangeListener(
            items -> {
                this.checkedBookmarks = items;
                deleteButton.setEnabled(!items.isEmpty());
            }
        );
        adapter.setOnItemClickListener(this::showDetails);
    }

    /**
     * Called when bookmarks were deleted.
     */
    private void onDelete(final Set<String> ids) {
        if (currentBookmarkId != null && ids.contains(currentBookmarkId)) {
            final View content = getView() != null ? getView().findViewById(R.id.child) : null;
            if (content != null) {
                final Fragment fragment = getChildFragmentManager().findFragmentById(R.id.child);
                if (fragment != null) {
                    getChildFragmentManager()
                        .beginTransaction()
                        .remove(fragment)
                        .commit();
                    content.setVisibility(View.GONE);
                }
            }
            currentBookmarkId = null;
        }
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("currentBookmarkId", currentBookmarkId);
    }

    private void showDetails(final Bookmark bookmark) {
        String url = "https://search.ams.usda.gov/farmersmarkets/v1/data.svc/mktDetail?id=" + bookmark.getMarketId();
        MarketRequest.start(
            new Intent(getActivity(), DownloadService.class)
                .putExtra("data", url)
                .putExtra("type", "market"),
            getActivity(),
            MarketsActivity.TitlesFragment.STATUS_DONE_DETAILS,
            new Market(bookmark.getMarketId(), bookmark.getTitle(), null),
            this::showMarket
        );
    }

    /**
     * Start new task to load bookmarks from database.
     */
    @SuppressLint("StaticFieldLeak")
    private void startLoadBookmarksFromDatabase() {
        if (bookmarks == null) {
            return;
        }
        new AsyncTask<Bookmarks, Void, List<Bookmark>>() {
            @Override
            protected List<Bookmark> doInBackground(final Bookmarks... bookmarks) {
                return bookmarks[0].bookmarks();
            }

            @Override
            protected void onPostExecute(final List<Bookmark> bookmarks) {
                showBookmarks(bookmarks);
            }
        }.execute(bookmarks);
    }

    private void showBookmarks( List<Bookmark> bookmarks) {
        // show placeholder if none of bookmarks
        if (bookmarks.isEmpty()) {
            placeholderView.setVisibility(View.VISIBLE);
            bookmarksView.setVisibility(View.GONE);
        } else {
            placeholderView.setVisibility(View.GONE);
            bookmarksView.setVisibility(View.VISIBLE);
        }
        adapter.update(bookmarks);
    }

    @Override
    public void setUserVisibleHint(final boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        startLoadBookmarksFromDatabase();
    }


    public static Fragment create() {
        return new BookmarksFragment();
    }

    private void showMarket(final Market market) {
        currentBookmarkId = market.getId();
        final View content = getView() != null ? getView().findViewById(R.id.child) : null;
        if (content == null) {
            Intent intent = new Intent();
            intent.setClass(getActivity(), MarketsActivity.DetailsActivity.class);

            intent.putExtra("obj", market);
            intent.putExtra("bookmarksShow", false);

            startActivity(intent);
        } else {
            content.setVisibility(View.VISIBLE);
            getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.child, MarketsActivity.DetailsFragment.newInstance(market))
                .commit();
        }
    }
}
