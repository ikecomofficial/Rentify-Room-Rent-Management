package com.example.rentify_roomrentmanagement;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class EditTenant extends AppCompatActivity {

    private EditText etTenantName, etTenantPhone, etTenantAddress;
    private String room_id, tenant_id, tenant_name, tenant_phone, tenant_address;
    private DatabaseReference tenantReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_tenant);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        room_id = getIntent().getStringExtra("room_id");
        tenant_id = getIntent().getStringExtra("tenant_id");
        tenant_name = getIntent().getStringExtra("tenant_name");
        tenant_phone = getIntent().getStringExtra("tenant_phone");
        tenant_address = getIntent().getStringExtra("tenant_address");

        tenantReference = FirebaseDatabase.getInstance().getReference().child("tenants")
                .child(room_id).child(tenant_id);

        etTenantName = findViewById(R.id.etEditTenantName);
        etTenantPhone = findViewById(R.id.etEditTenantPhone);
        etTenantAddress = findViewById(R.id.etEditTenantAddress);
        TextView updateTenant = (TextView) findViewById(R.id.btnUpdateTenant);

        etTenantName.setText(tenant_name);
        etTenantPhone.setText(tenant_phone);
        etTenantAddress.setText(String.valueOf(tenant_address));

        updateTenant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateTenantToFirebase();
            }
        });
    }

    private void updateTenantToFirebase(){
        String newTenantName = etTenantName.getText().toString().trim();
        String newTenantPhone = etTenantPhone.getText().toString().trim();
        String newTenantAddress = etTenantAddress.getText().toString().trim();

        if (newTenantName.isEmpty()) {
            etTenantName.setError("Enter Tenant Name");
            return;
        }if (newTenantPhone.isEmpty()) {
            etTenantPhone.setError("Enter Enter Tenant Phone Number");
            return;
        }if (newTenantAddress.isEmpty()) {
            etTenantAddress.setError("Enter Tenant Address");
            return;
        }
        HashMap<String, Object> tenantUpdateMap = new HashMap<>();

        if(!newTenantName.equals(tenant_name)){
            tenantUpdateMap.put("tenant_name", newTenantName);
        }
        if(!newTenantPhone.equals(tenant_phone)){
            tenantUpdateMap.put("tenant_phone", newTenantPhone);
        }
        if(!newTenantAddress.equals(tenant_address)){
            tenantUpdateMap.put("tenant_address", newTenantAddress);
        }
        tenantReference.updateChildren(tenantUpdateMap).addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Tenant Updated", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());


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