package com.example.admin.market.ui;

import android.text.TextUtils;
import android.view.View;

import com.example.admin.market.ui.map.MapApi;


public interface FilterChangeBehavior {
    void onFilterChanged(final String state,
                         final String city,
                         final String zip);


    final class Map implements FilterChangeBehavior {

        private static final String QUERY_DEFAULT = "USA";

        private final MapApi mapApi;

        Map(final MapApi mapApi) {
            this.mapApi = mapApi;
        }

        @Override
        public void onFilterChanged(final String state,
                                    final String city,
                                    final String zip) {
            final StringBuilder queryBuilder = new StringBuilder();
            if (!TextUtils.isEmpty(zip)) {
                queryBuilder.append(zip).append(", ");
            }
            if (!TextUtils.isEmpty(city)) {
                queryBuilder.append(city).append(", ");
            }
            if (!TextUtils.isEmpty(state)) {
                queryBuilder.append(state).append(", ");
            }
            queryBuilder.append(QUERY_DEFAULT);
            mapApi.search(queryBuilder.toString().trim());
        }
    }

    /////////////////
    final class Visibility implements FilterChangeBehavior {
        private final View view;

        Visibility( final View view ) {
            this.view = view;
        }

        @Override
        public void onFilterChanged(final String state,
                                    final String city,
                                    final String zip) {
            if (!TextUtils.isEmpty(state) && !TextUtils.isEmpty(city) && !TextUtils.isEmpty(zip)) {
                if(view!=null)
                view.setVisibility(View.VISIBLE);
            } else {
                if(view!=null)
                view.setVisibility(View.GONE);
            }
        }
    }
    /////////////////
}
