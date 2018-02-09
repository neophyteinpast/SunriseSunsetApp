package com.example.alex.myplacesapp;

import android.Manifest;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alex.myplacesapp.fragment.DatePickerFragment;
import com.example.alex.myplacesapp.model.Location;
import com.example.alex.myplacesapp.model.PlaceData;
import com.example.alex.myplacesapp.model.SunriseSunset;
import com.example.alex.myplacesapp.model.TimeZoneOffset;
import com.example.alex.myplacesapp.service.DateService;
import com.example.alex.myplacesapp.service.PlaceDataClient;
import com.example.alex.myplacesapp.service.ServiceGenerator;
import com.example.alex.myplacesapp.service.SunriseSunsetClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.jakewharton.threetenabp.AndroidThreeTen;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks, View.OnClickListener {

    public static final String TAG = MainActivity.class.getSimpleName();
    public static final int PLACE_PICKER_REQUEST = 1;

    private GoogleApiClient mGoogleApiClient;
    private PlaceDataClient mPlaceDataClient;
    private Place mPlace;

    @BindView(R.id.btn_getPlace)
    Button mGetPlaceBtn;

    @BindView(R.id.tvDate)
    TextView mDateTv;
    @BindView(R.id.tvPlace)
    TextView mPlaceTv;
    @BindView(R.id.tvTimeZone)
    TextView mTimezone;
    @BindView(R.id.tvLat)
    TextView mLatTv;
    @BindView(R.id.tvLng)
    TextView mLngTv;
    @BindView(R.id.tvSunrise)
    TextView mSunriseTv;
    @BindView(R.id.tvSunset)
    TextView mSunsetTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidThreeTen.init(getApplication());
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate():");

        ButterKnife.bind(this);

         mGoogleApiClient = new GoogleApiClient.Builder(this)
                 .addApiIfAvailable(Places.GEO_DATA_API)
                 .addApiIfAvailable(Places.PLACE_DETECTION_API)
                 .enableAutoManage(this, this)
                 .addConnectionCallbacks(this)
                 .addOnConnectionFailedListener(this)
                 .build();
        mGetPlaceBtn.setOnClickListener(this);
        mDateTv.setOnClickListener(this);

        mDateTv.setText(DateService.getCurrentDate()); // set current date
        mPlaceDataClient = ServiceGenerator.createService(PlaceDataClient.class);
    }

    // use Date Picker dialog
    public void showDatePickerDialog() {
        DialogFragment dialogFragment = new DatePickerFragment();
        dialogFragment.show(getFragmentManager(), "datePicker");
    }

    // use Place Picker widget
    public void search() {
        Log.d(TAG, "search():");
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
            return;
        }
        // Construct an intent for the place picker
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            // Start the intent by requesting a result, identified by a request code.
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            Log.d(TAG, "GooglePlayServicesRepairableException thrown");
        } catch (GooglePlayServicesNotAvailableException e) {
            Log.d(TAG, "GooglePlayServicesNotAvailableException thrown");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult():");
        if (requestCode == PLACE_PICKER_REQUEST && resultCode == RESULT_OK) {
            // The user has selected a place.
            mPlace = PlacePicker.getPlace(this, data);
            getPlaceData(mPlace.getId());
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void displayPlace(Place place) {
        Log.d(TAG, "displayPlace():");
        if (place == null) return;
        mPlaceTv.setText( place.getAddress());
    }

    private void getPlaceData(CharSequence placeId) {
        Resources resources = getResources();
        Call<PlaceData> call = mPlaceDataClient
                .getPlace(placeId, resources.getString(R.string.key_browser));
        call.enqueue(new Callback<PlaceData>() {
            @Override
            public void onResponse(Call<PlaceData> call, Response<PlaceData> response) {
                Toast.makeText(MainActivity.this, "Connected successful!", Toast.LENGTH_LONG).show();
                PlaceData placeData = response.body();
                Location location = placeData.getResult().getGeometry().getLocation();
                String timeZone = TimeZoneOffset.findTimeZone(mPlace.getName().toString());

                // search for sunrise sunset information
                searchForSunriseSunset(location.round(location.getLat(), 8),
                        location.round(location.getLng(), 8),
                        DateService.getDateFormat(mDateTv.getText().toString()), 0, timeZone);
            }

            @Override
            public void onFailure(Call<PlaceData> call, Throwable t) {
                // the network call was a failure
                Toast.makeText(MainActivity.this, "Error:(", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void searchForSunriseSunset(double lat, double lng, String date, Integer formatted,
                                        @NonNull String timeZone) {
        String url = "https://api.sunrise-sunset.org/json";
        Resources resources = getResources();

        SunriseSunsetClient sunriseSunsetClient =
                ServiceGenerator.createService(SunriseSunsetClient.class);
        Call<SunriseSunset> call =
                sunriseSunsetClient.getSunriseSunset(url, lat, lng, date, formatted);

        call.enqueue(new Callback<SunriseSunset>() {
            @Override
            public void onResponse(Call<SunriseSunset> call, Response<SunriseSunset> response) {
                SunriseSunset sunriseSunset = response.body();
                String sunriseRealTime =
                        DateService.getZoneTime(sunriseSunset.getResults().getSunrise(), timeZone);
                String sunsetRealTime =
                        DateService.getZoneTime(sunriseSunset.getResults().getSunrise(), timeZone);

                // set retrieved data to UI
                displayPlace(mPlace);
                mLatTv.setText(String.valueOf(lat));
                mLngTv.setText(String.valueOf(lng));
                mTimezone.setText(resources.getString(R.string.text_timezone, timeZone));
                mSunriseTv.setText(sunriseRealTime);
                mSunsetTv.setText(sunsetRealTime);
            }

            @Override
            public void onFailure(Call<SunriseSunset> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error:(", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(MainActivity.this, "Please allow ACCESS_COARSE_LOCATION persmission.",
                    Toast.LENGTH_LONG).show();
        }
        Log.d(TAG, "onConnected():");
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Error:(", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
        Log.d(TAG, "onStart():");
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        Log.d(TAG, "onStop():");
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "onClick():");
        switch(v.getId()) {
            case R.id.btn_getPlace:
                search();
                break;
            case R.id.tvDate:
                showDatePickerDialog();
                break;
            default:
                break;
        }
    }
}
