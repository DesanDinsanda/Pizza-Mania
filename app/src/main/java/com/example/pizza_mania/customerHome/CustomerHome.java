package com.example.pizza_mania.customerHome;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pizza_mania.R;
import com.example.pizza_mania.customerAccount.AccountSettings;
import com.example.pizza_mania.utils.BottomNavigationHelper;
import com.google.android.gms.tasks.OnCompleteListener;
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
}