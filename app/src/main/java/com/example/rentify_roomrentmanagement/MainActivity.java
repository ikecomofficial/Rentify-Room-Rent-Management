package com.example.rentify_roomrentmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

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
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private RecyclerView propertyList;
    private DatabaseReference propertiesReference;

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

        propertyList = (RecyclerView) findViewById(R.id.propertyListRecycleView);
        propertyList.setHasFixedSize(true);
        propertyList.setLayoutManager(new LinearLayoutManager(MainActivity.this));

        Button btnAddProperty = (Button) findViewById(R.id.btnAddProperty);
        btnAddProperty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddProperty.class);
                startActivity(intent);
            }
        });

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        // Check if the user is already signed in. If not, redirect to LoginScreen.
        if (user == null) {
            goToLoginScreen();
        } else {
            String providerId = user.getProviderId();
            if (providerId.equals("google.com")){
                // Configure Google Sign In to get the client for sign-out
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();
                mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
            }
        }



    }
    private void goToLoginScreen() {
        Intent intent = new Intent(MainActivity.this, LoginScreen.class);
        startActivity(intent);
        finish(); // End MainActivity so the user can't press back to it
    }

    @Override
    public void onStart(){
        super.onStart();

        propertiesReference = FirebaseDatabase.getInstance().getReference().child("properties");
        String userID = FirebaseAuth.getInstance().getUid();

        Query userProperties = propertiesReference.orderByChild("user_id").equalTo(userID);
        FirebaseRecyclerOptions<Properties> options = new FirebaseRecyclerOptions.Builder<Properties>()
                .setQuery(userProperties, Properties.class)
                .build();

        FirebaseRecyclerAdapter<Properties, PropertiesViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Properties, PropertiesViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull PropertiesViewHolder holder, int position, @NonNull Properties model) {
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
                };

        firebaseRecyclerAdapter.startListening();
        propertyList.setAdapter(firebaseRecyclerAdapter);

    }

    public class PropertiesViewHolder extends RecyclerView.ViewHolder{

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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_account_setting) {

            Intent intent = new Intent(MainActivity.this, AccountSetting.class);
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}