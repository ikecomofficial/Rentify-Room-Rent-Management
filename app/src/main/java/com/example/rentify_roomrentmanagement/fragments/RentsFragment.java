package com.example.rentify_roomrentmanagement.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rentify_roomrentmanagement.R;
import com.example.rentify_roomrentmanagement.Rents;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RentsFragment extends Fragment {

    private TextView tvNoRentRecord;
    private RecyclerView rvRentList;
    private String room_id;
    private DatabaseReference rentsReference;
    private FirebaseRecyclerAdapter<Rents, RentsFragment.RentsViewHolder> firebaseRecyclerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_rents, container, false);

        // Get roomId from arguments
        room_id = getArguments() != null ? getArguments().getString("room_id") : null;

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        rentsReference = databaseReference.child("rents").child(room_id);

        // Rent Recycler View
        rvRentList = view.findViewById(R.id.rvRentRecord);
        LinearLayoutManager layoutRentManager = new LinearLayoutManager(getContext());
        layoutRentManager.setReverseLayout(true);    // newest items appear at top
        layoutRentManager.setStackFromEnd(true);    // optional, usually false
        rvRentList.setLayoutManager(layoutRentManager);

        tvNoRentRecord = view.findViewById(R.id.tvNoRentRecord);

        loadRentRecyclerList();

        if (firebaseRecyclerAdapter != null) {
            rvRentList.setAdapter(firebaseRecyclerAdapter);
            firebaseRecyclerAdapter.startListening();
        }

        return view;  // Return the view **after** initializing RecyclerView
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (firebaseRecyclerAdapter != null) {
            firebaseRecyclerAdapter.stopListening(); // stop only when view is destroyed
        }
    }

    private void loadRentRecyclerList() {

        FirebaseRecyclerOptions<Rents> rent_options = new FirebaseRecyclerOptions.Builder<Rents>()
                .setQuery(rentsReference, Rents.class)
                .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Rents, RentsFragment.RentsViewHolder>(rent_options) {
            @Override
            protected void onBindViewHolder(@NonNull RentsFragment.RentsViewHolder holder, int position, @NonNull Rents model) {
                // Bind your data here

                holder.setRentAmount(model.getRent_amount());
                holder.setRentPaymentMode(model.getPayment_mode());
                holder.setRentTenantName(model.getTenant_name());

                // Format timestamp into date & time
                long timestamp = Long.parseLong(model.getRent_timestamp());
                Date date = new Date(timestamp);

                String dateOnly = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date);
                String timeOnly = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date);

                holder.setRentDateTime(dateOnly, timeOnly);

                /* String rentDate = model.getRent_date();
                holder.setRentDate(rentDate);
                holder.setRentAmount(model.getRent_amount());
                holder.setRentTime(model.getRent_time());
                holder.setRentPaymentMode(model.getPayment_mode());
                holder.setRentTenantName(model.getTenant_name());

                // Extract current month-year
                String[] parts = rentDate.split(" ");
                String currMonthYear = "";

                if (parts.length >= 3) {
                    currMonthYear = parts[1] + " " + parts[2]; // AUG 2025
                }

                boolean showHeader = false;

                if (position == getItemCount() - 1) {
                    // Always show header for the visually topmost item (last in adapter because reversed)
                    showHeader = true;
                } else {
                    // Compare with the item after this one (visually above in reversed list)
                    Rents nextModel = getItem(position + 1);
                    String[] nextParts = nextModel.getRent_date().split(" ");
                    String nextMonthYear = "";
                    if (nextParts.length >= 3) {
                        nextMonthYear = nextParts[1] + " " + nextParts[2];
                    }

                    // Show header if month-year changed compared to above item
                    if (!currMonthYear.equals(nextMonthYear)) {
                        showHeader = true;
                    }
                }

                String fullMonthYear = "";
                try {
                    Date date = new SimpleDateFormat("MMM yyyy", Locale.ENGLISH).parse(currMonthYear);
                    assert date != null;
                    fullMonthYear = new SimpleDateFormat("MMMM yyyy", Locale.ENGLISH).format(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                holder.setMonthHeader(fullMonthYear, showHeader);


                 */
                // Long press to delete
                holder.itemView.setOnLongClickListener(v -> {
                    new MaterialAlertDialogBuilder(v.getContext())
                            .setTitle("Delete Rent Record?")
                            .setMessage("Are you sure you want to delete this rent record?")
                            .setPositiveButton("Delete", (dialog, which) -> {
                                // 🔑 Call your delete function here
                                deleteRentRecord(getRef(position).getKey());
                            })
                            .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                            .show();

                    return true; // ✅ consume the long press
                });

            }

            @NonNull
            @Override
            public RentsFragment.RentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.single_rent_layout, parent, false);
                return new RentsViewHolder(view);
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChanged() {
                super.onDataChanged();
                rvRentList.post(() -> firebaseRecyclerAdapter.notifyDataSetChanged());

                int itemCount = getItemCount();
                if (itemCount == 0) {
                    tvNoRentRecord.setVisibility(View.VISIBLE);
                } else {
                    tvNoRentRecord.setVisibility(View.GONE);
                }
            }

        };

        rvRentList.setAdapter(firebaseRecyclerAdapter);
    }

    private String getMonthYear(String timestampStr) {
        try {
            long timestamp = Long.parseLong(timestampStr); // convert string → long
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return ""; // fallback if string is invalid
        }
    }

    private void deleteRentRecord(String rent_id) {
        rentsReference.child(rent_id).removeValue()
                .addOnSuccessListener(aVoid -> {
                    // ✅ Record deleted successfully
                    Toast.makeText(getContext(), "Rent Deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // ❌ Handle failure
                    Toast.makeText(getContext(), "Rent Not Deleted", Toast.LENGTH_SHORT).show();
                });
    }


    public static class RentsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public RentsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

        }


        public void setRentDateTime(String rentDate, String rentTime){
            TextView rentDateView = mView.findViewById(R.id.tvRentDateTime);
            String finalDateTime = rentDate + ", " + rentTime;
            rentDateView.setText(finalDateTime);

        }

        /*
        public void setMonthHeader(String monthYear, boolean showHeader) {
            TextView tvMonthYearHeader = mView.findViewById(R.id.tvMonthYear);
            LinearLayout layoutMonthHeader = mView.findViewById(R.id.monthYearHeader);

            if (showHeader) {
                layoutMonthHeader.setVisibility(View.VISIBLE);
                tvMonthYearHeader.setText(monthYear);
            } else {
                layoutMonthHeader.setVisibility(View.GONE);
            }
        }

         */

        public void setRentAmount(int rentAmount) {
            TextView rentAmountView = mView.findViewById(R.id.tvRentAmount);
            //String rent_amount = "₹" + String.valueOf(rentAmount);
            String displayRent = "+ ₹" + NumberFormat.getInstance(new Locale("en", "IN")).format(rentAmount);
            rentAmountView.setText(displayRent);
        }

        public void setRentPaymentMode(String rentPaymentMode) {
            TextView rentPaymentModeView = mView.findViewById(R.id.tvRentPaymentMode);
            rentPaymentModeView.setText(String.valueOf(rentPaymentMode));
        }

        public void setRentTenantName(String rentTenantName) {
            TextView rentTenantNameView = mView.findViewById(R.id.tvRentTenantName);
            rentTenantNameView.setText(rentTenantName);
        }
    }

}