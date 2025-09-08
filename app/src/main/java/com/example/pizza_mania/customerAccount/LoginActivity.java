package com.example.pizza_mania.customerAccount;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pizza_mania.MainActivity;
import com.example.pizza_mania.R;
import com.example.pizza_mania.customerHome.CustomerHome;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import es.dmoral.toasty.Toasty;

public class LoginActivity extends AppCompatActivity {

    EditText txtEmail, txtPassword;
    TextView  txtCreate;
    Button btnLogin;
    ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        txtEmail = findViewById(R.id.txtEmail);
        txtPassword = findViewById(R.id.txtPassword);
        txtCreate = findViewById(R.id.txtCreate);
        btnLogin = findViewById(R.id.btnSignIn);
        progressBar = findViewById(R.id.progressBar);

        btnLogin.setOnClickListener(v-> login());
        txtCreate.setOnClickListener(v-> startActivity(new Intent(LoginActivity.this, SignupMain.class)));
    }

    public void login(){
        String email = txtEmail.getText().toString();
        String password = txtPassword.getText().toString();

        boolean isValidated = validateDate(email, password);
        if(!isValidated){
            return;
        }
        loginAccountInFirebase(email,password);

    }

    public void loginAccountInFirebase(String email, String password){
        FirebaseAuth firebaseAuth =FirebaseAuth.getInstance();
        changeInProgress(true);
        firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                changeInProgress(false);
                if(task.isSuccessful()){
                    if(firebaseAuth.getCurrentUser().isEmailVerified()){
                        Intent intent = new Intent(LoginActivity.this, CustomerHome.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                    else {
                        Toasty.error(LoginActivity.this, "Please very email", Toast.LENGTH_SHORT, true).show();
                    }
                }
                else {
                    Toasty.error(LoginActivity.this, "Invalid details. Please try again", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    void changeInProgress(boolean inProgress){
        if(inProgress){
            btnLogin.setVisibility(TextView.GONE);
            progressBar.setVisibility(TextView.VISIBLE);
        }else {
            btnLogin.setVisibility(TextView.VISIBLE);
            progressBar.setVisibility(TextView.GONE);
        }
    }

    boolean validateDate(String email, String password){
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            txtEmail.setError("Email is invalid");
            return false;
        }
        if(password.length()<6){
            txtPassword.setError("Password length is too small");
            return false;
        }
        return true;
    }
}