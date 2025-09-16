package com.example.pizza_mania.customerAccount;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pizza_mania.R;
import com.example.pizza_mania.customerHome.CustomerHome;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class UpdateAccount extends AppCompatActivity {

    EditText txtFirstName, txtLastName, txtContact, txtEmail;
    Button btnUpdate, btnDelete, btnLogOut;
    ImageView btnArrow;
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
        setContentView(R.layout.activity_update_account);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        txtFirstName = findViewById(R.id.txtFirstName);
        txtLastName = findViewById(R.id.txtLastName);
        txtContact = findViewById(R.id.txtContact);
        txtEmail = findViewById(R.id.txtEmail);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);
        btnLogOut = findViewById(R.id.btnLogOut);
        loadingLayout = findViewById(R.id.loadingLayout);
        contentLayout = findViewById(R.id.contentLayout);
        btnArrow = findViewById(R.id.btnArrow);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser =auth.getCurrentUser();
        uid = currentUser.getUid();

        loadCustomerDetails();
        btnArrow.setOnClickListener(v-> startActivity(new Intent(UpdateAccount.this, CustomerHome.class)));
        btnUpdate.setOnClickListener(v-> updateDate());
        btnDelete.setOnClickListener(v-> deleteAccount());
        btnLogOut.setOnClickListener(v-> logout());
    }

    public void changeInProgress(boolean inProgress){
        if(inProgress){
            btnUpdate.setEnabled(false);
        } else {
            btnUpdate.setEnabled(true);
        }
    }

    public void loadCustomerDetails(){
        changeInProgress(true);
        loadingLayout.setVisibility(View.VISIBLE);
        contentLayout.setVisibility(View.GONE);
        if(currentUser == null){
            Toasty.error(UpdateAccount.this, "User not logged in!", Toasty.LENGTH_SHORT).show();
            return;
        }

        db.collection("Customer").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                changeInProgress(false);
                loadingLayout.setVisibility(View.GONE);
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()){
                        String firstName = document.getString("firstName");
                        String lastName = document.getString("lastName");
                        String contact = document.getString("contact");
                        String email = currentUser.getEmail();

                        contentLayout.setVisibility(View.VISIBLE);

                        txtFirstName.setText(firstName);
                        txtLastName.setText(lastName);
                        txtContact.setText(contact);
                        txtEmail.setText(email);
                    }
                    else{
                        Toasty.error(UpdateAccount.this, "No details found!", Toasty.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toasty.error(UpdateAccount.this, "Error", Toasty.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void updateDate(){
        String firsName = txtFirstName.getText().toString().trim();
        String lastName = txtLastName.getText().toString().trim();
        String contact = txtContact.getText().toString().trim();

        if(firsName.isEmpty() || lastName.isEmpty() || contact.isEmpty()){
            Toasty.error(UpdateAccount.this, "Please fill all details", Toasty.LENGTH_SHORT).show();
            return;
        }
        if(currentUser == null){
            Toasty.error(UpdateAccount.this, "User not logged in!", Toasty.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("firstName", firsName);
        updateData.put("lastName", lastName);
        updateData.put("contact", contact);

        db.collection("Customer").document(uid).set(updateData).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toasty.success(UpdateAccount.this, "Successfully Updated", Toasty.LENGTH_SHORT).show();
                    startActivity(new Intent(UpdateAccount.this, CustomerHome.class));
                }
                else{
                    Toasty.error(UpdateAccount.this, "Error", Toasty.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void deleteAccount(){

        new AlertDialog.Builder(this).setTitle("Delete Account").setMessage("Do you really want delete the account")
                .setPositiveButton("Yes", (dialog,which)->{
                    if (currentUser == null) {
                        Toasty.error(this, "User not logged in!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    db.collection("Customer").document(uid).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                currentUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Toasty.success(UpdateAccount.this, "Account deleted successfully", Toasty.LENGTH_SHORT).show();
                                            startActivity(new Intent(UpdateAccount.this, LoginActivity.class));
                                            finish();
                                        }
                                        else{
                                            Toasty.error(UpdateAccount.this, "Failed to delete user", Toasty.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                            else{
                                Toasty.error(UpdateAccount.this, "Failed to delete", Toasty.LENGTH_SHORT).show();
                            }
                        }
                    });
                })
                .setNegativeButton("No", (dialog, which)->{
            dialog.dismiss();

        }).show();



    }

    public void logout(){
        new androidx.appcompat.app.AlertDialog.Builder(this).setTitle("Do you want to log out").setMessage("You will be signed out from your account.")
                .setPositiveButton("Yes", (dialog, which)->{
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(UpdateAccount.this, LoginActivity.class));
                    finish();
                })
                .setNegativeButton("No", (dialog, which)->{
                    dialog.dismiss();

                }).show();
    }

}