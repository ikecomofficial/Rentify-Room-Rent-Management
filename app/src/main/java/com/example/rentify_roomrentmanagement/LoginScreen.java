package com.example.rentify_roomrentmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class LoginScreen extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "GoogleSignIn";
    private EditText etEmail, etPassword;
    private TextView tvCreateAccount, btnLogin;
    private LinearLayout btnGoogleSignIn;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseReference;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        tvCreateAccount = findViewById(R.id.tvCreateAccount);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogleSignIn = findViewById(R.id.google_sign_in_button);

        mAuth = FirebaseAuth.getInstance();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInEmailPassword();
            }
        });

        btnGoogleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInWithGoogle();
            }
        });

        tvCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginScreen.this, SignUpScreen.class);
                startActivity(intent);
            }
        });
    }

    private void signInEmailPassword(){

        String user_email = etEmail.getText().toString().trim();
        String user_password = etPassword.getText().toString().trim();

        if (user_email.isEmpty()){
            etEmail.setError("Enter User E-mail");
            return;
        }
        if (user_password.isEmpty()){
            etPassword.setError("Enter User Name");
            return;
        }

        mAuth.signInWithEmailAndPassword(user_email, user_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Intent intent = new Intent(LoginScreen.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
    private void signInWithGoogle(){

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    private void goToMainActivity() {
        Intent intent = new Intent(LoginScreen.this, MainActivity.class);
        startActivity(intent);
        finish(); // Finish LoginScreen so user can't press back to it
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in on start. If they are, go to MainActivity.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            goToMainActivity();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
                Toast.makeText(this, "Google Sign-In failed.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            String uid = user.getUid();

                            // Taking url of Google Signed In user Profile Pic
                            String originalProfileUrl = user.getPhotoUrl().toString();

                            // Replace the size suffix (e.g., =s96-c) with your own
                            String profileUrl = originalProfileUrl.replaceAll("=s\\d+-c", "=s256-c");
                            String thumbProfileUrl = originalProfileUrl.replaceAll("=s\\d+-c", "=s200-c");  // 200x200 full image

                            Toast.makeText(LoginScreen.this, "Signed In As " + user.getDisplayName(), Toast.LENGTH_SHORT).show();

                            mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
                            HashMap<String, String> userMap = new HashMap<>();
                            userMap.put("name", user.getDisplayName());
                            userMap.put("email", user.getEmail());
                            userMap.put("user_id", uid);
                            userMap.put("role", "owner");
                            userMap.put("profile_url", profileUrl);
                            userMap.put("thumb_profile_url", thumbProfileUrl);
                            userMap.put("created_at", String.valueOf(System.currentTimeMillis()));
                            userMap.put("is_verified", String.valueOf(user.isEmailVerified()));
                            if (user.getPhoneNumber() != null){
                                userMap.put("phone", user.getPhoneNumber());
                            }else {
                                userMap.put("phone", "null");
                            }

                            mDatabaseReference.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        goToMainActivity();
                                    }
                                }
                            });

                        } else {
                            Log.w(TAG, "signInWithCredential failed", task.getException());
                            Toast.makeText(LoginScreen.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}