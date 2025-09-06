package com.example.pizza_mania;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationRequest;
import android.os.Bundle;
import android.transition.Visibility;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.pizza_mania.databinding.ActivitySelectLocationBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class SelectLocation extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivitySelectLocationBinding binding;
    private FusedLocationProviderClient flpClient;
    final private int locationPermReqCode = 1;
    private ImageButton myLocationBtn;
    private ProgressBar myLocationLoadingAnim;
    private LottieAnimationView marker_anim;
    private LatLng currentLocation = new LatLng(6.9271, 79.8612);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySelectLocationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        myLocationBtn = findViewById(R.id.myLocationBtn);
        myLocationLoadingAnim = findViewById(R.id.myLocationLoadingAnim);
        marker_anim  = findViewById(R.id.map_marker);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //for fetching current location
        flpClient = LocationServices.getFusedLocationProviderClient(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //limiting the map to SriLanka
        LatLngBounds bounds = new LatLngBounds(new LatLng(5.11, 79.51), new LatLng(10.49, 82.02));
        mMap.setLatLngBoundsForCameraTarget(bounds);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bounds.getCenter(), 7.75f));
        mMap.setMinZoomPreference(7.75f);

        //MyLocationButton behaviour
        //adding this to onMapReady just in case
        myLocationBtn.setOnClickListener((v)->{
            myLocationLoadingAnim.setVisibility(View.VISIBLE); //enabling the loading animation
            SelectCurrentLocation(mMap);
            myLocationBtn.setEnabled(false); myLocationBtn.setAlpha(0.5f); //disabling button to stop spam while loading
        });

        //updating the custom animated marker when the camera moves
        mMap.setOnCameraMoveListener(()->{
            drawMarker();
        });
    }

    public void SelectCurrentLocation(GoogleMap mMap){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},locationPermReqCode );
        }
        CurrentLocationRequest request = new CurrentLocationRequest.Builder().setPriority(Priority.PRIORITY_HIGH_ACCURACY).build();
        flpClient.getCurrentLocation(request,null)
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null){
                            myLocationLoadingAnim.setVisibility(View.GONE); //disabling the loading animation
                            myLocationBtn.setEnabled(true); myLocationBtn.setAlpha(1.0f);//reenabling the button
                            currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f));
                            drawMarker(); //animated marker
                            marker_anim.setVisibility(View.VISIBLE);
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        myLocationLoadingAnim.setVisibility(View.GONE);
                        myLocationBtn.setEnabled(true); myLocationBtn.setAlpha(1.0f);
                        Toast.makeText(SelectLocation.this, "Failed to fetch location", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //function to move the animated marker to the target location
    public void drawMarker(){
        Point markerPos = mMap.getProjection().toScreenLocation(currentLocation);
        marker_anim.setX(markerPos.x - marker_anim.getWidth()/2f);
        marker_anim.setY(markerPos.y - marker_anim.getHeight()/2f);
    }

}