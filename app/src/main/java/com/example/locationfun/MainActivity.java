package com.example.locationfun;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity {
    private FusedLocationProviderClient mFusedLocationProviderClient;
    static final String TAG = "LocationFunTag";
    static final int LOCATION_REQUEST_CODE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(this);
        // we use mFusedLocationProviderClient for two main purposes
        // 1.  get the user's last known location
        // AKA their current location
        // 2. request location updates
        // we are gonna do the first one

        setupLastKnownLocation();
        setupUserLocationUpdates();
    }

    private void setupLastKnownLocation() {
        // implementing approach (1.)
        // starting with api level 23, at runtime we have to request/make sure
        // we have permission to access the users location

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            // we don't have permission to access the user's location
            // we need to request it
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST_CODE);
            // shows a dialog for the user to select allow or deny for location permission
            // we need to override a callback that executes once the user makes their choice
        } else {
            // we have permission!!! to access the user's location
            Task<Location> locationTask = mFusedLocationProviderClient.getLastLocation();
            // add a complete/successful/failure listener so we know when the task is done
            locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    // at this point the parameter location should store
                    // the users last known location
                    // location could be null if the device does not have a last known location
                    if (location != null) {
                        // we have it finally!
                        Log.d(TAG, "onSuccess: " + location.getLatitude() + ", " + location.getLongitude());
                    }
                }
            });
        }
    }

    private void setupUserLocationUpdates() {
        final LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000); // request an update every 10 seconds
        locationRequest.setFastestInterval(5000); // handle at most updates every 5 seconds
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); // most precise

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                LocationCallback locationCallback = new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        Log.d(TAG, "onLocationResult: ");
                        for (Location location : locationResult.getLocations()) {
                            Log.d(TAG, "onSuccess: " + location.getLatitude() + ", " + location.getLongitude());
                        }
                    }

                };
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            LOCATION_REQUEST_CODE);
                } else {
                    Log.d(TAG, "onSuccess: We have the user's permission");
                    mFusedLocationProviderClient.requestLocationUpdates(locationRequest,
                            locationCallback, null);
                }
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // this method executes when the user responds to the permissions dialog
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // we have the user's permission!!
                setupLastKnownLocation();
            }
        }
    }
}
