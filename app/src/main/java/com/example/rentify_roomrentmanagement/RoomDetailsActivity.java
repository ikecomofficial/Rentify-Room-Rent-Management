package com.example.rentify_roomrentmanagement;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class RoomDetailsActivity extends AppCompatActivity {

    private TextView tvRoomStatus, tvTenantName, tvTenantPhone, tvTenantStartDate,btnAddTenant;
    private CircleImageView cimgTenantProfilePic;
    private String room_id, tenant_name, tenant_profile_url, tenant_phone, tenant_start_date = "N/A";
    private boolean is_occupied;
    private  GradientDrawable gradientDrawable;
    private DatabaseReference roomReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_room_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        room_id = getIntent().getStringExtra("room_id");
        is_occupied = getIntent().getBooleanExtra("is_occupied", false);

        roomReference = FirebaseDatabase.getInstance().getReference().child("rooms").child(room_id);

        tvRoomStatus = findViewById(R.id.tvRoomStatus);
        tvTenantName = findViewById(R.id.tvTenantName);
        tvTenantPhone = findViewById(R.id.tvTenantPhone);
        tvTenantStartDate = findViewById(R.id.tvStartDate);
        cimgTenantProfilePic = findViewById(R.id.imgProfile);
        btnAddTenant = findViewById(R.id.btnAddTenant);

    }

    @Override
    public void onStart() {
        super.onStart();

        gradientDrawable = (GradientDrawable) tvRoomStatus.getBackground();
        if (!is_occupied){
            btnAddTenant.setVisibility(View.VISIBLE);
            tvRoomStatus.setText("Vacant");
            gradientDrawable.setColor(Color.parseColor("#C0F6695E"));
        } else {
            btnAddTenant.setVisibility(View.GONE);
            tvRoomStatus.setText("Occupied");
            gradientDrawable.setColor(Color.parseColor("#CB5CAF6E"));

            roomReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    tenant_name = snapshot.child("curr_tenant_name").getValue(String.class);
                    tenant_phone = snapshot.child("curr_tenant_phone").getValue(String.class);
                    tenant_profile_url = snapshot.child("thumb_tenant_profile").getValue(String.class);
                    tenant_start_date = snapshot.child("thumb_tenant_profile").getValue(String.class);

                    tvTenantName.setText(tenant_name);
                    tvTenantPhone.setText(tenant_phone);
                    tvTenantStartDate.setText(tenant_start_date);
                    if (tenant_profile_url == null || tenant_profile_url.trim().isEmpty() || tenant_profile_url.equals("N/A")){
                        // Show only placeholder
                        Glide.with(RoomDetailsActivity.this)
                                .load(R.drawable.ic_tenant_profile_default)
                                .into(cimgTenantProfilePic);
                    } else {
                        Glide.with(RoomDetailsActivity.this)
                                .load(tenant_profile_url)
                                .placeholder(R.drawable.ic_tenant_profile_default)
                                .into(cimgTenantProfilePic);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }

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