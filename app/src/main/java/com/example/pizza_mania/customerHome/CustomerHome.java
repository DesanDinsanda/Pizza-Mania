package com.example.pizza_mania.customerHome;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pizza_mania.GlobalApp;
import com.example.pizza_mania.R;
import com.example.pizza_mania.customerAccount.AccountSettings;
import com.example.pizza_mania.utils.BottomNavigationHelper;
import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import es.dmoral.toasty.Toasty;

public class CustomerHome extends AppCompatActivity {

    TextView txtFirstName;
    LinearLayout loadingLayout;
    ConstraintLayout contentLayout;
    FirebaseAuth auth;
    FirebaseFirestore db;
    FirebaseUser currentUser;
    String uid;
    private View overlayLoading;
    private TextView fetching_location_text;
    private Animation text_loading_animation;
    private int locationPermReqCode = 01;
    private LatLng currentLocation;

    private FusedLocationProviderClient flpClient;
    private GlobalApp globalApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //adding fetch location loading anim
        globalApp = (GlobalApp) getApplication();
        ViewGroup rootView = findViewById(android.R.id.content);
        overlayLoading = getLayoutInflater().inflate(R.layout.loading_fetching_location, rootView, false);
        rootView.addView(overlayLoading);
        fetching_location_text = overlayLoading.findViewById(R.id.fetchingLocationText);
        text_loading_animation = AnimationUtils.loadAnimation(this, R.anim.loading_text_anim);

        flpClient = LocationServices.getFusedLocationProviderClient(this); //for fetching current location


        txtFirstName = findViewById(R.id.txtFirstName);
        loadingLayout = findViewById(R.id.loadingLayout);
        contentLayout = findViewById(R.id.contentLayout);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser =auth.getCurrentUser();
        uid = currentUser.getUid();

        loadCustomerDetails();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        BottomNavigationHelper.setupBottomNavigation(this, bottomNavigationView, R.id.nav_home);

        fetchLocation();
    }

    public void loadCustomerDetails(){
        loadingLayout.setVisibility(View.VISIBLE);
        contentLayout.setVisibility(View.GONE);
        if(currentUser == null){
            Toasty.error(CustomerHome.this, "User not logged in!", Toasty.LENGTH_SHORT).show();
            return;
        }

        db.collection("Customer").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    loadingLayout.setVisibility(View.GONE);
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()){
                        String firstName = document.getString("firstName");
                        txtFirstName.setText(firstName);
                        contentLayout.setVisibility(View.VISIBLE);
                    }
                    else{
                        Toasty.error(CustomerHome.this, "No details found!", Toasty.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toasty.error(CustomerHome.this, "Error", Toasty.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void fetchLocation(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},locationPermReqCode );
        }
        showLoadingAnim();
        CurrentLocationRequest req = new CurrentLocationRequest.Builder().setPriority(Priority.PRIORITY_HIGH_ACCURACY).build();
        flpClient.getCurrentLocation(req, null).addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                hideLoadingAnim();
                currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                globalApp.setCurrentLocation(currentLocation);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                fetching_location_text.clearAnimation();
                fetchLocation();
            }
        });
    }

    private void showLoadingAnim(){
        fetching_location_text.startAnimation(text_loading_animation);
        overlayLoading.setVisibility(View.VISIBLE);
        overlayLoading.setClickable(true);
    }
    private void hideLoadingAnim(){
        fetching_location_text.clearAnimation();
        overlayLoading.setClickable(false);
        overlayLoading.setVisibility(View.GONE);
    }
}