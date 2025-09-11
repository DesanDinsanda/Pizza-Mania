package com.example.pizza_mania.customerAccount;

import android.content.Intent;
import android.os.Bundle;
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

import com.example.pizza_mania.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignupMain extends AppCompatActivity {

    EditText txtEmail, txtPassword, txtConformPassword;
    Button btnSignUp;
    ProgressBar progressBar;
    TextView btnLogin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        txtEmail = findViewById(R.id.txtEmail);
        txtPassword = findViewById(R.id.txtPassword);
        txtConformPassword = findViewById(R.id.txtConfirmPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        progressBar = findViewById(R.id.progressBar);
        btnLogin = findViewById(R.id.txtLogin);

        btnSignUp.setOnClickListener(v-> createAccount());
        btnLogin.setOnClickListener(v-> startActivity(new Intent(SignupMain.this, LoginActivity.class)));
    }

    public void createAccount(){
        String email = txtEmail.getText().toString();
        String password = txtPassword.getText().toString();
        String confirmPassword = txtConformPassword.getText().toString();

        boolean isValidated = validateData(email, password, confirmPassword);
        if(!isValidated){
            return;
        }
        createAccountInFirebase(email, password);
    }

    public void createAccountInFirebase(String email, String password){
        changeInProgress(true);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                changeInProgress(false);
                if(task.isSuccessful()){
                    Toast.makeText(SignupMain.this, "Successfully created account. Check email to verify", Toast.LENGTH_SHORT).show();
                    firebaseAuth.getCurrentUser().sendEmailVerification();
                    //firebaseAuth.signOut();
                    startActivity(new Intent(SignupMain.this, SignupAddDetails.class));
                    finish();
                }
                else{
                    Toast.makeText(SignupMain.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    void changeInProgress(boolean inProgress){
        if(inProgress){
            btnSignUp.setVisibility(TextView.GONE);
            progressBar.setVisibility(TextView.VISIBLE);
        }else {
            btnSignUp.setVisibility(TextView.VISIBLE);
            progressBar.setVisibility(TextView.GONE);
        }
    }

    boolean validateData(String email, String password, String confirmPassword){
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            txtEmail.setError("Email is invalid");
            return false;
        }
        if(password.length()<6){
            txtPassword.setError("Password length is invalid");
            return false;
        }
        if(!password.equals(confirmPassword)){
            txtConformPassword.setError("Password not matched");
            return false;
        }
        return true;
    }
}