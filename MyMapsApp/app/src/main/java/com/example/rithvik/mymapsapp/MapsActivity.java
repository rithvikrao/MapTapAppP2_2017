package com.example.rithvik.mymapsapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;
    private boolean canGetLocation = false;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 15;
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 5.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng sandiego = new LatLng(-32.799495, -117.154238);
        mMap.addMarker(new MarkerOptions().position(sandiego).title("Born here"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sandiego));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("MyMapsApp", "Failed permission check 1");
            Log.d("MyMapsApp", Integer.toString(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)));
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("MyMapsApp", "Failed permission check 2");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
        }
        mMap.setMyLocationEnabled(true);


    }

    public void toggleView(View view) {
        if (mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL) {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } else {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
    }

    public void getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            //get GPS status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (isGPSEnabled) Log.d("MyMaps", "getLocation: GPS is enabled");


            //get network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (isNetworkEnabled) Log.d("MyMaps", "getLocation: Network is enabled");

            if (!isGPSEnabled && !isNetworkEnabled) {
                Log.d("MyMaps", "getLocation: No provider is enabled!");
            } else {

                canGetLocation = true;
                if (isGPSEnabled) {
                    Log.d("MyMaps", "getLocation: GPS Enabled - requesting location updates");
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerGps);
                    Log.d("MyMaps", "getLocation: GPS update request success");
                    Toast.makeText(this, "Using GPS", Toast.LENGTH_SHORT);

                }

                if (isNetworkEnabled) {
                    Log.d("MyMaps", "getLocation: Network Enabled - requesting location updates");
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerNetwork);
                    Log.d("MyMaps", "getLocation: Network update request success");
                    Toast.makeText(this, "Using Network", Toast.LENGTH_SHORT);

                }

            }


        } catch(Exception e) {
            Log.d("MyMaps", "getLocation: Caught an exception in getLocation");
            e.printStackTrace();
        }
    }

    LocationListener locationListenerGps = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            //output message in Log.d and Toast

            Log.d("MyMaps", "locationListenerGps: Location changed");
            Toast.makeText(getApplicationContext(), "Location changed!", Toast.LENGTH_SHORT);

            //drop a marker on the map (create a method called dropAMarker)

            dropAMarker(location);

            //disable network updates (see LocationManager to remove updates)

            locationManager.removeUpdates(locationListenerNetwork);


        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            //setup a switch statement on status

            //case: LocationProvider.AVAILABLE --> output a message to Log.d and/or Toast

            //case: LocationProvider.OUT_OF_SERVICE --> request updates from NETWORK_PROVIDER

            //case: LocationProvider.TEMPORARILY_UNAVAILABLE --> request updates from NETWORK_PROVIDER

            //case: default --> request updates from NETWORK_PROVIDER
        }

        @Override
        public void onProviderEnabled(String s) {}

        @Override
        public void onProviderDisabled(String s) {}
    };

    LocationListener locationListenerNetwork = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            //output message in Log.d and Toast

            //drop a marker on the map (create a method called dropAMarker)

            //relaunch request for network location updates (requestLocationUpdates(NETWORK_PROVIDER))
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            //output message in Log.d and/or Toast
        }

        @Override
        public void onProviderEnabled(String s) {}

        @Override
        public void onProviderDisabled(String s) {}
    };


    public void dropAMarker(Location loc) {
        LatLng pos = new LatLng(loc.getLatitude(), loc.getLongitude());

        mMap.addMarker(new MarkerOptions().position(pos));
    }
}