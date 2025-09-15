package com.example.rentify_roomrentmanagement;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
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
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import java.text.NumberFormat;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class RoomDetailsActivity extends AppCompatActivity {

    private TextView tvRoomStatus, tvTenantName, tvTenantPhone, tvTenantStartDate, btnAddTenant, btnAddRent, btnAddElcBill;
    private TextView tvNoRentRecord, tvNoBillRecord;
    private CircleImageView cimgTenantProfilePic;
    private String room_id, tenant_id, tenant_name, thumb_tenant_url, tenant_phone, tenant_start_date = "N/A";
    private boolean is_occupied;
    private SpeedDialView addRecordSpeedDial;
    private RecyclerView rvRentList, rvElcBillList;

    private TabLayout tabLayout;
    private ViewPager2 viewPager2;
    private RentBillPagerAdapter rentBillPagerAdapter;
    private GradientDrawable gradientDrawable;
    private DatabaseReference databaseReference, roomReference, tenantReference, rentsReference, ebillsReference;
   // private FirebaseRecyclerAdapter<Rents, RoomDetailsActivity.RentsViewHolder> firebaseRecyclerAdapter;
    //private FirebaseRecyclerAdapter<Bills, RoomDetailsActivity.BillsViewHolder> firebaseBillsRecyclerAdapter;


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
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        room_id = getIntent().getStringExtra("room_id");
        is_occupied = getIntent().getBooleanExtra("is_occupied", false);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        roomReference = databaseReference.child("rooms").child(room_id);
        tenantReference = databaseReference.child("tenants");
        rentsReference = databaseReference.child("rents");
        ebillsReference = databaseReference.child("e-bills").child(room_id);

        // Rent Recycler View
        /*rvRentList = (RecyclerView) findViewById(R.id.rvRentRecord);
        rvRentList.setVerticalScrollBarEnabled(true);
        LinearLayoutManager layoutRentManager = new LinearLayoutManager(RoomDetailsActivity.this);
        layoutRentManager.setReverseLayout(true);    // newest items appear at top
        layoutRentManager.setStackFromEnd(false);    // optional, usually false
        rvRentList.setLayoutManager(layoutRentManager);
        rvRentList.setHasFixedSize(true);

        // e-bill Recycler View
        // rvElcBillList = (RecyclerView) findViewById(R.id.rvElcBillRecord);
        rvElcBillList.setVerticalScrollBarEnabled(true);
        LinearLayoutManager layoutBillManager = new LinearLayoutManager(RoomDetailsActivity.this);
        layoutBillManager.setReverseLayout(true);    // newest items appear at top
        layoutBillManager.setStackFromEnd(false);    // optional, usually false
        rvElcBillList.setLayoutManager(layoutBillManager);
        rvElcBillList.setHasFixedSize(true); */

        tvRoomStatus = findViewById(R.id.tvRoomStatus);
        tvTenantName = findViewById(R.id.tvTenantName);
        tvTenantPhone = findViewById(R.id.tvTenantPhone);
        tvTenantStartDate = findViewById(R.id.tvStartDate);
        // tvNoRentRecord = findViewById(R.id.tvNoRentRecord);
        //tvNoBillRecord = findViewById(R.id.tvNoBillRecord);
        cimgTenantProfilePic = findViewById(R.id.imgProfile);
        btnAddTenant = findViewById(R.id.btnAddTenant);
        //btnAddRent = findViewById(R.id.tvAddRent);
        //btnAddElcBill = findViewById(R.id.btnAddElcBill);
        addRecordSpeedDial = findViewById(R.id.fabAddRecord);

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

        // loadRentRecyclerList();
        //loadElcBillRecyclerList();

        btnAddTenant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addTenantIntent = new Intent(RoomDetailsActivity.this, AddTenantActivity.class);
                addTenantIntent.putExtra("room_id", room_id);
                startActivity(addTenantIntent);
            }
        });

        /* btnAddRent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addRentIntent = new Intent(RoomDetailsActivity.this, AddRentActivity.class);
                addRentIntent.putExtra("room_id", room_id);
                addRentIntent.putExtra("tenant_id", tenant_id);
                startActivity(addRentIntent);
            }
        });

        btnAddElcBill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addBillIntent = new Intent(RoomDetailsActivity.this, AddEbillActivity.class);
                addBillIntent.putExtra("room_id", room_id);
                addBillIntent.putExtra("tenant_id", tenant_id);
                startActivity(addBillIntent);
            }
        }); */

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
        tenantReference.child(tenant_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tenant_name = snapshot.child("tenant_name").getValue(String.class);
                tenant_phone = snapshot.child("tenant_phone").getValue(String.class);
                thumb_tenant_url = snapshot.child("thumb_tenant_url").getValue(String.class);
                tenant_start_date = snapshot.child("tenant_start_date").getValue(String.class);

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

    /*private void loadRentRecyclerList() {

        Query userRents = rentsReference.orderByChild("room_id").equalTo(room_id);
        FirebaseRecyclerOptions<Rents> options = new FirebaseRecyclerOptions.Builder<Rents>()
                .setQuery(userRents, Rents.class)
                .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Rents, RoomDetailsActivity.RentsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull RoomDetailsActivity.RentsViewHolder holder, int position, @NonNull Rents model) {
                // Bind your data here
                holder.setRentDate(model.getRent_date());
                holder.setRentAmount(model.getRent_amount());
                holder.setRentTime(model.getRent_time());
                holder.setRentPaymentMode(model.getPayment_mode());
                holder.setRentTenantName(model.getTenant_name());
                // etc.

                // Send pid to PropertyDetails Activity
                //String property_id = getRef(position).getKey();

                         holder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent propertyDetailIntent = new Intent(RoomDetailsActivity.this, PropertyDetailsActivity.class);
                                propertyDetailIntent.putExtra("property_id", property_id);
                                startActivity(propertyDetailIntent);
                            }
                        });
            }

            @NonNull
            @Override
            public RoomDetailsActivity.RentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.single_rent_layout, parent, false);
                return new RoomDetailsActivity.RentsViewHolder(view);
            }

            @Override
            public void onDataChanged() {
                super.onDataChanged();

                int itemCount = getItemCount();
                if (itemCount == 0) {
                    tvNoRentRecord.setVisibility(View.VISIBLE);
                } else {
                    tvNoRentRecord.setVisibility(View.GONE);
                }
                int maxVisibleItems = Math.min(itemCount, 3);

                if (itemCount > 0) {
                    RecyclerView.ViewHolder firstHolder = createViewHolder(rvRentList, 0);
                    firstHolder.itemView.measure(
                            View.MeasureSpec.makeMeasureSpec(rvRentList.getWidth(), View.MeasureSpec.EXACTLY),
                            View.MeasureSpec.UNSPECIFIED
                    );
                    int itemHeight = firstHolder.itemView.getMeasuredHeight();

                    ViewGroup.LayoutParams params = rvRentList.getLayoutParams();
                    params.height = itemHeight * maxVisibleItems;
                    rvRentList.setLayoutParams(params);
                }
            }

        };

        rvRentList.setAdapter(firebaseRecyclerAdapter);
    } */

    /* private void loadElcBillRecyclerList(){

        FirebaseRecyclerOptions<Bills> bill_options = new FirebaseRecyclerOptions.Builder<Bills>()
                .setQuery(ebillsReference, Bills.class)
                .build();
        //Toast.makeText(RoomDetailsActivity.this, "Check 01 " + bill_options, Toast.LENGTH_SHORT).show();

        firebaseBillsRecyclerAdapter = new FirebaseRecyclerAdapter<Bills, RoomDetailsActivity.BillsViewHolder>(bill_options) {
            @Override
            protected void onBindViewHolder(@NonNull RoomDetailsActivity.BillsViewHolder holder, int position, @NonNull Bills model) {
                // Bind your data here
                holder.setBillAmount(model.getEbill_amount());
                holder.setBillDate(model.getEbill_date());
                holder.setBillTime(model.getEbill_time());
                holder.setBillPaymentMode(model.getPayment_mode());
                holder.setBillUnitPaidTill(model.getPaid_upto());
                holder.setBillUnitUsed(model.getUnits_used());
                // etc.
            }

            @NonNull
            @Override
            public RoomDetailsActivity.BillsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.single_ebill_layout, parent, false);
                return new RoomDetailsActivity.BillsViewHolder(view);
            }

            @Override
            public void onDataChanged() {
                super.onDataChanged();

                int itemCount = getItemCount();
                if (itemCount == 0) {
                    tvNoBillRecord.setVisibility(View.VISIBLE);
                } else {
                    tvNoBillRecord.setVisibility(View.GONE);
                }
                int maxVisibleItems = Math.min(itemCount, 3);

                if (itemCount > 0) {
                    RecyclerView.ViewHolder firstHolder = createViewHolder(rvElcBillList, 0);
                    firstHolder.itemView.measure(
                            View.MeasureSpec.makeMeasureSpec(rvElcBillList.getWidth(), View.MeasureSpec.EXACTLY),
                            View.MeasureSpec.UNSPECIFIED
                    );
                    int itemHeight = firstHolder.itemView.getMeasuredHeight();

                    ViewGroup.LayoutParams params = rvElcBillList.getLayoutParams();
                    params.height = itemHeight * maxVisibleItems;
                    rvElcBillList.setLayoutParams(params);
                }
            }

        };

        rvElcBillList.setAdapter(firebaseBillsRecyclerAdapter);

    } */

    /* public class RentsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public RentsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setRentDate(String rentDate) {
            TextView rentDateView = mView.findViewById(R.id.tvRentDate);
            rentDateView.setText(rentDate);
        }

        public void setRentAmount(int rentAmount) {
            TextView rentAmountView = mView.findViewById(R.id.tvRentAmount);
            //String rent_amount = "₹" + String.valueOf(rentAmount);
            String displayRent = "+ ₹" + NumberFormat.getInstance(new Locale("en", "IN")).format(rentAmount);
            rentAmountView.setText(displayRent);
        }

        public void setRentTime(String rentTime) {
            TextView rentTimeView = mView.findViewById(R.id.tvRentTime);
            rentTimeView.setText(rentTime);
        }

        public void setRentPaymentMode(String rentPaymentMode) {
            TextView rentPaymentModeView = mView.findViewById(R.id.tvRentPaymentMode);
            rentPaymentModeView.setText(String.valueOf(rentPaymentMode));
        }

        public void setRentTenantName(String rentTenantName) {
            TextView rentTenantNameView = mView.findViewById(R.id.tvRentTenantName);
            String displayTenantName = "By: " + rentTenantName;
            rentTenantNameView.setText(displayTenantName);
        }

    } */

    /* public class BillsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public BillsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setBillDate(String billDate) {
            TextView billDateView = mView.findViewById(R.id.tvBillDate);
            billDateView.setText(billDate);
        }

        public void setBillTime(String billTime) {
            TextView billTimeView = mView.findViewById(R.id.tvBillTime);
            billTimeView.setText(billTime);
        }

        public  void setBillUnitPaidTill(int billUnitPaidTill){
            TextView billUnitPaidTillView = mView.findViewById(R.id.tvBillPaidTill);
            billUnitPaidTillView.setText(String.valueOf(billUnitPaidTill));
        }

        public  void setBillUnitUsed(int billUnitUsed){
            TextView billUnitUsedView = mView.findViewById(R.id.tvBillUsedUnit);
            String displayUnitUsed = "+" + billUnitUsed;
            billUnitUsedView.setText(displayUnitUsed);
        }

        public void setBillAmount(int billAmount) {
            TextView billAmountView = mView.findViewById(R.id.tvBillAmount);
            String displayBill = "₹" + NumberFormat.getInstance(new Locale("en", "IN")).format(billAmount);
            billAmountView.setText(displayBill);
        }

        public void setBillPaymentMode(String billPaymentMode) {
            TextView billPaymentModeView = mView.findViewById(R.id.tvBillPaymentMode);
            billPaymentModeView.setText(String.valueOf(billPaymentMode));
        }

    } */

    @Override
    public void onStart() {
        super.onStart();

        gradientDrawable = (GradientDrawable) tvRoomStatus.getBackground();
        if (!is_occupied) {
            //btnAddRent.setVisibility(View.GONE);
            //btnAddElcBill.setVisibility(View.GONE);
            btnAddTenant.setVisibility(View.VISIBLE);
            addRecordSpeedDial.setVisibility(View.GONE);

            //tvNoRentRecord.setVisibility(View.VISIBLE);
            //tvNoBillRecord.setVisibility(View.VISIBLE);
            tvRoomStatus.setText("Vacant");
            gradientDrawable.setColor(Color.parseColor("#C0F6695E"));
        } else {
            /* if (firebaseRecyclerAdapter != null) {
                firebaseRecyclerAdapter.startListening();
            }
            if (firebaseBillsRecyclerAdapter != null) {
                firebaseBillsRecyclerAdapter.startListening();
            } */
            //btnAddRent.setVisibility(View.VISIBLE);
            //btnAddElcBill.setVisibility(View.VISIBLE);
            addRecordSpeedDial.setVisibility(View.VISIBLE);
            btnAddTenant.setVisibility(View.GONE);
            tvRoomStatus.setText("Occupied");
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

    @Override
    protected void onStop() {
        super.onStop();
        /* if (firebaseRecyclerAdapter != null) {
            firebaseRecyclerAdapter.stopListening();
        }
        if (firebaseBillsRecyclerAdapter != null) {
            firebaseBillsRecyclerAdapter.stopListening();
        } */

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
        getMenuInflater().inflate(R.menu.room_details_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_edit_property) {

            return true;
        } else if (id == R.id.action_delete_property) {
            // Confirm and delete property
            //propertyDeleteConfirmation();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}