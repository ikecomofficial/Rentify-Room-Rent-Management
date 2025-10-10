package com.example.rentify_roomrentmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private RecyclerView propertyList;
    private int occupiedSum, totalSum, propertiesItemCount, monthlyAmount = 0;
    private String curr_my, user_id;
    private long backPressedTime = 0;
    private boolean amount_visible = false;
    private ImageView imgViewEye;
    private TextView tvNoPropertyList, tvUserName, tvProgressLabel, tvPropertiesCount, tvViewAmount, tvCurrentMY;
    private ProgressBar progressBarProperties, occupancyProgressBar;
    private DatabaseReference monthlyRecordsDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        assert user != null;
        user_id = user.getUid();

        MaterialButton btnAddProperty = findViewById(R.id.btnAddProperty);
        progressBarProperties = findViewById(R.id.progressBarProperties);
        tvNoPropertyList = findViewById(R.id.tvNoPropertyList);
        tvUserName = findViewById(R.id.tvUserName);
        tvProgressLabel = findViewById(R.id.tvProgressLabel);

        // View Monthly Collection

        tvViewAmount = findViewById(R.id.tvMonthlyAmount);
        imgViewEye = findViewById(R.id.imgViewEye);
        // initially eye closed
        imgViewEye.setImageResource(R.drawable.ic_eye_close);
        tvViewAmount.setText("View Amount");

        tvCurrentMY = findViewById(R.id.tvCurrentMY);

        tvPropertiesCount = findViewById(R.id.tvPropertiesCount);
        occupancyProgressBar = findViewById(R.id.occupancyProgressBar);
        CircleImageView cimgUserAccount = findViewById(R.id.cimgUserAccount);
        propertyList = findViewById(R.id.propertyListRecycleView);
        propertyList.setHasFixedSize(true);
        propertyList.setLayoutManager(new LinearLayoutManager(MainActivity.this));

        // Get Current Month Year (Oct 2025)
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM yyyy", Locale.getDefault());
        curr_my = dateFormat.format(date);
        tvCurrentMY.setText(curr_my);

        //

        // Monthly Rent & Bills Data Reference
        monthlyRecordsDatabase = FirebaseDatabase.getInstance().getReference().child("monthly_records").child(curr_my).child(user_id);

        // Check if the user is already signed in. If not, redirect to LoginScreen.
        if (user == null) {
            goToLoginScreen();
        } else {

            String providerId = user.getProviderId();
            if (providerId.equals("google.com")){
                // Set UserName on top
                tvUserName.setText(user.getDisplayName());
                // Configure Google Sign In to get the client for sign-out
                /*

                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();
                GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

                 */
            }else {
                DatabaseReference userReference = FirebaseDatabase.getInstance().getReference().child("users").child(user_id);
                userReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String user_name = snapshot.child("name").getValue(String.class);
                        tvUserName.setText(user_name);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }


        cimgUserAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AccountSetting.class);
                startActivity(intent);
            }
        });


        btnAddProperty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddProperty.class);
                startActivity(intent);
            }
        });

        imgViewEye.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!amount_visible){
                    if (monthlyAmount == 0){
                        fetchUserMonthlyCollection();
                    }else {
                        showMonthlyAmount();
                    }
                }else {
                    // hide amount
                    tvViewAmount.setText("View Amount");
                    imgViewEye.setImageResource(R.drawable.ic_eye_close);
                    amount_visible = false;
                }


            }
        });

    }
    private void goToLoginScreen() {
        Intent intent = new Intent(MainActivity.this, LoginScreen.class);
        startActivity(intent);
        finish(); // End MainActivity so the user can't press back to it
    }

    @Override
    public void onStart(){
        super.onStart();
        loadPropertiesFromFirebase();
    }

    private void loadPropertiesFromFirebase(){
        DatabaseReference propertiesReference = FirebaseDatabase.getInstance().getReference().child("properties");

        Query userProperties = propertiesReference.orderByChild("user_id").equalTo(user_id);
        FirebaseRecyclerOptions<Properties> options = new FirebaseRecyclerOptions.Builder<Properties>()
                .setQuery(userProperties, Properties.class)
                .build();

        FirebaseRecyclerAdapter<Properties, PropertiesViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Properties, PropertiesViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull PropertiesViewHolder holder, int position, @NonNull Properties model) {


                        // Fetch occupancy data
                        int occupied_rooms = model.getRooms_occupied();
                        int occupied_shops = model.getShops_occupied();
                        int total_rooms = model.getTotal_rooms();
                        int total_shops = model.getTotal_shops();

                        // Property Sums
                        int propertyOccSum = occupied_rooms + occupied_shops;
                        int propertyTotalSum = total_rooms + total_shops;

                        // Get Total and Occupied for All Properties Combined
                        occupiedSum += propertyOccSum;
                        totalSum += propertyTotalSum;

                        setProgressBarData(occupiedSum, totalSum);



                        // Bind your data here
                        holder.setPropertyName(model.getProperty_name());
                        holder.setPropertyAddress(model.getProperty_address());
                        holder.setOccupiedRooms(model.getRooms_occupied());
                        holder.setTotalRooms(model.getTotal_rooms());
                        holder.setOccupiedShops(model.getShops_occupied());
                        holder.setTotalShops(model.getTotal_shops());
                        // etc.

                        // Send pid to PropertyDetails Activity
                        String property_id = getRef(position).getKey();

                        holder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent propertyDetailIntent = new Intent(MainActivity.this, PropertyDetailsActivity.class);
                                propertyDetailIntent.putExtra("property_id", property_id);
                                startActivity(propertyDetailIntent);
                            }
                        });
                    }


                    @NonNull
                    @Override
                    public PropertiesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.single_property_layout, parent, false);
                        return new PropertiesViewHolder(view);
                    }

                    @Override
                    public void onDataChanged(){
                        super.onDataChanged();
                        progressBarProperties.setVisibility(View.GONE);
                        propertyList.setVisibility(View.VISIBLE);
                        propertiesItemCount = getItemCount();
                        if (propertiesItemCount == 0) {
                            tvNoPropertyList.setVisibility(View.VISIBLE);
                        } else {
                            tvNoPropertyList.setVisibility(View.GONE);
                            String p_count = "Properties" + " (" + propertiesItemCount + ")";
                            tvPropertiesCount.setText(p_count);
                        }
                    }
                };

        firebaseRecyclerAdapter.startListening();
        propertyList.setAdapter(firebaseRecyclerAdapter);

    }

    public void setProgressBarData(int occupied, int total) {
        if (total > 0) {
            int bar_percentage = (int) ((occupied * 100) / total);

            occupancyProgressBar.setMax(100); // make sure max is 100
            occupancyProgressBar.setProgress(bar_percentage);

            // Update label
            String progressLabelText = bar_percentage + "%";
            tvProgressLabel.setText(progressLabelText);
        } else {
            occupancyProgressBar.setProgress(0);
            tvProgressLabel.setText("No Data");
        }
    }

    private void fetchUserMonthlyCollection(){
        monthlyRecordsDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int total_rent = snapshot.child("total_rent").getValue(Integer.class);
                    int total_ebill = snapshot.child("total_ebill").getValue(Integer.class);
                    monthlyAmount = total_rent + total_ebill;
                    String total_formatted = "₹" + NumberFormat.getNumberInstance().format(monthlyAmount);;
                    tvViewAmount.setText(total_formatted);
                } else {
                    tvViewAmount.setText("N/A");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        amount_visible = true;
        imgViewEye.setImageResource(R.drawable.ic_eye_open);

    }

    private void showMonthlyAmount(){
        String total_amount = "₹" + NumberFormat.getNumberInstance().format(monthlyAmount);
        tvViewAmount.setText(total_amount);
        imgViewEye.setImageResource(R.drawable.ic_eye_open);
        amount_visible = true;
    }



    public static class PropertiesViewHolder extends RecyclerView.ViewHolder{

        View mView;
        public PropertiesViewHolder(View itemView){
            super(itemView);
            mView = itemView;
        }

        public void setPropertyName(String propertyName){
            TextView propertyNameView = mView.findViewById(R.id.tvPropertyName);
            propertyNameView.setText(propertyName);
        }

        public void setPropertyAddress(String propertyAddress){
            TextView propertyNameView = mView.findViewById(R.id.tvPropertyAddress);
            propertyNameView.setText(propertyAddress);
        }

        public void setOccupiedRooms(int occupiedRooms){
            TextView propertyNameView = mView.findViewById(R.id.tvOccupiedRooms);
            propertyNameView.setText(String.valueOf(occupiedRooms));
        }

        public void setTotalRooms(int totalRooms){
            TextView propertyNameView = mView.findViewById(R.id.tvTotalRooms);
            propertyNameView.setText(String.valueOf(totalRooms));
        }

        public void setOccupiedShops(int occupiedShops){
            TextView propertyNameView = mView.findViewById(R.id.tvOccupiedShops);
            propertyNameView.setText(String.valueOf(occupiedShops));
        }
        public void setTotalShops(int totalShops){
            TextView propertyNameView = mView.findViewById(R.id.tvTotalShops);
            propertyNameView.setText(String.valueOf(totalShops));
        }
    }

    @Override
    public void onBackPressed(){
        if (SystemClock.elapsedRealtime() - backPressedTime < 2000){
            super.onBackPressed();
            return;
        }else {
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
        }
        backPressedTime = SystemClock.elapsedRealtime();
    }

}