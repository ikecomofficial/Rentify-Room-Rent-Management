package com.example.rentify_roomrentmanagement;

import android.os.Bundle;
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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class EditProperty extends AppCompatActivity {

    private EditText etPropertyName, etPropertyAddress, etDefaultRentAmount, etDefaultUnitRate;
    private String property_id, property_name, property_address;
    private long default_rent, default_unit_rate, newRent, newUnitRate;
    DatabaseReference propertyReference, roomReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_property);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        property_id = getIntent().getStringExtra("property_id");
        property_name = getIntent().getStringExtra("property_name");
        property_address = getIntent().getStringExtra("property_address");
        default_rent = getIntent().getLongExtra("prop_room_rent",0);
        default_unit_rate = getIntent().getLongExtra("prop_unit_rate", 0);


        propertyReference = FirebaseDatabase.getInstance().getReference().child("properties").child(property_id);
        roomReference = FirebaseDatabase.getInstance().getReference().child("rooms");

        etPropertyName = findViewById(R.id.etUpdatePropertyName);
        etPropertyAddress = findViewById(R.id.etUpdatePropertyAddress);
        etDefaultRentAmount = findViewById(R.id.etUpdateDefaultRent);
        etDefaultUnitRate = findViewById(R.id.etUpdateUnitRate);
        TextView updateProperty = (TextView) findViewById(R.id.btnUpdateProperty);

        etPropertyName.setText(property_name);
        etPropertyAddress.setText(property_address);
        etDefaultRentAmount.setText(String.valueOf(default_rent));
        etDefaultUnitRate.setText(String.valueOf(default_unit_rate));

        updateProperty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updatePropertyToFirebase();
                updateRoomRentToFirebase();
            }
        });
    }

    private void updatePropertyToFirebase(){
        String newPropertyName = etPropertyName.getText().toString().trim();
        String newPropertyAddress = etPropertyAddress.getText().toString().trim();
        String newpPropertyDefaultRent = etDefaultRentAmount.getText().toString().trim();
        String newPropertyUnitRate = etDefaultUnitRate.getText().toString().trim();

        if (newPropertyName.isEmpty()) {
            etPropertyName.setError("Enter property name");
            return;
        }
        if (newPropertyAddress.isEmpty()) {
            etPropertyAddress.setError("Enter city/address");
            return;
        }
        if (newpPropertyDefaultRent.isEmpty()) {
            etDefaultRentAmount.setError("Enter Rent Amount");
            return;
        }
        if (newPropertyUnitRate.isEmpty()){
            etDefaultUnitRate.setError("Enter Electricity Unit Rate");
            return;
        }

        HashMap<String, Object> propertyUpdateMap = new HashMap<>();
        propertyUpdateMap.put("prop_room_rent", Integer.parseInt(newpPropertyDefaultRent));

        if(!newPropertyName.equals(property_name)){
            propertyUpdateMap.put("property_name", newPropertyName);
        }
        if (!newPropertyAddress.equals(property_address)){
            propertyUpdateMap.put("property_address", newPropertyAddress);
        }
        newRent = Long.parseLong(newpPropertyDefaultRent);
        if (newRent != default_rent){
            propertyUpdateMap.put("property_default_rent", Integer.parseInt(newpPropertyDefaultRent));
        }
        newUnitRate = Long.parseLong(newPropertyUnitRate);
        if (newUnitRate != default_unit_rate){
            propertyUpdateMap.put("prop_unit_rate", Integer.parseInt(newPropertyUnitRate));
        }

        propertyReference.updateChildren(propertyUpdateMap).addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Property Updated", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());

    }

    private void updateRoomRentToFirebase(){
        roomReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Object> updates = new HashMap<>();
                for (DataSnapshot roomSnapshot : snapshot.getChildren()){
                    String room_id = roomSnapshot.getKey();
                    String pid = roomSnapshot.child("property_id").getValue(String.class);
                    Boolean isRentCustom = roomSnapshot.child("is_rent_custom").getValue(Boolean.class);
                    Boolean isUnitCustom = roomSnapshot.child("is_unit_custom").getValue(Boolean.class);
                    if (pid != null && pid.equals(property_id)) {
                        if (room_id != null) {
                            if (Boolean.FALSE.equals(isRentCustom)){
                                updates.put(room_id + "/room_rent", newRent);
                            }
                            if (Boolean.FALSE.equals(isUnitCustom)){
                                updates.put(room_id + "/elc_unit_rate", newUnitRate);
                            }
                        }
                    }
                }
                if (!updates.isEmpty()) {
                    roomReference.updateChildren(updates)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(EditProperty.this, "Rooms Updated", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> Toast.makeText(EditProperty.this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
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