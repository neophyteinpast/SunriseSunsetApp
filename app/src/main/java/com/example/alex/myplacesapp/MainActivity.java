package com.example.alex.myplacesapp;

import android.Manifest;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Criteria;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alex.myplacesapp.fragment.DatePickerFragment;
import com.example.alex.myplacesapp.fragment.GpsSettingDialog;
import com.example.alex.myplacesapp.model.Location;
import com.example.alex.myplacesapp.model.PlaceData;
import com.example.alex.myplacesapp.model.SunriseSunset;
import com.example.alex.myplacesapp.model.time_zone.TimeZoneData;
import com.example.alex.myplacesapp.service.DateService;
import com.example.alex.myplacesapp.service.PlaceDataClient;
import com.example.alex.myplacesapp.service.ServiceGenerator;
import com.example.alex.myplacesapp.service.SunriseSunsetClient;
import com.example.alex.myplacesapp.service.TimeZoneClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.jakewharton.threetenabp.AndroidThreeTen;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks, View.OnClickListener, OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener, android.location.LocationListener {

    public static final String TAG = MainActivity.class.getSimpleName();
    private static final int PLACE_PICKER_REQUEST = 1;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int DEFAULT_ZOOM = 5;
    private static final int CURRENT_LOCATION_REQUEST = 0;

    private GoogleApiClient mGoogleApiClient;
    // The entry points to the Places API.
    private PlaceDataClient mPlaceDataClient;
    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;

    private GoogleMap mMap;
    private Place mPlace;
    private android.location.Location mLastKnownLocation;
    //A default location (Dnipro, Ukraine)
    private LatLng mDefaultLocation = new LatLng(48.464717, 35.046183);

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private boolean mLocationPermissionGranted = false;
    private LocationManager mLocationManager;
    private SupportMapFragment mapFragment;

    @BindView(R.id.btn_search)
    Button mSearchBtn;

    @BindView(R.id.tvDate)
    TextView mDateTv;
    @BindView(R.id.tv_currentTimeInfo)
    TextView mCommonInfo;
    @BindView(R.id.tvSunrise)
    TextView mSunriseTv;
    @BindView(R.id.tvSunset)
    TextView mSunsetTv;
    @BindView(R.id.tv_dayLength)
    TextView mDayLength;

    @BindView(R.id.tvPlace)
    TextView mPlaceTv;
    @BindView(R.id.tvTimeZone)
    TextView mTimezone;
    @BindView(R.id.tvLat)
    TextView mLatTv;
    @BindView(R.id.tvLng)
    TextView mLngTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidThreeTen.init(getApplication());
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate():");

        ButterKnife.bind(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApiIfAvailable(LocationServices.API)
                .addApiIfAvailable(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mSearchBtn.setOnClickListener(this);
        mDateTv.setOnClickListener(this);

        mDateTv.setText(DateService.getCurrentDate()); // set current date
        mPlaceDataClient = ServiceGenerator.createService(PlaceDataClient.class);

        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this, null);
        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);
        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1, this);
        Log.d(TAG, "mLocationManager:" + mLocationManager);
    }

    // use Date Picker dialog
    public void showDatePickerDialog() {
        Log.d(TAG, "showDatePickerDialog():");
        DialogFragment dialogFragment = new DatePickerFragment();
        dialogFragment.show(getFragmentManager(), "datePicker");
    }

    public void showEnableGpsDialog() {
        Log.d(TAG, "showEnableGpsDialog():");
        GpsSettingDialog settingDialog = new GpsSettingDialog();
        settingDialog.show(getFragmentManager(), "gps_dialog");
    }

    @Override
    public void onProviderDisabled(String s) {
        Log.d(TAG, "onProviderDisabled(): ");
        boolean enabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!enabled) {
            Log.d(TAG, "!enabled");
            showEnableGpsDialog();
        }
    }

    @Override
    public void onProviderEnabled(String s) {
        Log.d(TAG, "onProviderEnabled(): ");
        Toast.makeText(getBaseContext(), "Gps is turned on!!",
                Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        Log.d(TAG, "onStatusChanged(): ");
    }

    @Override
    public void onLocationChanged(android.location.Location location) {
        long time = System.currentTimeMillis();
        Date date = new Date(time);
        String timestamp = String.format(Locale.getDefault(), "Time: %tH:%<tM:%<tS", date);
        Log.d(TAG, "onLocationChanged(): " + timestamp);
        makeUseOfNewLocation(location);
    }

    private void makeUseOfNewLocation(android.location.Location location) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        updateLocationUI();
        getDeviceLocation(new LatLng(lat, lng), CURRENT_LOCATION_REQUEST);
        Log.d(TAG, "makeUseOfNewLocation(): lat = " + lat + ", lng = " + lng);
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
        mPlaceTv.setText(place.getAddress());
    }

    private void getPlaceData(CharSequence placeId) {
        Log.d(TAG, "getPlaceData():");
        Resources resources = getResources();
        Call<PlaceData> call = mPlaceDataClient
                .getPlace(placeId, resources.getString(R.string.key_browser));
        call.enqueue(new Callback<PlaceData>() {
            @Override
            public void onResponse(Call<PlaceData> call, Response<PlaceData> response) {
                Log.d(TAG, "onResponse():");
                Toast.makeText(MainActivity.this, "Connected successful!", Toast.LENGTH_LONG).show();
                PlaceData placeData = response.body();
                Location location = placeData.getResult().getGeometry().getLocation();

                // connect to Google Timezone API
                getTimeZoneData(location, mDateTv.getText().toString());
            }

            @Override
            public void onFailure(Call<PlaceData> call, Throwable t) {
                Log.d(TAG, "onFailure(): ");
                // the network call was a failure
                Toast.makeText(MainActivity.this, "Connection is failed!", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getTimeZoneData(Location location, String date) {
        Log.d(TAG, "getTimeZoneData():");
        String url = "https://maps.googleapis.com/maps/api/timezone/json?";
        Resources resources = getResources();

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        TimeZoneClient timeZoneClient =
                ServiceGenerator.createService(TimeZoneClient.class);

        Call<TimeZoneData> call =
                timeZoneClient.getTimeZone(url, location, timestamp.getTime() / 1000,
                        resources.getString(R.string.key_browser));
        call.enqueue(new Callback<TimeZoneData>() {
            @Override
            public void onResponse(Call<TimeZoneData> call, Response<TimeZoneData> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "response.isSuccessful(): true");
                    TimeZoneData timeZoneData = response.body();
                    String timeZone = timeZoneData.getTimeZoneId();
                    // search for sunrise sunset information
                    searchForSunriseSunset(location, DateService.getDateFormat(date), 0, timeZone);
                }
            }

            @Override
            public void onFailure(Call<TimeZoneData> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error:(", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void searchForSunriseSunset(Location location, String date, Integer formatted,
                                        @NonNull String timeZone) {
        Log.d(TAG, "searchForSunriseSunset():");
        String url = "https://api.sunrise-sunset.org/json";

        double lat = location.round(location.getLat(), 8);
        double lng = location.round(location.getLng(), 8);
        SunriseSunsetClient sunriseSunsetClient =
                ServiceGenerator.createService(SunriseSunsetClient.class);
        Call<SunriseSunset> call =
                sunriseSunsetClient.getSunriseSunset(url, lat, lng, date, formatted);

        call.enqueue(new Callback<SunriseSunset>() {
            @Override
            public void onResponse(Call<SunriseSunset> call, Response<SunriseSunset> response) {
                SunriseSunset sunriseSunset = response.body();
                String commonInfo
                        = DateService.getFormattedDateTime(mDateTv.getText().toString(), timeZone);
                String sunriseRealTime =
                        DateService.getZoneTime(sunriseSunset.getResults().getSunrise(), timeZone);
                String sunsetRealTime =
                        DateService.getZoneTime(sunriseSunset.getResults().getSunset(), timeZone);
                String dayLength =
                        DateService.getTime(Long.valueOf(sunriseSunset
                                .getResults()
                                .getDayLength()));

                // set retrieved data to UI
                mCommonInfo.setText(commonInfo);
                mSunriseTv.setText(sunriseRealTime);
                mSunsetTv.setText(sunsetRealTime);
                mDayLength.setText(dayLength);

                displayPlace(mPlace);
                mLatTv.setText(String.valueOf(lat));
                mLngTv.setText(String.valueOf(lng));
                mTimezone.setText(timeZone);
                getDeviceLocation(new LatLng(lat, lng), 1);
            }

            @Override
            public void onFailure(Call<SunriseSunset> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error:(", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected():");
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended():");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed():");
        Toast.makeText(this, "The connection is failed!", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart():");
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
            Log.d(TAG, "mGoogleApiClient.connect():");
        }
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause(): ");
        super.onPause();
        mLocationManager.removeUpdates(this);
        Log.d(TAG, "mLocationManager.removeUpdates(this)");
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop():");
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
            Log.d(TAG, "mGoogleApiClient.disconnected");
        }
        super.onStop();
    }

    /**
     * Manipulates the map when it's available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady(): ");
        mMap = googleMap;

        // get location permissions
        getLocationPermission();
        // Turn on the My Location layer and the related control on the map.
        // connect to Google Timezone API
        updateLocationUI();
        // Get the current location of the device and set the position of the map.
        getDeviceLocation(null, CURRENT_LOCATION_REQUEST);
    }

    /**
     * Prompts the user for permission to use the device location.
     */
    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission():");
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult():");
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    private void updateLocationUI() {
        Log.d(TAG, " updateLocationUI():");
        if (mMap == null) {
            Log.d(TAG, "mMap = null");
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                Log.d(TAG, "permission granted, setMyLocationEnabled(true)");
            } else {
                Log.d(TAG, "setMyLocationEnabled(false)");
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getDeviceLocation(LatLng latLng, int flag) {
        Log.d(TAG, "getDeviceLocation()");
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                if (flag == 1) {
                    Log.d(TAG, "location != null");
                    mMap.clear();
                    mMap.addMarker(new MarkerOptions().position(latLng));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));

                    Log.d(TAG, "getDeviceLocation(): Location: lat = "
                            + latLng.latitude
                            + ", lng = " + latLng.longitude);
                } else {
                    Log.d(TAG, "location == null");
                    Task<android.location.Location> locationResult
                            = mFusedLocationProviderClient.getLastLocation();
                    locationResult.addOnCompleteListener(this, new OnCompleteListener<android.location.Location>() {
                        @Override
                        public void onComplete(@NonNull Task<android.location.Location> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                Log.d(TAG, "getDeviceLocation(): task.isSuccessful");
                                // Set the map's camera position to the current location of the device.
                                mLastKnownLocation = task.getResult();

                                mMap.clear();
                                mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(mLastKnownLocation.getLatitude(),
                                                mLastKnownLocation.getLongitude())));
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(mLastKnownLocation.getLatitude(),
                                                mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));

                                LatLng latLng1 = new LatLng(mLastKnownLocation.getLatitude(),
                                        mLastKnownLocation.getLongitude());

                                Location location = new Location(latLng1);
                                getTimeZoneData(location, mDateTv.getText().toString());

                                Log.d(TAG, "getDeviceLocation(): Location: lat = "
                                        + location.getLat() + ", lng = " + latLng1.longitude);
                            } else {
                                Log.d(TAG, "Current location is null. Using defaults.");
                                Log.e(TAG, "Exception: %s", task.getException());
                                mMap.moveCamera(CameraUpdateFactory
                                        .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                            }
                        }
                    });
                }
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "onClick():");
        switch(v.getId()) {
            case R.id.btn_search:
                search();
                break;
            case R.id.tvDate:
                showDatePickerDialog();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Log.d(TAG, "onMyLocationButtonClick(): ");
        return true;
    }
}
