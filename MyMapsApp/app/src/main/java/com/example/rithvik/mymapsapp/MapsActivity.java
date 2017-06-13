package com.example.rithvik.mymapsapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;
    private boolean canGetLocation = false;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 15;
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 5.0f;
    private Location myLocation;
    private Marker mCurrLocationMarker;
    private static final float MY_LOC_ZOOM_FACTOR = 17.0f;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private LatLng userLoc;
    private boolean isTracking = false;
    private EditText editSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        editSearch = (EditText) findViewById(R.id.edittext_search);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng sandiego = new LatLng(32.7157, -117.1611);
        mMap.addMarker(new MarkerOptions().position(sandiego).title("Born here"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sandiego));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            Log.d("MyMapsApp", "Failed permission check 1");
            Log.d("MyMapsApp", Integer.toString(ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)));
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            Log.d("MyMapsApp", "Failed permission check 2");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
        }
        //mMap.setMyLocationEnabled(true);


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
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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


        } catch (Exception e) {
            Log.d("MyMaps", "getLocation: Caught an exception in getLocation");
            e.printStackTrace();
        }
    }




    LocationListener locationListenerGps = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            //output message in Log.d and Toast

            Log.d("MyMaps", "locationListenerGps: Location changed");
            Toast.makeText(getApplicationContext(), "GPS: Location changed!", Toast.LENGTH_SHORT);

            //drop a marker on the map (create a method called dropAMarker)

            dropAMarker(LocationManager.GPS_PROVIDER);

            //disable network updates (see LocationManager to remove updates)

            locationManager.removeUpdates(locationListenerNetwork);


        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            //setup a switch statement on status

            switch (status) {
                case LocationProvider.AVAILABLE:
                    Log.d("MyMaps", "locationListenerGps: Location provider available!");
                    Toast.makeText(getApplicationContext(), "GPS: Location provider available!", Toast.LENGTH_SHORT);

                case LocationProvider.OUT_OF_SERVICE:
                    Log.d("MyMaps", "locationListenerGps: Location provider out of service!");
                    Toast.makeText(getApplicationContext(), "GPS: Location provider out of service!", Toast.LENGTH_SHORT);

                    if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this,
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerNetwork);

                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.d("MyMaps", "locationListenerGps: Location provider temporarily unavailable!");
                    Toast.makeText(getApplicationContext(), "GPS: Location provider temporarily unavailable!",
                            Toast.LENGTH_SHORT);

                    if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this,
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerNetwork);

                default:
                    Log.d("MyMaps", "locationListenerGps: Default case -- requesting network provider updates!");
                    Toast.makeText(getApplicationContext(), "GPS: Default. Requesting network updates.", Toast.LENGTH_SHORT);

                    if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this,
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerNetwork);

            }

            //case: LocationProvider.AVAILABLE --> output a message to Log.d and/or Toast

            //case: LocationProvider.OUT_OF_SERVICE --> request updates from NETWORK_PROVIDER

            //case: LocationProvider.TEMPORARILY_UNAVAILABLE --> request updates from NETWORK_PROVIDER

            //case: default --> request updates from NETWORK_PROVIDER
        }

        @Override
        public void onProviderEnabled(String s) {
        }

        @Override
        public void onProviderDisabled(String s) {
        }
    };

    LocationListener locationListenerNetwork = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            //output message in Log.d and Toast

            Log.d("MyMaps", "locationListenerNetwork: Location changed");
            Toast.makeText(getApplicationContext(), "Network: Location changed!", Toast.LENGTH_SHORT);

            //drop a marker on the map (create a method called dropAMarker)

            dropAMarker(LocationManager.NETWORK_PROVIDER);

            //relaunch request for network location updates (requestLocationUpdates(NETWORK_PROVIDER))

            if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES,
                    locationListenerNetwork);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            //output message in Log.d and/or Toast

            Log.d("MyMaps", "locationListenerNetwork: Status changed");
            Toast.makeText(getApplicationContext(), "Network: Status changed!", Toast.LENGTH_SHORT);
        }

        @Override
        public void onProviderEnabled(String s) {
        }

        @Override
        public void onProviderDisabled(String s) {
        }
    };


    public void dropAMarker(String provider) {

        LatLng userLocation = null;

        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            myLocation = locationManager.getLastKnownLocation(provider);
        }

        if (myLocation == null) {
            //display a message in Log.d and/or Toast
            Log.d("MyMaps", "dropAMarker: Location null -- update failed.");
            Toast.makeText(getApplicationContext(), "Location update failed -- location null.", Toast.LENGTH_SHORT);
        } else {
            userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());

            //display message in Log.d and/or Toast

            Log.d("MyMaps", "dropAMarker: Location updated!");
            Toast.makeText(getApplicationContext(), "Location updated!", Toast.LENGTH_SHORT);

            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(userLocation, MY_LOC_ZOOM_FACTOR);

            int color = Color.RED;

            if (provider.equals(LocationManager.GPS_PROVIDER)) {
                color = Color.CYAN;
            }


            //Add a shape for your marker
            Circle circle = mMap.addCircle(new CircleOptions()
                    .center(userLocation)
                    .radius(1)
                    .strokeColor(color)
                    .strokeWidth(2)
                    .fillColor(color));

            //mMap.animateCamera(update);


        }


    }

    public void searchPOI(View view) throws IOException {
        Geocoder gc = new Geocoder(this.getApplicationContext());
        if (myLocation != null && editSearch.getText() != null) {
            List<Address> addlist = gc.getFromLocationName(editSearch.getText().toString(), 3, myLocation.getLatitude() - .07246, myLocation.getLongitude() - .07246, myLocation.getLatitude() + .07246, myLocation.getLongitude() + .07246);
            for (int i = 0; i < addlist.size(); i++) {
                LatLng poi = new LatLng(addlist.get(i).getLatitude(), addlist.get(i).getLongitude());
                mMap.addMarker(new MarkerOptions().position(poi).title(addlist.get(i).getAddressLine(0)));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(poi, MY_LOC_ZOOM_FACTOR));

            }

            Toast.makeText(this.getApplicationContext(), "Markers successfully added!", Toast.LENGTH_SHORT).show();
        }
    }

    public void clearMarkers(View view) {
        mMap.clear();

        LatLng sandiego = new LatLng(32.7157, -117.1611);
        mMap.addMarker(new MarkerOptions().position(sandiego).title("Born here"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sandiego));
    }

    public void trackMe(View view) {
        if (isTracking) {
            isTracking = false;
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d("MyMapsApp","trackMe: Permission check failed");
                return;
            }
            locationManager.removeUpdates(locationListenerGps);
            locationManager.removeUpdates(locationListenerNetwork);


        }
        else {
            Log.d("MyMapsApp","trackMe: Calling getLocation Method");
            getLocation();
            Toast.makeText(this, "Tracking now!", Toast.LENGTH_SHORT);
            isTracking = true;
        }
    }

}