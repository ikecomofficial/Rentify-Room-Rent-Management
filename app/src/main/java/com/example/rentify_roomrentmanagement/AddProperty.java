package com.example.rentify_roomrentmanagement;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Locale;

public class AddProperty extends AppCompatActivity {

    private EditText etPropertyName, etPropertyAddress, etDefaultRentAmount, etUnitRate;
    private TextView textTotalRooms, textTotalShops;
    private int currTotalRooms = 0;
    private int currTotalShops = 0;

    private String userId;
    private String pid;
    String currTimestamp;
    private DatabaseReference propertyReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_property);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Adjust/Scroll Layout to move view on top of keyboard.
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        NestedScrollView scrollView = findViewById(R.id.main);
        scrollView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            // Get visible area of the screen
            Rect r = new Rect();
            scrollView.getWindowVisibleDisplayFrame(r);
            int screenHeight = scrollView.getRootView().getHeight();

            // Calculate keyboard height
            int keypadHeight = screenHeight - r.bottom;

            if (keypadHeight > screenHeight * 0.15) { // If keyboard is open
                View focusedView = getCurrentFocus();
                if (focusedView != null) {
                    scrollView.post(() -> scrollView.smoothScrollTo(0, focusedView.getBottom()));
                }
            }
        });

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Add New Property");
        }

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        assert user != null;
        userId = user.getUid();

        propertyReference = FirebaseDatabase.getInstance().getReference().child("properties");

        etPropertyName = findViewById(R.id.editTextPropertyName);
        etPropertyAddress = findViewById(R.id.editTextPropertyAddress);
        etDefaultRentAmount = findViewById(R.id.editTextDefaultRent);
        etUnitRate = findViewById(R.id.editTextUnitRate);
        ImageView imgRoomsMinus = findViewById(R.id.imgRoomsMinus);
        textTotalRooms = findViewById(R.id.textTotalRooms);
        ImageView imgRoomsPlus = findViewById(R.id.imgRoomsPlus);
        ImageView imgShopsMinus = findViewById(R.id.imgShopsMinus);
        textTotalShops = findViewById(R.id.textTotalShops);
        ImageView imgShopsPlus = findViewById(R.id.buttonPlus);
        MaterialButton btnCreateProperty = findViewById(R.id.btnCreateProperty);

        textTotalRooms.setText(String.valueOf(currTotalRooms));
        textTotalShops.setText(String.valueOf(currTotalShops));

        btnCreateProperty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (savePropertyToFirebase()){
                    createRoomsShopsInFirebase();
                }
            }
        });

        //Minus Buttons Action On click
        imgRoomsMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currTotalRooms > 0){
                    currTotalRooms--;
                    textTotalRooms.setText(String.valueOf(currTotalRooms));
                }
            }
        });
        imgShopsMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currTotalShops > 0){
                    currTotalShops--;
                    textTotalShops.setText(String.valueOf(currTotalShops));
                }
            }
        });

        // Plus Buttons Action On click
        imgRoomsPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currTotalRooms++;
                textTotalRooms.setText(String.valueOf(currTotalRooms));
            }
        });
        imgShopsPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currTotalShops++;
                textTotalShops.setText(String.valueOf(currTotalShops));
            }
        });

    }

    private boolean savePropertyToFirebase(){

        String propertyName = etPropertyName.getText().toString().trim();
        String propertyAddress = etPropertyAddress.getText().toString().trim();
        String propertyDefaultRent = etDefaultRentAmount.getText().toString().trim();
        String propertyDefaultUnitRate = etUnitRate.getText().toString().trim();

        currTimestamp = String.valueOf(System.currentTimeMillis());

        if (propertyName.isEmpty()) {
            etPropertyName.setError("Enter property name");
            return false;
        }
        if (propertyAddress.isEmpty()) {
            etPropertyAddress.setError("Enter city/address");
            return false;
        }
        if (currTotalRooms == 0 && currTotalShops == 0){
            Toast.makeText(AddProperty.this, "Please Add Rooms or Shops", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (propertyDefaultRent.isEmpty()) {
            etDefaultRentAmount.setError("Enter Rent");
            return false;
        }
        if (propertyDefaultUnitRate.isEmpty()){
            etUnitRate.setError("Enter Electricity Unit Rate");
            return false;
        }

        // Create unique property ID
        pid = propertyReference.push().getKey();
        HashMap<String, Object> propertyMap = new HashMap<>();
        propertyMap.put("property_name", propertyName);
        propertyMap.put("property_address", propertyAddress);
        propertyMap.put("prop_room_rent", Integer.parseInt(propertyDefaultRent));
        propertyMap.put("prop_unit_rate", Integer.parseInt(propertyDefaultUnitRate));
        propertyMap.put("user_id", userId);
        propertyMap.put("total_rooms", Integer.parseInt(String.valueOf(currTotalRooms)));
        propertyMap.put("total_shops", Integer.parseInt(String.valueOf(currTotalShops)));
        propertyMap.put("rooms_occupied", 0);
        propertyMap.put("shops_occupied", 0);
        propertyMap.put("property_created_on", currTimestamp);

        if (pid != null){
            propertyReference.child(pid).setValue(propertyMap)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Property Added Successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
        return true;
    }

    private void createRoomsShopsInFirebase(){
        DatabaseReference roomsReference = FirebaseDatabase.getInstance().getReference().child("rooms");

        for (int i = 1; i<= currTotalRooms; i++){
            String room_id = roomsReference.push().getKey();
            if (room_id != null){
                HashMap<String, Object> roomsMap = new HashMap<>();
                roomsMap.put("room_no", i);
                roomsMap.put("room_name", String.format(Locale.US, "Room %02d", i));
                roomsMap.put("room_rent", Integer.parseInt(etDefaultRentAmount.getText().toString().trim()));
                roomsMap.put("elc_unit_rate", Integer.parseInt(etUnitRate.getText().toString().trim()));
                roomsMap.put("user_id", userId);
                roomsMap.put("property_id", pid);
                roomsMap.put("is_room", true);
                roomsMap.put("is_occupied", false);
                roomsMap.put("created_on", currTimestamp);
                roomsMap.put("is_rent_custom", false);
                roomsMap.put("is_unit_custom", false);

                // Last month paid monthKey.
                roomsMap.put("tenant_id","null");
                roomsMap.put("cm_rent_paid", false);
                roomsMap.put("last_unit_paid", 0);
                roomsMap.put("last_rent_month", "2025-07");

                // Add this code while adding and fetching the rent paid status - refer to text file in backup filen folder

                roomsReference.child(room_id).setValue(roomsMap)
                        .addOnSuccessListener(aVoid -> {
                            finish();
                        });

            }
        }
        for (int i = 1; i<= currTotalShops; i++){
            String room_id = roomsReference.push().getKey();
            if (room_id != null){
                HashMap<String, Object> roomsMap = new HashMap<>();
                roomsMap.put("room_no", currTotalRooms + i);
                roomsMap.put("room_name", String.format(Locale.US, "Shop %02d", i));
                roomsMap.put("room_rent", Integer.parseInt(etDefaultRentAmount.getText().toString().trim()));
                roomsMap.put("elc_unit_rate", Integer.parseInt(etUnitRate.getText().toString().trim()));
                roomsMap.put("user_id", userId);
                roomsMap.put("property_id", pid);
                roomsMap.put("is_room", false);
                roomsMap.put("is_occupied", false);
                roomsMap.put("created_on", currTimestamp);
                roomsMap.put("is_rent_custom", false);
                roomsMap.put("is_unit_custom", false);

                // Last month paid monthKey.
                roomsMap.put("tenant_id","null");
                roomsMap.put("cm_rent_paid", false);
                roomsMap.put("last_unit_paid", 0);
                roomsMap.put("last_rent_month", "2025-07");

                roomsReference.child(room_id).setValue(roomsMap)
                        .addOnSuccessListener(aVoid -> {
                            finish();
                        });
            }
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