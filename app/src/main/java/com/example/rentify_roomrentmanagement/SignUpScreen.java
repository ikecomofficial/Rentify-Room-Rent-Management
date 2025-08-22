package com.example.rentify_roomrentmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SignUpScreen extends AppCompatActivity {

    private EditText etName, etEmail, etPhone, etPassword;
    private TextView tvLogin, btnSignUp;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvLogin = findViewById(R.id.tvLogin);


        mAuth = FirebaseAuth.getInstance();

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUpUser();
            }
        });

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignUpScreen.this, LoginScreen.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void signUpUser(){

        String user_name = etName.getText().toString().trim();
        String user_email = etEmail.getText().toString().trim();
        String user_phone = etPhone.getText().toString().trim();
        String user_password = etPassword.getText().toString().trim();

        if (user_name.isEmpty()){
            etName.setError("Enter User Name");
            return;
        }
        if (user_email.isEmpty()){
            etEmail.setError("Enter User E-mail");
            return;
        }
        if (user_phone.isEmpty()){
            etPhone.setError("Enter User Phone No");
            return;
        }
        if (user_password.isEmpty()){
            etPassword.setError("Enter Password");
            return;
        }

        mAuth.createUserWithEmailAndPassword(user_email, user_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    String uid = user.getUid();

                    Toast.makeText(SignUpScreen.this, "Account Created: " + user_email, Toast.LENGTH_SHORT).show();

                    mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
                    HashMap<String, String> userMap = new HashMap<>();
                    userMap.put("name", user_name);
                    userMap.put("email", user.getEmail());
                    userMap.put("user_id", uid);
                    userMap.put("role", "owner");
                    userMap.put("profile_url", "default");
                    userMap.put("thumb_profile_url", "default");
                    userMap.put("created_at", String.valueOf(System.currentTimeMillis()));
                    userMap.put("phone", user_phone);

                    mDatabaseReference.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                goToMainActivity();
                            }
                        }
                    });

                } else {
                    Exception e = task.getException();
                    Log.e("SignUpError", "SignUp failed", e);
                    Toast.makeText(SignUpScreen.this,
                            "Sign Up Failed: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    //Toast.makeText(SignUpScreen.this, "Sign Up Failed.", Toast.LENGTH_SHORT).show();
                }
            }

            private void goToMainActivity(){
                Intent intent = new Intent(SignUpScreen.this, LoginScreen.class);
                startActivity(intent);
                finish();
            }
        });
    }
    @Override
    public void onBackPressed(){
        super.onBackPressed();
        finish();
    }
}