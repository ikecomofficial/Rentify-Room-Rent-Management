package com.example.rentify_roomrentmanagement;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;



import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class RoomDetailsActivity extends AppCompatActivity {

    private TextView tvRoomStatus, tvTenantName, tvTenantPhone, tvTenantStartDate, btnAddTenant;
    private CircleImageView cimgTenantProfilePic;
    private MaterialCardView btnCall, btnWhatsApp;
    private LinearLayout layoutActions;
    private String room_id, property_id, room_name, tenant_id, tenant_name, tenant_address,
            thumb_tenant_url, tenant_phone, tenant_start_date = "N/A";
    private boolean is_occupied, is_room;
    private SpeedDialView addRecordSpeedDial;
    private ExtendedFloatingActionButton fabAddTenant;

    private TabLayout tabLayout;
    private ViewPager2 viewPager2;
    private RentBillPagerAdapter rentBillPagerAdapter;
    private GradientDrawable gradientDrawable;
    private DatabaseReference databaseReference, roomReference, propertyReference, tenantReference, allTenantReference;

    private FirebaseRecyclerAdapter<Tenants, RoomDetailsActivity.TenantsViewHolder> firebaseTenantsRecyclerAdapter;


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

        room_id = getIntent().getStringExtra("room_id");
        is_occupied = getIntent().getBooleanExtra("is_occupied", false);
        room_name = getIntent().getStringExtra("room_name");
        is_room = getIntent().getBooleanExtra("is_room", false);
        property_id = getIntent().getStringExtra("property_id");


        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(room_name);
        }

        databaseReference = FirebaseDatabase.getInstance().getReference();
        roomReference = databaseReference.child("rooms").child(room_id);
        tenantReference = databaseReference.child("tenants");
        propertyReference = databaseReference.child("properties");
        allTenantReference = tenantReference.child(room_id);


        tvRoomStatus = findViewById(R.id.tvRoomStatus);
        tvTenantName = findViewById(R.id.tvTenantName);
        tvTenantPhone = findViewById(R.id.tvTenantPhone);
        tvTenantStartDate = findViewById(R.id.tvStartDate);
        cimgTenantProfilePic = findViewById(R.id.imgProfile);
        btnAddTenant = findViewById(R.id.btnAddTenant);
        layoutActions = findViewById(R.id.layoutActions);
        btnCall = findViewById(R.id.btnCall);
        btnWhatsApp = findViewById(R.id.btnWhatsApp);
        addRecordSpeedDial = findViewById(R.id.fabAddRecord);
        fabAddTenant = findViewById(R.id.fabAddTenant);

        // Fragment References
        tabLayout = findViewById(R.id.rentTabLayout);
        viewPager2 = findViewById(R.id.rentViewPager);
        rentBillPagerAdapter = new RentBillPagerAdapter(this, room_id);
        viewPager2.setAdapter(rentBillPagerAdapter);




        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager2.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                tabLayout.getTabAt(position).select();
            }
        });

        // Keep it expanded initially
        fabAddTenant.extend();

// Schedule collapse after 10 seconds
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (fabAddTenant.isExtended()) {
                fabAddTenant.shrink();
            }
        }, 10_000); // 10 seconds = 10,000 ms

        btnAddTenant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addTenantIntent = new Intent(RoomDetailsActivity.this, AddTenantActivity.class);
                addTenantIntent.putExtra("room_id", room_id);
                startActivity(addTenantIntent);
            }
        });

        fabAddTenant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addTenantIntent = new Intent(RoomDetailsActivity.this, AddTenantActivity.class);
                addTenantIntent.putExtra("room_id", room_id);
                startActivity(addTenantIntent);
            }
        });

        btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        btnWhatsApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        // Add menu items
        addRecordSpeedDial.addActionItem(
                new SpeedDialActionItem.Builder(R.id.fab_add_rent, R.drawable.ic_rupee_symbol)
                        .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.teal_700, null))
                        .setFabImageTintColor(ContextCompat.getColor(this, R.color.white))
                        .setLabel("Add Rent")
                        .create()
        );

        addRecordSpeedDial.addActionItem(
                new SpeedDialActionItem.Builder(R.id.fab_add_elc_bill, R.drawable.ic_meter_reading)
                        .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.teal_700, null))
                        .setFabImageTintColor(ContextCompat.getColor(this, R.color.white))
                        .setLabel("Add Electricity Bill")
                        .create()
        );

