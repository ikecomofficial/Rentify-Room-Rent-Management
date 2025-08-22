package com.example.rentify_roomrentmanagement;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccountSetting extends AppCompatActivity {

    private TextView tvName, tvEmail, tvBtnSignOut;
    private CircleImageView cimgProfileImage;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_account_setting);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        tvName = findViewById(R.id.text_display_name);
        tvEmail = findViewById(R.id.text_display_email);
        cimgProfileImage = findViewById(R.id.acc_setting_profile_image);
        tvBtnSignOut = findViewById(R.id.text_sign_out_button);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null){
            String providerId = user.getProviderId();
            if (providerId.equals("google.com")){

                // Configure Google Sign In to get the client for sign-out
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();
                mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

                tvName.setText(user.getDisplayName());
                tvEmail.setText(user.getEmail());

                // Taking url of Google Signed In user Profile Pic
                String originalProfileUrl = user.getPhotoUrl().toString();

                // Replace the size suffix (e.g., =s96-c) with your own
                String thumbProfileUrl = originalProfileUrl.replaceAll("=s\\d+-c", "=s200-c");
                Glide.with(this)
                        .load(thumbProfileUrl)
                        .placeholder(R.drawable.ic_tenant_profile_default)
                        .into(cimgProfileImage);
                tvBtnSignOut.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        googleSignOut();
                    }
                });
            } else {

                tvEmail.setText(user.getEmail());

                tvBtnSignOut.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        emailSignOut();
                    }
                });
            }
        }
    }

    private void googleSignOut(){
        // First, sign out from Firebase

        AlertDialog.Builder builder = new AlertDialog.Builder(this); // 'this' is your context
        builder.setTitle("Sign Out?");
        builder.setMessage("Want to Sign Out from This Account?");

        // Positive button -> Yes
        builder.setPositiveButton("Sign Out", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Perform sign out here
                mAuth.signOut();

                // Next, sign out from Google
                mGoogleSignInClient.signOut().addOnCompleteListener(AccountSetting.this, task -> {
                    Toast.makeText(AccountSetting.this, "Signed out successfully", Toast.LENGTH_SHORT).show();
                    goToLoginScreen(); // Redirect to the login screen after successful sign out
                });
            }
        });

        // Negative button -> No
        builder.setNegativeButton("Stay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing, just dismiss
            }
        });

        // Show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void emailSignOut(){
        // First, sign out from Firebase

        AlertDialog.Builder builder = new AlertDialog.Builder(this); // 'this' is your context
        builder.setTitle("Sign Out?");
        builder.setMessage("Want to Sign Out from This Account?");

        // Positive button -> Yes
        builder.setPositiveButton("Sign Out", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mAuth.signOut();
                // Redirect to login screen
                Intent intent = new Intent(AccountSetting.this, LoginScreen.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                goToLoginScreen();
                finish();
            }
        });

        // Negative button -> No
        builder.setNegativeButton("Stay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing, just dismiss
            }
        });

        // Show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void goToLoginScreen() {
        Intent intent = new Intent(this, LoginScreen.class);
        startActivity(intent);
        finish(); // End MainActivity so the user can't press back to it
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        finish();
    }
}