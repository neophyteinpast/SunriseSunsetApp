package com.example.alex.myplacesapp.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.alex.myplacesapp.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by Alex on 11.02.2018.
 */

public class MapFragmentCurrentPlace extends SupportMapFragment {
    private SupportMapFragment mMapFragment;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_map, null);

//        if (mMapFragment == null) {
//            mMapFragment = SupportMapFragment.newInstance();
//            mMapFragment.getMapAsync(new OnMapReadyCallback() {
//                @Override
//                public void onMapReady(GoogleMap googleMap) {
//                    LatLng latLng = new LatLng(1.289545, 103.849972);
//                    googleMap.addMarker(new MarkerOptions().position(latLng)
//                    .title("Singapore"));
//                    googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
//                }
//            });
//        }
//
//        // R.id.map is a FrameLayout, not a Fragment
//        getChildFragmentManager().beginTransaction().replace(R.id.map_fragment_container, mMapFragment).commit();

        return rootView;
    }
}