// Handle click actions
        addRecordSpeedDial.getMainFab()
                .setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white)));
        addRecordSpeedDial.setOnActionSelectedListener(actionItem -> {
            if (actionItem.getId() == R.id.fab_add_rent) {
                Intent addRentIntent = new Intent(RoomDetailsActivity.this, AddRentActivity.class);
                addRentIntent.putExtra("room_id", room_id);
                addRentIntent.putExtra("tenant_id", tenant_id);
                startActivity(addRentIntent);
                return false; // closes the speed dial
            } else if (actionItem.getId() == R.id.fab_add_elc_bill) {
                Intent addBillIntent = new Intent(RoomDetailsActivity.this, AddEbillActivity.class);
                addBillIntent.putExtra("room_id", room_id);
                addBillIntent.putExtra("tenant_id", tenant_id);
                startActivity(addBillIntent);
                return false;
            }
            return false;
        });

    }

    private void loadTenantData() {
        tenantReference.child(room_id).child(tenant_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tenant_name = snapshot.child("tenant_name").getValue(String.class);
                tenant_phone = snapshot.child("tenant_phone").getValue(String.class);
                thumb_tenant_url = snapshot.child("thumb_tenant_url").getValue(String.class);
                tenant_start_date = snapshot.child("tenant_start_date").getValue(String.class);
                tenant_address = snapshot.child("tenant_address").getValue(String.class);

                tvTenantName.setText(tenant_name);
                tvTenantPhone.setText(tenant_phone);
                tvTenantStartDate.setText(tenant_start_date);

                if (thumb_tenant_url == null || thumb_tenant_url.trim().isEmpty() || thumb_tenant_url.equals("default")) {
                    // Show only placeholder
                    Glide.with(RoomDetailsActivity.this)
                            .load(R.drawable.ic_tenant_profile_default)
                            .into(cimgTenantProfilePic);
                } else {
                    Glide.with(RoomDetailsActivity.this)
                            .load(thumb_tenant_url)
                            .placeholder(R.drawable.ic_tenant_profile_default)
                            .into(cimgTenantProfilePic);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        gradientDrawable = (GradientDrawable) tvRoomStatus.getBackground();
        if (!is_occupied) {
            btnAddTenant.setVisibility(View.VISIBLE);
            fabAddTenant.setVisibility(View.VISIBLE);
            layoutActions.setVisibility(View.GONE);
            addRecordSpeedDial.setVisibility(View.GONE);
            btnCall.setVisibility(View.GONE);
            btnWhatsApp.setVisibility(View.GONE);

            tvRoomStatus.setText(R.string.text_vac_status);
            gradientDrawable.setColor(Color.parseColor("#C0F6695E"));
        } else {
            addRecordSpeedDial.setVisibility(View.VISIBLE);
            layoutActions.setVisibility(View.VISIBLE);
            btnAddTenant.setVisibility(View.GONE);
            fabAddTenant.setVisibility(View.GONE);
            btnCall.setVisibility(View.VISIBLE);
            btnWhatsApp.setVisibility(View.VISIBLE);
            tvRoomStatus.setText(R.string.text_occ_vac_no);
            gradientDrawable.setColor(Color.parseColor("#CB5CAF6E"));

            roomReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    tenant_id = snapshot.child("tenant_id").getValue(String.class);
                    if (tenant_id != null && !tenant_id.isEmpty() && !tenant_id.equals("null")) {
                        loadTenantData();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
    }

    private void tenantRemoveFromRoomConfirmation(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this); // 'this' is your context
        builder.setTitle("Confirm Tenant Removal..!!");
        builder.setMessage("Want to Remove Current Tenant from this Room?");

        // Positive button -> Yes
        builder.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              removeTenantFromRoom();
                    // Toast.makeText(RoomDetailsActivity.this, "Error Removing Tenant", Toast.LENGTH_SHORT).show();


            }
        });

        // Negative button -> No
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing, just dismiss
                //dialog.dismiss();
                Toast.makeText(RoomDetailsActivity.this, "Deletion Cancelled", Toast.LENGTH_SHORT).show();
            }
        });

        // Show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void removeTenantFromRoom(){

        // Add End date to previous tenant id
        String tenantEndDate = new java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.US)
                .format(new java.util.Date());

        tenantReference.child(room_id).child(tenant_id).child("tenant_end_date").setValue(tenantEndDate);

        HashMap<String, Object> tenantRemoveMap = new HashMap<>();
        tenantRemoveMap.put("is_occupied", false);
        tenantRemoveMap.put("tenant_id", "null");

        roomReference.updateChildren(tenantRemoveMap);

        updatePropertyDatabase();

    }

    private void updatePropertyDatabase(){
        // Now Update the occupied Rooms/Shops in PID
        if (is_room){
            propertyReference.child(property_id).child("rooms_occupied").setValue(ServerValue.increment(-1));
            finish();
        }else {
            propertyReference.child(property_id).child("shops_occupied").setValue(ServerValue.increment(-1));
            finish();
        }
        Toast.makeText(RoomDetailsActivity.this, "Tenant Removed", Toast.LENGTH_SHORT).show();
  }


    private void showBottomSheetTenants(){
        // Create BottomSheetDialog
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);

        // Inflate layout for bottom sheet
        View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_tenant_list, null, false);
        bottomSheetDialog.setContentView(view);

        // Make sure we modify the bottom-sheet container after it is shown
        bottomSheetDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                BottomSheetDialog d = (BottomSheetDialog) dialogInterface;
                FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                if (bottomSheet != null) {
                    // clear default background so your drawable shows through
                    bottomSheet.setBackground(new ColorDrawable(Color.TRANSPARENT));
                    bottomSheet.setClipToPadding(false);
                }
            }
        });

        RecyclerView rvTenants = view.findViewById(R.id.rvAllTenants);
        // Tenants Recycler View
        LinearLayoutManager layoutTenantManager = new LinearLayoutManager(this);

        rvTenants.setLayoutManager(new LinearLayoutManager(this));
        layoutTenantManager.setReverseLayout(true);    // newest items appear at top
        layoutTenantManager.setStackFromEnd(true);    // optional, usually false
        rvTenants.setLayoutManager(layoutTenantManager);

        FirebaseRecyclerOptions<Tenants> tenants_options = new FirebaseRecyclerOptions.Builder<Tenants>()
                .setQuery(allTenantReference, Tenants.class)
                .build();

        firebaseTenantsRecyclerAdapter = new FirebaseRecyclerAdapter<Tenants, TenantsViewHolder>(tenants_options) {
            @Override
            protected void onBindViewHolder(@NonNull RoomDetailsActivity.TenantsViewHolder holder, int position, @NonNull Tenants model) {
                // Bind your data here

                holder.setTenantName(model.getTenant_name());
                holder.setTenantProfileUrl(model.getThumb_tenant_url());
                holder.setTenantPhone(model.getTenant_phone());
                holder.setTenantStartDate(model.getTenant_start_date());
                holder.setTenantEndDate(model.getTenant_end_date());

                }

            @NonNull
            @Override
            public TenantsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.single_tenant_layout, parent, false);
                return new RoomDetailsActivity.TenantsViewHolder(view);
            }

            @Override
            public void onDataChanged() {
                super.onDataChanged();

            }

        };

        rvTenants.setAdapter(firebaseTenantsRecyclerAdapter);
        if (firebaseTenantsRecyclerAdapter != null) {
            rvTenants.setAdapter(firebaseTenantsRecyclerAdapter);
            firebaseTenantsRecyclerAdapter.startListening();
        }

        // Show the bottom sheet
        bottomSheetDialog.show();
    }

    public static class TenantsViewHolder extends RecyclerView.ViewHolder{

        View mView;
        public TenantsViewHolder(View itemView){
            super(itemView);
            mView = itemView;
        }

        public void setTenantProfileUrl(String tenantProfileUrl){
            CircleImageView tenantProfileUrlView = mView.findViewById(R.id.imgItemProfile);

            Glide.with(mView.getContext()).load(tenantProfileUrl)
                    .placeholder(R.drawable.ic_tenant_profile_default)
                    .into(tenantProfileUrlView);

        }

        public void setTenantName(String tenantName){
            TextView tenantNameView = mView.findViewById(R.id.tvItemTenantName);
            tenantNameView.setText(tenantName);
        }

        public void setTenantPhone(String tenantPhone){
            TextView tenantPhoneView = mView.findViewById(R.id.tvItemTenantPhone);
            tenantPhoneView.setText(tenantPhone);
        }
        public void setTenantStartDate(String tenantStartDate){
            TextView tenantStartDateView = mView.findViewById(R.id.tvItemStartDate);
            String finalStartDate = "Start: " + tenantStartDate;
            tenantStartDateView.setText(finalStartDate);
        }
        public void setTenantEndDate(String tenantEndDate){
            TextView tenantEndDateView = mView.findViewById(R.id.tvItemEndDate);
            if (tenantEndDate.equals("null")){
                tenantEndDateView.setText("current Tenant");
            }else {
                String finalEndDate = "End: " + tenantEndDate;
                tenantEndDateView.setText(finalEndDate);
            }
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
    }
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (is_occupied){
            getMenuInflater().inflate(R.menu.room_details_activity_menu, menu);
            return true;
        } else {
           return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_edit_tenant) {
            Intent editTenantIntent = new Intent(RoomDetailsActivity.this, EditTenant.class);
            editTenantIntent.putExtra("room_id", room_id);
            editTenantIntent.putExtra("tenant_id", tenant_id);
            editTenantIntent.putExtra("tenant_name", tenant_name);
            editTenantIntent.putExtra("tenant_phone", tenant_phone);
            editTenantIntent.putExtra("tenant_address", tenant_address);
            startActivity(editTenantIntent);
            return true;
        } else if (id == R.id.action_remove_tenant) {
            tenantRemoveFromRoomConfirmation();
            return true;
        } else if (id == R.id.action_view_all_tenant) {
            showBottomSheetTenants();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}