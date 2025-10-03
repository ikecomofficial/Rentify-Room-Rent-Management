package com.example.rentify_roomrentmanagement;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class PropertyDetailsActivity extends AppCompatActivity {

    private TextView tvPropertyName, tvPropertyAddress, tvOccupied, tvVacant;
    private String property_name, property_address, property_id;
    private long property_default_rent, property_unit_rate;
    private long rooms_occupied, shops_occupied, total_rooms, total_shops;
    private RecyclerView roomsRecyclerView;
    private RoomCardAdapter roomCardAdapter;
    private List<Rooms> roomsList;
    private DatabaseReference databaseReference, roomsReference, propertiesReference, tenantReference, rentsReference, e_billReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_property_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        databaseReference = FirebaseDatabase.getInstance().getReference();
        property_id = getIntent().getStringExtra("property_id");
        propertiesReference = databaseReference.child("properties").child(property_id);
        roomsReference = databaseReference.child("rooms");
        tenantReference = databaseReference.child("tenants");
        rentsReference = databaseReference.child("rents");
        e_billReference = databaseReference.child("e-bills");

        tvPropertyName = (TextView) findViewById(R.id.tvPropertyName);
        tvPropertyAddress = (TextView) findViewById(R.id.tvPropertyAddress);
        tvOccupied = (TextView) findViewById(R.id.tvOccupiedCount);
        tvVacant = (TextView) findViewById(R.id.tvVacantCount);
        roomsRecyclerView = findViewById(R.id.roomsListRecyclerView);

        roomsRecyclerView.setHasFixedSize(true);
        roomsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        roomsList = new ArrayList<>();
        roomCardAdapter = new RoomCardAdapter(this, roomsList);
        roomCardAdapter.setOnItemClickListener(room -> {
            Intent roomDetailsIntent = new Intent(PropertyDetailsActivity.this, RoomDetailsActivity.class);
            roomDetailsIntent.putExtra("property_id", property_id);
            roomDetailsIntent.putExtra("room_id", room.getRoom_id());
            roomDetailsIntent.putExtra("is_occupied", room.isIs_occupied());
            roomDetailsIntent.putExtra("room_name", room.getRoom_name());
            startActivity(roomDetailsIntent);
        });
        roomsRecyclerView.setAdapter(roomCardAdapter);
        propertiesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                property_name = snapshot.child("property_name").getValue(String.class);
                property_address = snapshot.child("property_address").getValue(String.class);
                property_default_rent = snapshot.child("prop_room_rent").getValue(Long.class);
                property_unit_rate = snapshot.child("prop_unit_rate").getValue(Long.class);
                rooms_occupied = snapshot.child("rooms_occupied").getValue(Long.class);
                shops_occupied = snapshot.child("shops_occupied").getValue(Long.class);
                total_rooms = snapshot.child("total_rooms").getValue(Long.class);
                total_shops = snapshot.child("total_shops").getValue(Long.class);

                Long totalRoomShopOcc = rooms_occupied + shops_occupied;
                Long totalRoomShop = total_rooms + total_shops;
                Long totalRoomShopVacant = totalRoomShop - totalRoomShopOcc;

                tvPropertyName.setText(property_name);
                tvPropertyAddress.setText(property_address);
                tvOccupied.setText(String.valueOf(totalRoomShopOcc));
                tvVacant.setText(String.valueOf(totalRoomShopVacant));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        loadRooms();
    }

    private void loadRooms() {
        roomsReference.orderByChild("property_id").equalTo(property_id)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot roomsSnapshot) {
                        roomsList.clear();

                        for (DataSnapshot roomSnap : roomsSnapshot.getChildren()) {
                            String roomId = roomSnap.getKey();
                            String roomName = roomSnap.child("room_name").getValue(String.class);
                            Integer rentAmount = roomSnap.child("room_rent").getValue(Integer.class);
                            Boolean isOccupied = roomSnap.child("is_occupied").getValue(Boolean.class);
                            String tenantId = roomSnap.child("tenant_id").getValue(String.class);
                            Integer roomNo = roomSnap.child("room_no").getValue(Integer.class);

                            Rooms model = new Rooms();
                            model.setRoom_id(roomId);
                            model.setRoom_name(roomName);
                            model.setRoom_rent(rentAmount != null ? rentAmount : 0);
                            model.setIs_occupied(isOccupied != null && isOccupied);
                            model.setRoom_no(roomNo != null ? roomNo : 0);

                            if (tenantId != null && !tenantId.equals("null") && !tenantId.isEmpty()) {
                                tenantReference.child(roomId).child(tenantId).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot tenantSnap) {
                                        if (tenantSnap.exists()) {
                                            model.setTenant_id(tenantId);
                                            model.setTenant_name(tenantSnap.child("tenant_name").getValue(String.class));
                                            model.setTenant_phone(tenantSnap.child("tenant_phone").getValue(String.class));
                                            model.setThumb_tenant_url(tenantSnap.child("thumb_tenant_url").getValue(String.class));
                                        } else {
                                            model.setTenant_name("No Tenant");
                                            model.setTenant_phone("");
                                            model.setThumb_tenant_url(null);
                                        }
                                        roomsList.add(model);
                                        // 🔥 sort by room_name (numeric if possible)
                                        roomsList.sort(Comparator.comparingInt(Rooms::getRoom_no));
                                        roomCardAdapter.notifyDataSetChanged();
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {}
                                });

                            } else {
                                model.setTenant_name("No Tenant");
                                model.setTenant_phone("");
                                model.setThumb_tenant_url(null);
                                roomsList.add(model);
                                // 🔥 sort by room_name (numeric if possible)
                                roomsList.sort(Comparator.comparingInt(Rooms::getRoom_no));
                                roomCardAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
    private void deletePropertyFromFirebase(){

        // Query all rooms
        roomsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot roomDeleteSnap : snapshot.getChildren()) {
                    String roomPropertyId = roomDeleteSnap.child("property_id").getValue(String.class);

                    if (roomPropertyId != null && roomPropertyId.equals(property_id)) {
                        String del_room_id = roomDeleteSnap.getKey();

                        // Delete Tenant, Rent and Ebills for all rooms of the opened property.
                        rentsReference.child(del_room_id).removeValue();
                        e_billReference.child(del_room_id).removeValue();
                        tenantReference.child(del_room_id).removeValue();
                        // Delete this room
                        roomsReference.child(del_room_id).removeValue();
                    }
                }
                // Remove Property
                propertiesReference.removeValue().addOnSuccessListener(aVoid -> {
                            finish(); // closes PropertyDetailsActivity
                            Toast.makeText(PropertyDetailsActivity.this, "Deleted Rooms & Properties", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(PropertyDetailsActivity.this, "Failed to delete property", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Failed to delete rooms: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void propertyDeleteConfirmation(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this); // 'this' is your context
        builder.setTitle("Confirm Deletion..!!");
        builder.setMessage("Do You Want to Delete this Property?");

        // Positive button -> Yes
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Perform deletion here
                //deleteItem();
                deletePropertyFromFirebase();
            }
        });

        // Negative button -> No
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing, just dismiss
                //dialog.dismiss();
                Toast.makeText(PropertyDetailsActivity.this, "Deletion Cancelled", Toast.LENGTH_SHORT).show();
            }
        });

        // Show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.property_details_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_edit_property) {
            Intent editPropertyIntent = new Intent(PropertyDetailsActivity.this, EditProperty.class);
            editPropertyIntent.putExtra("property_id", property_id);
            editPropertyIntent.putExtra("property_name", property_name);
            editPropertyIntent.putExtra("property_address", property_address);
            editPropertyIntent.putExtra("prop_room_rent", property_default_rent);
            editPropertyIntent.putExtra("prop_unit_rate", property_unit_rate);
            startActivity(editPropertyIntent);
            return true;
        }
        else if (id == R.id.action_delete_property) {
            // Confirm and delete property
            propertyDeleteConfirmation();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onItemClick(Rooms room) {

    }
}