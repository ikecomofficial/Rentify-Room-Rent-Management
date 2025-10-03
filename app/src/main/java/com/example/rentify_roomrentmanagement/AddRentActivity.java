package com.example.rentify_roomrentmanagement;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
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
    private TextView btnSaveRent;
    private MaterialButton btnDatePicker, btnTimePicker;
    private MaterialButtonToggleGroup tgPaymentMode;
    private String room_id, tenant_id, tenant_name, currTimeStamp, rent_id, paymentMode = "Cash";
    private String rent_date, rent_time;
    private Integer room_rent = 0;
    private SwitchMaterial smCustomDateTime;
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
        }

        room_id = getIntent().getStringExtra("room_id");
        tenant_id = getIntent().getStringExtra("tenant_id");

        databaseReference = FirebaseDatabase.getInstance().getReference();
        roomReference = databaseReference.child("rooms").child(room_id);
        tenantReference = databaseReference.child("tenants").child(room_id).child(tenant_id);
        rentReference = databaseReference.child("rents").child(room_id);

        etRentAmount = findViewById(R.id.etRentAmount);
        tgPaymentMode = findViewById(R.id.togglePaymentMode);
        smCustomDateTime = findViewById(R.id.switchCustomDateTime);
        btnDatePicker = findViewById(R.id.btnCustomDateSelector);
        btnTimePicker = findViewById(R.id.btnCustomTimeSelector);
        btnSaveRent = findViewById(R.id.btnSaveRent);

        fetchRentTenantFirebase();

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
        smCustomDateTime.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                //layoutDateTimePicker.setVisibility(View.VISIBLE);
                showDateTimePicker();
            } else {
                //layoutDateTimePicker.setVisibility(View.GONE);
                setCurrentDateTime();
            }
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

    // Get current date & time
    private void setCurrentDateTime() {
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

        rent_date = dateFormat.format(calendar.getTime());
        rent_time = timeFormat.format(calendar.getTime());
    }

    // Show picker dialogs
    private void showDateTimePicker() {
        Calendar calendar = Calendar.getInstance();

        // Date Picker
        DatePickerDialog datePicker = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    Calendar pickedDate = Calendar.getInstance();
                    pickedDate.set(year, month, dayOfMonth);

                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                    rent_date = dateFormat.format(pickedDate.getTime());

                    // After date, show time picker
                    TimePickerDialog timePicker = new TimePickerDialog(this,
                            (timeView, hourOfDay, minute) -> {
                                Calendar pickedTime = Calendar.getInstance();
                                pickedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                pickedTime.set(Calendar.MINUTE, minute);

                                SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                                rent_time = timeFormat.format(pickedTime.getTime());

                            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);

                    timePicker.show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        datePicker.show();
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
        rentMap.put("rent_date", rent_date);
        rentMap.put("rent_time", rent_time);

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