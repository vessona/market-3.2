package com.example.admin.market.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.Toast;
import com.example.admin.market.net.DownloadService;
import com.example.admin.market.entities.Market;
import com.example.admin.market.entities.MyStates;
import com.example.admin.market.R;
import com.example.admin.market.ui.map.MapFragment;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;


public class AddressFilterFragment extends Fragment {
    private AutoCompleteTextView stateTV, cityTV, zipTV;
    private ImageView arrowState, arrowCity, arrowZip;
    private ArrayAdapter<String> stateAdapter, cityAdapter, zipAdapter;
    private OnFragmentInteractionListener mListener;
    public static final int STATE_FLAG = 0;
    public static final int CITY_FLAG = 5;
    public static ArrayList<Market> markets;
    private TheResponse response;
    private final ArrayList<FilterChangeBehavior> filterChangeBehaviors = new ArrayList<>();
    private List<String> cities;
    private List<String> zips;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.address_filter, container, false);
    }

    private final TextWatcherAdapter dropDownListener = new TextWatcherAdapter() { //in future -> for stateTV, cityTV and zipTV
        @Override
        public void afterTextChanged(final Editable s) {
            onDropDownChanged(); //we letting know all filterChangeBehaviours that dropdown changed
        }
    };

    private void onDropDownChanged() { //we letting know all filterChangeBehaviours(MAP & SEARCH buttons, MapFragment) that dropdown changed
        for (FilterChangeBehavior filterChangeBehavior : filterChangeBehaviors) {
            filterChangeBehavior.onFilterChanged(
                    stateTV.getText().toString(),
                    cityTV.getText().toString(),
                    zipTV.getText().toString()
            );
        }
    }

    @Override
    public void onStart() {
        Log.d("onStart" , "Called");
        super.onStart();
        response = new TheResponse(getActivity());
        markets = new ArrayList<>();
        stateTV = getActivity().findViewById(R.id.actvState);
        cityTV = getActivity().findViewById(R.id.actvCity);
        zipTV = getActivity().findViewById(R.id.actvZipcode);
        arrowState = getActivity().findViewById(R.id.arrowState);
        arrowCity = getActivity().findViewById(R.id.arrowCity);
        arrowZip = getActivity().findViewById(R.id.arrowZip);
        stateAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, MyStates.data);
        stateTV.setAdapter(stateAdapter);
        stateTV.setThreshold(1);
        cityTV.setThreshold(1);
        zipTV.setThreshold(1);
        stateTV.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        cityTV.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        zipTV.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        arrowState.setOnClickListener(v -> {stateTV.showDropDown();});
        arrowCity.setOnClickListener(v -> {cityTV.showDropDown();});
        arrowZip.setOnClickListener(v -> {zipTV.showDropDown();});

        Button btnProcess = getActivity().findViewById(R.id.btnProcess);
        btnProcess.setOnClickListener(v -> { if (zipTV.getText().toString() != "" && zipTV.getText().toString().length() == 5) {
            String url = "https://search.ams.usda.gov/farmersmarkets/v1/data.svc/zipSearch?zip=" + zipTV.getText().toString();
            Intent i = new Intent(getActivity(), DownloadService.class);
            i.putExtra("data", url);
            i.putExtra("type", "zip");
            getActivity().startService(i);
        }
        });

        for (final AutoCompleteTextView view : Arrays.asList(stateTV, cityTV, zipTV)) {
            view.addTextChangedListener(dropDownListener); //trigger to onDropDownChanged()
        }

        filterChangeBehaviors.clear();

        filterChangeBehaviors.add((state, city, zip) -> { //new FilterCHangeBehaviour for control of visibility of btnProcess
            boolean zipFound = false;
            final ListAdapter adapter = zipTV.getAdapter();
            if (adapter != null) {
                for (int pos = 0, length = adapter.getCount(); pos < length; ++pos) {
                    if (adapter.getItem(pos).equals(zip)) {
                        zipFound = true;
                        break;
                    }
                }
            }
            if (!TextUtils.isEmpty(state) && !TextUtils.isEmpty(city) && !TextUtils.isEmpty(zip) && zipFound) {
                btnProcess.setVisibility(View.VISIBLE);
            } else {
                btnProcess.setVisibility(View.GONE);

            }
        });

        final View view = getView();
        final FrameLayout mapContainer = view != null ? view.findViewById(R.id.map_container) : null; //if landscape we have map
        if (mapContainer != null) {
            final MapFragment mapFragment = new MapFragment();
            getChildFragmentManager()
                    .beginTransaction()
                    .add(R.id.map_container, mapFragment)
                    .commit();
            filterChangeBehaviors.add(new FilterChangeBehavior.Map(mapFragment)); //to follow the changes in dropdown
        }


        final View showMapButton = view != null ? view.findViewById(R.id.show_map_button) : null;
        if (showMapButton != null) {
            filterChangeBehaviors.add(new FilterChangeBehavior.Visibility(showMapButton)); //to control visibilty of mapbutton
            showMapButton.setOnClickListener(v -> {
                MapsActivity.start(
                        getActivity(),
                        String.format("%s, %s, %s, USA", zipTV.getText(), cityTV.getText(), stateTV.getText())
                );
            });
        }


        stateTV.addTextChangedListener(new TextWatcherAdapter() { //for loading data to next dropdown
            @Override
            public void afterTextChanged(final Editable s) {
                interact(stateTV.getText().toString(), STATE_FLAG);
                cityTV.setText("");
                zipTV.setText("");
            }
        });
        cityTV.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void afterTextChanged(final Editable s) {
                interact(cityTV.getText().toString(), CITY_FLAG);
                zipTV.setText("");
            }
        });
        if (cities != null) { //when switching layouts
            updateCity(cities);
        }
        if (zips != null) { //when switching layouts
            updateZip(zips);
        }
        onDropDownChanged(); //we letting now all filterChangeBehaviours that dropdown changed when changing orientation in this case
    }

    private void interact(String text, int flag) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        mListener.onFragmentInteraction(text, flag, 0);
    }


    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState); //when switching orientation saving
        outState.putStringArray("cities", cities != null ? cities.toArray(new String[0]) : null);
        outState.putStringArray("zips", zips != null ? zips.toArray(new String[0]) : null);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) { //when switchED orientation retirieving
            final String[] zipArr = savedInstanceState.getStringArray("zips");
            final String[] cityArr = savedInstanceState.getStringArray("cities");
            this.zips = zipArr != null ? Arrays.asList(zipArr) : null;
            this.cities = cityArr != null ? Arrays.asList(cityArr) : null;
        }
    }

    @Override
    public void onViewStateRestored(final Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }



    public void updateCity(List<String> cityName) { //used in filling dropdown and when switching orientation
        cities = cityName;
        cityAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, cityName);
        cityTV.setAdapter(cityAdapter);
        cityAdapter.notifyDataSetChanged();
    }

    public void updateZip(List<String> zip) {
        zips = zip;
        zipAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, zip);
        zipTV.setAdapter(zipAdapter);
        zipAdapter.notifyDataSetChanged();
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(String value, int flag, int position);
    }


    public class TheResponse extends BroadcastReceiver {
        Context c;
        public TheResponse(Context c) {
            this.c = c;
        }
        public static final String STATUS_DONE_MARKETS = "MARKETS_ALL_DONE";

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("receive", "inside");
            if (intent.getAction().equals(STATUS_DONE_MARKETS)) {
                String text = "{" + intent.getStringExtra("output_data");
                Log.d("MARKETS", text);

                try {
                    ArrayList<Market> markets = new ArrayList<>();
                    JSONObject jo = new JSONObject(text);
                    JSONArray ja = new JSONArray(jo.getString("results"));
                    for (int i = 0; i < ja.length(); i++) {
                        String sentence = ja.getJSONObject(i).getString("marketname");
                        String[] words = sentence.split(" ");
                        String name = "";
                        for (int j = 1; j < words.length; j++)
                            name += " " + words[j];
                        String id = ja.getJSONObject(i).getString("id");
                        markets.add(new Market(id, name, words[0]));

                    }
                    if (!markets.isEmpty()) {
                        Intent intentMarket = new Intent(getActivity(), MarketsActivity.class);
                        intentMarket.putExtra("QuestionListExtra", markets);
                        startActivity(intentMarket);
                    } else
                        Toast.makeText(getActivity(), "No markets here", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }

        }

    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(response);
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(AddressFilterFragment.TheResponse.STATUS_DONE_MARKETS);
        getActivity().registerReceiver(response, filter);
    }

}