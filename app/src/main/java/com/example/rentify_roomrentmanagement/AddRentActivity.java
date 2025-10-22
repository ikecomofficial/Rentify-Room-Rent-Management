package com.example.rentify_roomrentmanagement;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class AddRentActivity extends AppCompatActivity {
    private EditText etRentAmount;
    private TextView tvCustomDate, tvCustomTime, tvCurrDateTimeHint;
    private MaterialButtonToggleGroup tgPaymentMode;
    private Calendar calendar;
    private String room_id, tenant_id, tenant_name, currTimeStamp, rent_timestamp, rent_id, paymentMode = "Cash";
    private String rent_date, rent_time;
    private Integer room_rent = 0;
    private RadioGroup rgCurrentCustomDT;
    private LinearLayout layoutDateTimePicker, layoutDatePicker, layoutTimePicker;
    private DatabaseReference databaseReference, roomReference, tenantReference, rentReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_rent);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Add Rent Record");
        }

        room_id = getIntent().getStringExtra("room_id");
        tenant_id = getIntent().getStringExtra("tenant_id");

        databaseReference = FirebaseDatabase.getInstance().getReference();
        roomReference = databaseReference.child("rooms").child(room_id);
        tenantReference = databaseReference.child("tenants").child(room_id).child(tenant_id);
        rentReference = databaseReference.child("rents").child(room_id);

        etRentAmount = findViewById(R.id.etRentAmount);
        tgPaymentMode = findViewById(R.id.togglePaymentMode);

        rgCurrentCustomDT = findViewById(R.id.rgCurrentCustomDT);

        tvCustomDate = findViewById(R.id.tvCustomDate);
        tvCustomTime = findViewById(R.id.tvCustomTime);

        layoutDateTimePicker = findViewById(R.id.layoutDateTimePicker);
        layoutDatePicker = findViewById(R.id.layoutDatePicker);
        layoutTimePicker = findViewById(R.id.layoutTimePicker);
        layoutDateTimePicker.setVisibility(View.GONE);
        tvCurrDateTimeHint = findViewById(R.id.tvCurrDateTimeHint);


        MaterialButton btnSaveRent = findViewById(R.id.btnSaveRent);

        fetchRentTenantFirebase();

        calendar = Calendar.getInstance();

        rgCurrentCustomDT.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbCurrentDateTime) {
                // Current selected
                layoutDateTimePicker.setVisibility(View.GONE);
                setCurrentDateTime();
            } else if (checkedId == R.id.rbCustomDateTime) {
                // Custom selected
                layoutDateTimePicker.setVisibility(View.VISIBLE);
                displayCurrDateTime();
                tvCurrDateTimeHint.setText("Custom Date & Time Applied.");
            }
        });



        tgPaymentMode.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if (isChecked){
                    if (checkedId == R.id.btnCashSelection){
                        paymentMode = "Cash";
                    } else if (checkedId == R.id.btnOnlineSelection) {
                        paymentMode = "Online";
                    }
                }
            }
        });

        setCurrentDateTime();

        // 1️⃣ Click on Date Layout (Date + Time)
        layoutDatePicker.setOnClickListener(v -> {
            showDateTimePicker();
        });

        // 2️⃣ Click on Time Layout (Time Only)
        layoutTimePicker.setOnClickListener(v -> {
            showTimePicker();
        });

        btnSaveRent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveRentToFirebase();
            }
        });



    }
    private void fetchRentTenantFirebase(){
        roomReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long rentValue = snapshot.child("room_rent").getValue(Long.class);
                room_rent = rentValue.intValue();
                etRentAmount.setText(String.valueOf(room_rent));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        tenantReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String tenantName = snapshot.child("tenant_name").getValue(String.class);
                tenant_name = tenantName;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Helper method to update TextViews
    private void updateDateTimeViews(Calendar cal) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

        tvCustomDate.setText(dateFormat.format(cal.getTime()));
        tvCustomTime.setText(timeFormat.format(cal.getTime()));
    }

    // Helper method to get timestamp
    private String getTimestamp(Calendar cal) {
        return String.valueOf(cal.getTimeInMillis());
    }



    // Get current date & time
    private void setCurrentDateTime() {

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

        rent_date = dateFormat.format(calendar.getTime());
        rent_time = timeFormat.format(calendar.getTime());

        String currDateTime = "Date & Time Applied: " + rent_date + ", " + rent_time;
        tvCurrDateTimeHint.setText(currDateTime);

        rent_timestamp = String.valueOf(System.currentTimeMillis());
    }
    private void displayCurrDateTime(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

        rent_date = dateFormat.format(calendar.getTime());
        rent_time = timeFormat.format(calendar.getTime());

        tvCustomDate.setText(rent_date);
        tvCustomTime.setText(rent_time);

    }

    // Show picker dialogs
    private void showDateTimePicker() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                AddRentActivity.this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    calendar.set(Calendar.YEAR, selectedYear);
                    calendar.set(Calendar.MONTH, selectedMonth);
                    calendar.set(Calendar.DAY_OF_MONTH, selectedDay);

                    // After date, show time picker
                    int hour = calendar.get(Calendar.HOUR_OF_DAY);
                    int minute = calendar.get(Calendar.MINUTE);

                    TimePickerDialog timePickerDialog = new TimePickerDialog(
                            AddRentActivity.this,
                            (timeView, selectedHour, selectedMinute) -> {
                                calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                                calendar.set(Calendar.MINUTE, selectedMinute);

                                // Update TextViews
                                updateDateTimeViews(calendar);

                                // Get final timestamp
                                rent_timestamp = getTimestamp(calendar);

                            }, hour, minute, false // false for 12-hour format
                    );
                    timePickerDialog.show();

                }, year, month, day
        );
        datePickerDialog.show();
    }

    private void showTimePicker(){
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                AddRentActivity.this,
                (timeView, selectedHour, selectedMinute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                    calendar.set(Calendar.MINUTE, selectedMinute);

                    // Update only Time TextView
                    SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                    tvCustomTime.setText(timeFormat.format(calendar.getTime()));

                    // Update timestamp
                    rent_timestamp = getTimestamp(calendar);

                }, hour, minute, false
        );
        timePickerDialog.show();
    }

    private void saveRentToFirebase(){
        String rentAmount = etRentAmount.getText().toString().trim();

        currTimeStamp = String.valueOf(System.currentTimeMillis());

        if (rentAmount.isEmpty()) {
            etRentAmount.setError("Enter Rent Amount Paid");
            return;
        }
        // Create unique rent ID
        rent_id = rentReference.push().getKey();
        HashMap<String, Object> rentMap = new HashMap<>();
        rentMap.put("tenant_name", tenant_name);
        rentMap.put("rent_amount", Integer.parseInt(rentAmount));
        rentMap.put("room_id", room_id);
        rentMap.put("tenant_id", tenant_id);
        rentMap.put("payment_mode", paymentMode);
        //rentMap.put("rent_date", rent_date);
        //rentMap.put("rent_time", rent_time);
        rentMap.put("rent_timestamp", rent_timestamp);

        if (rent_id != null){
            rentReference.child(rent_id).setValue(rentMap)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Rent Added Successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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