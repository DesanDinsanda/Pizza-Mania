package com.example.pizza_mania.customerAccount;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pizza_mania.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class SignupAddDetails extends AppCompatActivity {

    EditText txtFirstName, txtLastName, txtNumber;
    Button btnContinue;

    FirebaseAuth auth;
    FirebaseFirestore db;
    FirebaseUser currentUser;
    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup_add_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        txtFirstName = findViewById(R.id.txtFirstName);
        txtLastName = findViewById(R.id.txtLastName);
        txtNumber = findViewById(R.id.txtNumber);
        btnContinue = findViewById(R.id.btnContinue);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = auth.getCurrentUser();
        uid = currentUser.getUid();

        btnContinue.setOnClickListener(v-> saveCustomerDetails());
    }

    public void saveCustomerDetails(){
        String firstName = txtFirstName.getText().toString().trim();
        String lastName = txtLastName.getText().toString().trim();
        String number = txtNumber.getText().toString().trim();

        if(firstName.isEmpty() || lastName.isEmpty() || number.isEmpty()){
            Toasty.error(SignupAddDetails.this, "Please fill all details", Toasty.LENGTH_SHORT).show();
            return;
        }

        if(currentUser == null){
            Toasty.error(SignupAddDetails.this, "User not logged in!", Toasty.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> customer =new HashMap<>();
        customer.put("firstName", firstName);
        customer.put("lastName", lastName);
        customer.put("contact", number);

        DocumentReference docRef =db.collection("Customer").document(uid);
        docRef.set(customer).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(SignupAddDetails.this, "Details saved successfully!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SignupAddDetails.this, LoginActivity.class));
                } else {
                    Toast.makeText(SignupAddDetails.this, "Error: " + task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });


    }
}