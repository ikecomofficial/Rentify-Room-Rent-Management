package com.example.rentify_roomrentmanagement;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
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

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class AddEbillActivity extends AppCompatActivity {

    private EditText etCurrentReading, etElcBillAmount, etElcLastPaidTill;
    private TextView btnSaveElcBill;
    private LinearLayout layoutLastPaidUnit, layoutElcBillAmount, layoutElcUnitPaid;
    private MaterialButtonToggleGroup tgPaymentMode, tgElcBillMode;
    private String room_id, tenant_id, ebill_id, paymentMode = "Cash", elcBillAmount, unitPaidUpTo;
    private String ebill_date, ebill_time;
    private Integer last_paid_upto = -1, elc_unit_rate = 0, units_used = 0, elc_bill_amount = 0, units_paid_upto = 0;
    private boolean isByUnits = true, isFirstRecord = true;
    private SwitchMaterial smCustomDateTime;
    private DatabaseReference databaseReference, roomReference, tenantReference, elcBillReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_ebill);
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
        tenantReference = databaseReference.child("tenants").child(tenant_id);
        elcBillReference = databaseReference.child("e-bills").child(room_id);

        etCurrentReading = findViewById(R.id.etCurrentMeterReading);
        etElcBillAmount = findViewById(R.id.etElcBillAmount);
        etElcLastPaidTill = findViewById(R.id.etElcLastPaidUnit);
        layoutElcUnitPaid = findViewById(R.id.layoutElcUnitPaid);
        layoutElcBillAmount = findViewById(R.id.layoutElcBillAmount);
        layoutLastPaidUnit = findViewById(R.id.layoutLastPaidTill);
        btnSaveElcBill = findViewById(R.id.btnSaveElcBill);
        tgElcBillMode = findViewById(R.id.toggleElcBillMode);
        tgPaymentMode = findViewById(R.id.togglePaymentMode);
        smCustomDateTime = findViewById(R.id.switchCustomDateTime);

        if (fetchLastPaidUnitFirebase()){
            fetchElcUnitRate();
        }

        tgElcBillMode.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if (isChecked){
                    if (checkedId == R.id.btnByUnitsSelection){
                        layoutElcUnitPaid.setVisibility(View.VISIBLE);
                        layoutElcBillAmount.setVisibility(View.GONE);
                        isByUnits = true;
                    } else if (checkedId == R.id.btnByAmountSelection) {
                        layoutElcUnitPaid.setVisibility(View.GONE);
                        layoutElcBillAmount.setVisibility(View.VISIBLE);
                        isByUnits = false;
                    }
                }
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
        smCustomDateTime.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                showDateTimePicker();
            } else {
                setCurrentDateTime();
            }
        });

        btnSaveElcBill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveElcBillToFirebase();
            }
        });

    }

    private boolean fetchLastPaidUnitFirebase() {
        elcBillReference.limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot billSnapshot : snapshot.getChildren()) {
                        String bill_id = billSnapshot.getKey();
                        assert bill_id != null;
                        Long lastPaidUnit = billSnapshot.child("paid_upto").getValue(Long.class);
                        if (lastPaidUnit != null) {
                            last_paid_upto = lastPaidUnit.intValue();
                            isFirstRecord = false;
                            layoutLastPaidUnit.setVisibility(View.GONE);
                        }
                    }
                } else {
                    layoutLastPaidUnit.setVisibility(View.VISIBLE);
                    isFirstRecord = true;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return true;
    }
    private void fetchElcUnitRate(){
        roomReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long elcUnitRate = snapshot.child("elc_unit_rate").getValue(Long.class);
                elc_unit_rate = elcUnitRate.intValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void setCurrentDateTime() {
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

        ebill_date = dateFormat.format(calendar.getTime());
        ebill_time = timeFormat.format(calendar.getTime());
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
                    ebill_date = dateFormat.format(pickedDate.getTime());

                    // After date, show time picker
                    TimePickerDialog timePicker = new TimePickerDialog(this,
                            (timeView, hourOfDay, minute) -> {
                                Calendar pickedTime = Calendar.getInstance();
                                pickedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                pickedTime.set(Calendar.MINUTE, minute);

                                SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                                ebill_time = timeFormat.format(pickedTime.getTime());

                            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);

                    timePicker.show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        datePicker.show();
    }

    private void saveElcBillToFirebase(){

        // Step 1: Get previous units
        if (isFirstRecord) {
            String prevUnitPaid = etElcLastPaidTill.getText().toString().trim();
            if (prevUnitPaid.isEmpty()) {
                etElcLastPaidTill.setError("Enter Previous Units");
                return;
            }
            last_paid_upto = Integer.parseInt(prevUnitPaid);
        }
        if (isByUnits){
            // Unit Mode
            unitPaidUpTo = etCurrentReading.getText().toString().trim();
            if (unitPaidUpTo.isEmpty()){
                etCurrentReading.setError("Enter Current Meter Reading");
                return;
            }
            units_paid_upto = Integer.parseInt(unitPaidUpTo);
            units_used = units_paid_upto - last_paid_upto;
            elc_bill_amount = units_used * elc_unit_rate;
        }else {
            elcBillAmount = etElcBillAmount.getText().toString().trim();
            if (elcBillAmount.isEmpty()){
                etElcBillAmount.setError("Enter Electricity Bill Paid Amount");
                return;
            }
            elc_bill_amount = Integer.parseInt(elcBillAmount);
            units_used = elc_bill_amount / elc_unit_rate;
            units_paid_upto = last_paid_upto + units_used;
        }
        // Create unique rent ID
        ebill_id = elcBillReference.push().getKey();
        HashMap<String, Object> billMap = new HashMap<>();
        billMap.put("room_id", room_id);
        billMap.put("tenant_id", tenant_id);
        billMap.put("payment_mode", paymentMode);
        billMap.put("ebill_date", ebill_date);
        billMap.put("ebill_time", ebill_time);
        billMap.put("paid_upto", units_paid_upto);
        billMap.put("units_used", units_used);
        billMap.put("ebill_amount", elc_bill_amount);
        billMap.put("last_paid_upto", last_paid_upto);

        if (ebill_id != null){
            elcBillReference.child(ebill_id).setValue(billMap)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Bill Added Successfully", Toast.LENGTH_SHORT).show();
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