package com.example.pizza_mania;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationRequest;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Visibility;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.example.pizza_mania.model.AdapterItem;
import com.example.pizza_mania.model.AutoCompleteResult;
import com.example.pizza_mania.model.GeoLocationResponse;
import com.example.pizza_mania.service.AutoCompleteInterface;
import com.example.pizza_mania.service.GeoLocationInterface;
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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.geojson.GeoJsonReader;

public class SelectLocation extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivitySelectLocationBinding binding;
    private FusedLocationProviderClient flpClient;
    final private int locationPermReqCode = 1;
    private ImageButton myLocationBtn;
    private ImageButton manualLocationBtn;
    private MaterialButton finalizeLocationBtn;
    private ProgressBar myLocationLoadingAnim;
    private LottieAnimationView marker_anim;
    private LottieAnimationView location_selector_anim;
    private LatLng currentLocation = new LatLng(6.9271, 79.8612); //colombo latlng to avoid null error at startup
    private LatLng deliveryLocation;
    final private String geoLocUrl = "https://nominatim.openstreetmap.org/";
    private String SLJson;
    private Call<AutoCompleteResult> autoCompleteCall;
    private ArrayAdapter<AdapterItem> autoCompAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySelectLocationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //global vars
        myLocationBtn = findViewById(R.id.myLocationBtn);
        manualLocationBtn = findViewById(R.id.manualLocationBtn);
        finalizeLocationBtn = findViewById(R.id.finalizeLocationBtn);
        myLocationLoadingAnim = findViewById(R.id.myLocationLoadingAnim);
        marker_anim  = findViewById(R.id.map_marker);
        location_selector_anim = findViewById(R.id.location_selector);
        SLJson = loadJsonMap(SelectLocation.this, "srilanka.json");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //for fetching current location
        flpClient = LocationServices.getFusedLocationProviderClient(this);

        //setting up the adapter for auto complete
        autoCompAdapter = new ArrayAdapter<>(this, R.layout.autocomplete_layout, R.id.textViewItem);
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
            location_selector_anim.setVisibility(View.GONE); //disabling cursor for manual location selector
            myLocationLoadingAnim.setVisibility(View.VISIBLE); //enabling the loading animation
            SelectCurrentLocation(mMap);
            myLocationBtn.setEnabled(false); myLocationBtn.setAlpha(0.5f); //disabling button to stop spam while loading
            finalizeLocationBtn.setEnabled(false); finalizeLocationBtn.setAlpha(0.5f); //disabling this till the location is loaded
            manualLocationBtn.setEnabled(false); manualLocationBtn.setAlpha(0.5f); //same here
        });

        //manual location selection button
        manualLocationBtn.setOnClickListener((v)->{
            marker_anim.setVisibility(View.GONE); //disabling the marker from MyLocation
            location_selector_anim.setVisibility(View.VISIBLE); //enabling the cursor
            enableFinalizeBtn();
        });

        //finalize location btn behaviour
        finalizeLocationBtn.setOnClickListener((v)->{
            if (deliveryLocation!=null){
                Toast.makeText(this, String.format("Lat: %f Lng: %f", deliveryLocation.latitude, deliveryLocation.longitude), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please select a location to deliver", Toast.LENGTH_SHORT).show();
            }
        });

        //updating the custom animated marker when the camera moves
        mMap.setOnCameraMoveListener(()->{
            drawMarker(currentLocation);
        });

        mMap.setOnCameraIdleListener(()->{
            if (location_selector_anim.getVisibility()==View.VISIBLE){
                deliveryLocation=mMap.getCameraPosition().target; //updating the delivery loc only if the manual location selection is on
            }
        });

        //geolocation client - need this for OSM geocoding. it wont work otherwise
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> chain.proceed(
                        chain.request().newBuilder()
                                .header("User-Agent", "Pizza-Mania/1.0 (pizzamania@gmail.com)")
                                .build()
                )).readTimeout(60, TimeUnit.SECONDS)
                .build();
        Retrofit retroClient = new Retrofit.Builder().baseUrl(geoLocUrl).client(client).addConverterFactory(GsonConverterFactory.create()).build();
        GeoLocationInterface geoLocationService = retroClient.create(GeoLocationInterface.class);

        //geolocation btn setup
        ImageButton geoLocationBtn = findViewById(R.id.geolocationBtn);
        AutoCompleteTextView geoLocationText = findViewById(R.id.geolocationText);
        geoLocationBtn.setOnClickListener((v)->{
            geoLocationText.clearFocus();
            if (!geoLocationText.getText().isEmpty()){
                String text = geoLocationText.getText().toString();
                Call<List<GeoLocationResponse>> coords = geoLocationService.getCoords(text, "json", 1);

                coords.enqueue(new Callback<List<GeoLocationResponse>>() {
                    @Override
                    public void onResponse(Call<List<GeoLocationResponse>> call, Response<List<GeoLocationResponse>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()){
                            GeoLocationResponse parsedResp = response.body().get(0);
                            LatLng parsedLocation = new LatLng(parsedResp.lat, parsedResp.lon);

                            //this part checks if the entered location is in sri lanka or not
                            if(checkIfCoordsInMap(parsedLocation, SLJson)){
                                currentLocation=parsedLocation;
                                deliveryLocation=parsedLocation;
                                drawMarker(currentLocation);
                                marker_anim.setVisibility(View.VISIBLE);
                                location_selector_anim.setVisibility(View.GONE);
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(parsedResp.lat, parsedResp.lon), 18f));
                            } else {
                                String text = getString(R.string.location_not_in_country);
                                Toast.makeText(SelectLocation.this, text, Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            Toast.makeText(SelectLocation.this, "Failed to fetch location", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<GeoLocationResponse>> call, Throwable t) {
                        Toast.makeText(SelectLocation.this, "Location Service Error", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        //adding autocomplete to text field
        Retrofit retrofitClient = new Retrofit.Builder().baseUrl("https://photon.komoot.io/").client(client).addConverterFactory(GsonConverterFactory.create()).build();
        AutoCompleteInterface autoCompleteService = retrofitClient.create(AutoCompleteInterface.class);
        geoLocationText.setThreshold(2);
        geoLocationText.setAdapter(autoCompAdapter);
        geoLocationText.setDropDownBackgroundResource(R.drawable.autocomplete_background);
        geoLocationText.setDropDownHorizontalOffset(50);
        geoLocationText.setDropDownVerticalOffset(1);
        geoLocationText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                AdapterItem item = (AdapterItem) adapterView.getItemAtPosition(i);
                if (checkIfCoordsInMap(new LatLng(item.getLat(), item.getLon()), SLJson)){
                    currentLocation = new LatLng(item.getLat(), item.getLon());
                    deliveryLocation=currentLocation;
                    drawMarker(currentLocation);
                    marker_anim.setVisibility(View.VISIBLE);
                    location_selector_anim.setVisibility(View.GONE);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 18f));
                    enableFinalizeBtn();
                } else {
                    String text = getString(R.string.location_not_in_country);
                    Toast.makeText(SelectLocation.this, text, Toast.LENGTH_SHORT).show();
                }

            }
        });
        geoLocationText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                if (!geoLocationText.getText().isEmpty()){
                    getAutocomplete(autoCompleteService, geoLocationText.getText().toString(), 5, currentLocation.latitude, currentLocation.longitude, new HandleAutoComplete() {
                        @Override
                        public void onSuccess(AutoCompleteResult result) {
//                            Toast.makeText(SelectLocation.this, String.valueOf(result.features.size()), Toast.LENGTH_SHORT).show();
                            autoCompAdapter.clear();
                            for (AutoCompleteResult.Feature feature : result.features){
                                autoCompAdapter.add(new AdapterItem(feature.properties.name, feature.geometry.coordinates.getLast(), feature.geometry.coordinates.getFirst()));
                            }
                            autoCompAdapter.getFilter().filter(geoLocationText.getText(), geoLocationText);
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            t.printStackTrace();
                            Toast.makeText(SelectLocation.this, "Error encountered", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
        });
    }

    public void getAutocomplete(AutoCompleteInterface service, String text, int limit, Double lat, Double lon, HandleAutoComplete callback){
        if (autoCompleteCall != null && !autoCompleteCall.isCanceled()){ //making sure the queue doesnt get long
            autoCompleteCall.cancel();
        }

        autoCompleteCall = service.getAutocompletions(text, limit, lat, lon);
        autoCompleteCall.enqueue(new Callback<AutoCompleteResult>() {
            @Override
            public void onResponse(Call<AutoCompleteResult> call, Response<AutoCompleteResult> response) {
                if (response.isSuccessful() && response.body()!=null){
                    callback.onSuccess(response.body());
                }
            }

            @Override
            public void onFailure(Call<AutoCompleteResult> call, Throwable t) {
                if (call.isCanceled()){
                    return;
                }
                callback.onFailure(t);
            }
        });
    }
    public interface HandleAutoComplete{
        public void onSuccess(AutoCompleteResult result);
        public void onFailure(Throwable t);
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
                            manualLocationBtn.setEnabled(true); manualLocationBtn.setAlpha(1.0f);
                            enableFinalizeBtn();
                            currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            deliveryLocation=currentLocation; //setting the current location as the deli location
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, mMap.getCameraPosition().zoom));
                            drawMarker(currentLocation); //animated marker
                            marker_anim.setVisibility(View.VISIBLE);
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        myLocationLoadingAnim.setVisibility(View.GONE);
                        myLocationBtn.setEnabled(true); myLocationBtn.setAlpha(1.0f);//reenabling the button
                        manualLocationBtn.setEnabled(true); manualLocationBtn.setAlpha(1.0f);
                        Toast.makeText(SelectLocation.this, "Failed to fetch location", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //function to move the animated marker to the target location
    public void drawMarker(LatLng location){
        Point markerPos = mMap.getProjection().toScreenLocation(location);
        marker_anim.setX(markerPos.x - marker_anim.getWidth()/2f);
        marker_anim.setY(markerPos.y - marker_anim.getHeight()/2f);
    }

    public void enableFinalizeBtn(){
        if (!finalizeLocationBtn.isEnabled()){
            finalizeLocationBtn.setEnabled(true);
            finalizeLocationBtn.setAlpha(1.0f);
        }
    }

    //function to load sl json map
    public String loadJsonMap(Context context, String filename){
        try{
            InputStream input = context.getAssets().open(filename);
            int size = input.available();
            byte[] buffer = new byte[size];
            input.read(buffer);
            input.close();
            return new String(buffer, "UTF-8");
        } catch (IOException ex){
            ex.printStackTrace();
            return null;
        }
    }

    public boolean checkIfCoordsInMap(LatLng coords, String countryJson){
        //loading sri lanka json map to check coords later
        GeoJsonReader GeoReader = new GeoJsonReader();
        try {
            Geometry polygon = GeoReader.read(countryJson);
            GeometryFactory factory = new GeometryFactory();
            org.locationtech.jts.geom.Point point = factory.createPoint(new Coordinate(coords.longitude, coords.latitude));
            if (polygon.contains(point)){
                return true;
            } else {
                return false;
            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

}