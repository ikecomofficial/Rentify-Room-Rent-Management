package com.example.rentify_roomrentmanagement;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class AddTenantActivity extends AppCompatActivity {

    private EditText etTenantName, etTenantPhone, etTenantAddress;
    private TextView btnAddTenant;
    private String user_id, room_id, tenant_id, property_id;
    private boolean is_room;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference, tenantReference, roomReference, propertyReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_tenant);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        room_id = getIntent().getStringExtra("room_id");
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        user_id = user.getUid();

        databaseReference = FirebaseDatabase.getInstance().getReference();
        roomReference = databaseReference.child("rooms").child(room_id);
        tenantReference = databaseReference.child("tenants");
        propertyReference = databaseReference.child("properties");

        etTenantName = findViewById(R.id.editTextTenantName);
        etTenantPhone = findViewById(R.id.editTextTenantPhone);
        etTenantAddress = findViewById(R.id.editTextTenantAddress);
        btnAddTenant = findViewById(R.id.btnAddTenant);

        btnAddTenant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (saveTenantToFirebase()){
                    updateRoomDatabase();
                    updatePropertyDatabase();
                }
            }
        });
    }
    private boolean saveTenantToFirebase(){
        String tenantName = etTenantName.getText().toString().trim();
        String tenantPhone = etTenantPhone.getText().toString().trim();
        String tenantAddress = etTenantAddress.getText().toString().trim();
        String todayDate = new java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.US)
                .format(new java.util.Date());

        if (tenantName.isEmpty()) {
            etTenantName.setError("Enter Tenant Name");
            return false;
        }
        if (tenantAddress.isEmpty()) {
            etTenantAddress.setError("Enter City/Address");
            return false;
        }

        // Create unique tenant ID
        tenant_id = tenantReference.push().getKey();
        HashMap<String, Object> tenantMap = new HashMap<>();
        tenantMap.put("tenant_name", tenantName);
        tenantMap.put("tenant_phone", tenantPhone.isEmpty() ? "N/A" : tenantPhone);
        tenantMap.put("tenant_address", tenantAddress);
        tenantMap.put("tenant_profile_url", "default");
        tenantMap.put("thumb_tenant_url", "default");
        tenantMap.put("tenant_start_date", todayDate);
        tenantMap.put("tenant_end_date", "null");
        tenantMap.put("room_id", room_id);
        tenantMap.put("user_id", user_id);

        if (tenant_id != null) {
            tenantReference.child(tenant_id).setValue(tenantMap)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Tenant Added Successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
        return true;
    }

    private void updateRoomDatabase(){
        Map<String, Object> roomUpdates = new HashMap<>();
        roomUpdates.put("is_occupied", true);
        roomUpdates.put("tenant_id", tenant_id);
        roomReference.updateChildren(roomUpdates);
    }
    private void updatePropertyDatabase(){
        // Get pid from rooms -> rid data
        roomReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                property_id = snapshot.child("property_id").getValue(String.class);
                is_room = snapshot.child("is_room").getValue(Boolean.class);

                // Now Update the occupied Rooms/Shops in PID
                if (is_room){
                    propertyReference.child(property_id).child("rooms_occupied").setValue(ServerValue.increment(1));
                }else {
                    propertyReference.child(property_id).child("shops_occupied").setValue(ServerValue.increment(1));      }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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