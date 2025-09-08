package com.example.pizza_mania.customerAccount;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pizza_mania.R;
import com.example.pizza_mania.utils.BottomNavigationHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import es.dmoral.toasty.Toasty;

public class AccountSettings extends AppCompatActivity {

    Button btnAccount, btnLogOut;
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
        setContentView(R.layout.activity_account_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        BottomNavigationHelper.setupBottomNavigation(this, bottomNavigationView, R.id.nav_profile);

        btnAccount = findViewById(R.id.btnAccount);
        btnLogOut = findViewById(R.id.btnLogOut);
        txtFirstName = findViewById(R.id.txtFirstName);
        loadingLayout = findViewById(R.id.loadingLayout);
        contentLayout = findViewById(R.id.contentLayout);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser =auth.getCurrentUser();
        uid = currentUser.getUid();

        loadCustomerDetails();
        btnLogOut.setOnClickListener(v->logout());
        btnAccount.setOnClickListener(v->startActivity(new Intent(AccountSettings.this, UpdateAccount.class)));

    }

    public void logout(){
        new AlertDialog.Builder(this).setTitle("Do you want to log out").setMessage("You will be signed out from your account.")
                .setPositiveButton("Yes", (dialog, which)->{
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(AccountSettings.this, LoginActivity.class));
                    finish();
                })
                .setNegativeButton("No", (dialog, which)->{
                    dialog.dismiss();

                }).show();
    }

    public void loadCustomerDetails(){
        loadingLayout.setVisibility(View.VISIBLE);
        contentLayout.setVisibility(View.GONE);
        if(currentUser == null){
            Toasty.error(AccountSettings.this, "User not logged in!", Toasty.LENGTH_SHORT).show();
            return;
        }

        db.collection("Customer").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                loadingLayout.setVisibility(View.GONE);
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()){
                        String firstName = document.getString("firstName");
                        txtFirstName.setText(firstName);
                        contentLayout.setVisibility(View.VISIBLE);
                    }
                    else{
                        Toasty.error(AccountSettings.this, "No details found!", Toasty.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toasty.error(AccountSettings.this, "Error", Toasty.LENGTH_SHORT).show();
                }
            }
        });
    }
}