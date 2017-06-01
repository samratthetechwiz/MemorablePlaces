package com.example.samrat.memorableplaces;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;

    public void centerMapOnLocation(Location lastKnownLocation, String title){
        LatLng userLocation = new LatLng(lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude());
        mMap.clear();

        mMap.addMarker(new MarkerOptions().position(userLocation).title(title));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,4));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 1){
            if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                }
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapLongClickListener(this);

        Intent intent = getIntent();

        int i = intent.getIntExtra("place",0);
        Log.i("Place",String.valueOf(i));
        Log.i("place",String.valueOf(MainActivity.locations.get(intent.getIntExtra("place",0)).latitude));
        if(intent.getIntExtra("place",0) == 0){
            locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    centerMapOnLocation(location,"Your Location");
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };
        }else{
            Location placeLocation = new Location(LocationManager.GPS_PROVIDER);
            placeLocation.setLatitude(MainActivity.locations.get(intent.getIntExtra("place",0)).latitude);
            placeLocation.setLongitude(MainActivity.locations.get(intent.getIntExtra("place",0)).longitude);

            centerMapOnLocation(placeLocation,MainActivity.places.get(intent.getIntExtra("place",0)));
        }

        if(Build.VERSION.SDK_INT <23){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
        }else{
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
            }else{
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                centerMapOnLocation(lastKnownLocation,"Your Location");
            }
        }
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        String address = "";

        try {
            List<Address> listAddresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

            if (listAddresses != null && listAddresses.size() > 0) {
                if (listAddresses.get(0).getSubThoroughfare() != null) {
                    address += listAddresses.get(0).getSubThoroughfare() + " ";
                }
                if (listAddresses.get(0).getThoroughfare() != null) {
                    address += listAddresses.get(0).getThoroughfare() + ", ";
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(address == ""){
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm yyyy-MM-dd");
            address = sdf.format(new Date());
        }

        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng).title(address));

        MainActivity.locations.add(latLng);
        MainActivity.places.add(address);

        Toast.makeText(this,latLng.toString(),Toast.LENGTH_LONG).show();

        MainActivity.arrayAdapter.notifyDataSetChanged();
    }
}
