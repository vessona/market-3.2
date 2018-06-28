package com.example.admin.market.ui;

import android.app.ActionBar;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.example.admin.market.net.DownloadService;
import com.example.admin.market.entities.Market;
import com.example.admin.market.R;
import com.example.admin.market.bookmarks.BookmarksSqlite;
import com.example.admin.market.net.MarketRequest;
import com.example.admin.market.ui.bookmarks.BookmarksFragment;
import com.example.admin.market.ui.tabs_pager.TabsView;
import com.google.android.gms.maps.model.LatLng;
import java.io.Serializable;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarketsActivity extends AppCompatActivity {

    static ArrayList<Market> markets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide(); //making space to place tabs
        }

        markets = (ArrayList<Market>) getIntent().getSerializableExtra("QuestionListExtra");
        final TabsView tabsView = new TabsView(this);
        tabsView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(tabsView);
        tabsView.show(getSupportFragmentManager(),
            Arrays.asList(
                Pair.create(new MarketsFragment(), "Markets"), //just another way to create List<Pair<Fragment, String>>
                Pair.create(BookmarksFragment.create(), "Bookmarks")
            )
        );
    }

    public static final class MarketsFragment extends Fragment {

        @Override
        public View onCreateView(final LayoutInflater inflater,  final ViewGroup container,  final Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_markets, container, false);
        }
    }

    public static class DetailsActivity extends AppCompatActivity {

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                finish();
                return;
            }

            if (savedInstanceState == null) {
                DetailsFragment details = new DetailsFragment();
                details.setArguments(getIntent().getExtras());
                getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, details).commit();
            }
        }

        @Override
        public void onBackPressed() {
            finish();
        }
    }



    public static class TitlesFragment extends ListFragment {
        boolean mDualPane;
        int mCurCheckPosition = 0;
        Market selMarket;
        public static String STATUS_DONE_DETAILS = "DETAILS_ALL_DONE";

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            List<HashMap<String, Object>> fillMaps = new ArrayList<>();
            String[] from = new String[]{"title", "dist", "id"};
            int[] to = new int[]{R.id.textView, R.id.textView2, R.id.textView3};

            for (int i = 0; i < markets.size(); i++) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("title", markets.get(i).getName().trim());
                map.put("id", markets.get(i).getId());
                map.put("dist", markets.get(i).getDistance());
                fillMaps.add(map);
            }

            SimpleAdapter adapter = new SimpleAdapter(getActivity(), fillMaps, R.layout.row_market, from, to);
            setListAdapter(adapter);
            getListView().setSelector(R.drawable.fragment_listselector);

            View detailsFrame = getActivity().findViewById(R.id.details);

            mDualPane = detailsFrame != null
                && detailsFrame.getVisibility() == View.VISIBLE;

            if (savedInstanceState != null) {
                // Restore last state for checked position.
                mCurCheckPosition = savedInstanceState.getInt("curChoice", 0);
            }

            if (mDualPane) {
                if (selMarket != null) {
                    Market last = markets.get(mCurCheckPosition);
                    showDetails(last);
                }
                onItemClick(mCurCheckPosition);
            }
            else
                getListView().setItemChecked(mCurCheckPosition, true);

        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putInt("curChoice", mCurCheckPosition);
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            onItemClick(position);
        }

        private void onItemClick(int position) {
            String mID = markets.get(position).getId();
            String url = "https://search.ams.usda.gov/farmersmarkets/v1/data.svc/mktDetail?id=" + mID;
            selMarket = new Market();
            selMarket.setId(mID);
            selMarket.setDistance(markets.get(position).getDistance());
            selMarket.setName(markets.get(position).getName());

            MarketRequest.start( //loading rest of the details about market and setting it to selMarket (shorter version)
                new Intent(getActivity(), DownloadService.class) //intent
                    .putExtra("data", url)
                    .putExtra("type", "market"),
                getActivity(), //context
                STATUS_DONE_DETAILS, //action
                selMarket, //market
                response -> showDetails(selMarket = response) //callback
            );
            mCurCheckPosition = position;
        }


        public void showDetails(Market market) {
            if (mDualPane) {
                DetailsFragment details = DetailsFragment.newInstance(market, true);
                FragmentTransaction ft = getActivity().getSupportFragmentManager()
                    .beginTransaction();
                ft.replace(R.id.details, details);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.commit();
            } else {
                Intent intent = new Intent();
                intent.setClass(getActivity(), DetailsActivity.class);
                intent.putExtra("obj", market);
                intent.putExtra("bookmarksShow", true);
                startActivity(intent);
            }
        }

    }

    public static class DetailsFragment extends Fragment {

        public static DetailsFragment newInstance(Market market) {
            return newInstance(market, false);
        }

        public static DetailsFragment newInstance(Market market, boolean showBookmark) {
            DetailsFragment f = new DetailsFragment();
            Bundle args = new Bundle();
            args.putSerializable("obj", market);
            args.putBoolean("bookmarksShow", showBookmark);
            f.setArguments(args);

            return f;
        }

        public Serializable getShownIndex() {
            return getArguments().getSerializable("obj");
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            Market market = (Market) getShownIndex();
            View rootView = inflater.inflate(R.layout.details, container, false);
            TextView tvName = rootView.findViewById(R.id.mName);
            TextView tvProduce = rootView.findViewById(R.id.mProduce);
            TextView tvAddr = rootView.findViewById(R.id.mAddress);
            TextView tvLink = rootView.findViewById(R.id.mLink);
            TextView tvSched = rootView.findViewById(R.id.mSchedule);
            TextView tvDist = rootView.findViewById(R.id.mDist);

            tvAddr.setText(market.getAddress());
            tvDist.setText(market.getDistance());
            tvLink.setText(market.getGoogle());

            ///////////////
            tvName.setText(market.getName().trim());
            tvProduce.setText(market.getProduce().replace("; ","\n"));
            tvSched.setText(market.getSchedule().replaceFirst("(.{25})", "$1\n").replace(";","\n"));
            ///////////////

            final Button btnBookmark = rootView.findViewById(R.id.details_bookmark);
            final Button btnShowMap = rootView.findViewById(R.id.details_show_map);

            boolean bookmarksShow = getArguments().getBoolean("bookmarksShow", false);
            if (bookmarksShow) {
                btnBookmark.setVisibility(View.VISIBLE);
            } else {
                btnBookmark.setVisibility(View.GONE);
            }
            // add market to bookmarks
            ///////////////

            btnBookmark.setOnClickListener(v ->
                    new AsyncTask<Market, Void, Void>() {
                        @Override
                        protected Void doInBackground(final Market... markets) {
                            BookmarksSqlite.getInstance(getActivity().getApplicationContext())
                                    .bookmark(markets[0].getId(), markets[0].getName());
                            getActivity().runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(getActivity(), "Added to bookmarks", Toast.LENGTH_SHORT).show();
                                }
                            });
                            return null;
                        }
                    }.execute(market)
            );
            ///////////////

            final String link = market.getGoogle();
            if (link == null) {
                btnShowMap.setOnClickListener(v -> MapsActivity.start(getContext(), market.getAddress()));
            } else {
                final String query = Uri.parse(link).getQuery();
                final Matcher matcher = Pattern.compile("q=([\\d.\\-]+),\\s([\\d\\.\\-]+).*").matcher(query);
                if (matcher.matches()) {
                    final NumberFormat nf = NumberFormat.getInstance(Locale.US);
                    double lat;
                    double lng;
                    try {
                        String gr1 = matcher.group(1);
                        final boolean gr1Neg = gr1.startsWith("-");
                        gr1 = gr1Neg ? gr1.substring(1) : gr1;

                        String gr2 = matcher.group(2);
                        final boolean gr2Neg = gr2.startsWith("-");
                        gr2 = gr2Neg ? gr2.substring(1) : gr2;

                        lat = nf.parse(gr1).doubleValue();
                        lng = nf.parse(gr2).doubleValue();
                        if (gr1Neg) {
                            lat = lat * -1.0;
                        }
                        if (gr2Neg) {
                            lng = lng * -1.0;
                        }
                        final double latitude = lat;
                        final double longitude = lng;
                        btnShowMap.setOnClickListener(v -> MapsActivity.start(getContext(), new LatLng(latitude, longitude)));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }

            return rootView;

        }
    }

}

