package com.example.rentify_roomrentmanagement.fragments;

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

import com.example.rentify_roomrentmanagement.Bills;
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

public class BillsFragment extends Fragment {

    private TextView tvNoBillRecord;
    private LinearLayout layoutBillHeaderRow;
    private RecyclerView rvElcBillList;
    private DatabaseReference ebillsReference;
    private FirebaseRecyclerAdapter<Bills, BillsFragment.BillsViewHolder> firebaseBillsRecyclerAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_bills, container, false);

        // Get roomId from arguments

        String room_id = getArguments() != null ? getArguments().getString("room_id") : null;

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        assert room_id != null;
        ebillsReference = databaseReference.child("e-bills").child(room_id);

        // e-bill Recycler View
        rvElcBillList = view.findViewById(R.id.rvElcBillRecord);
        LinearLayoutManager layoutBillManager = new LinearLayoutManager(getContext());
        layoutBillManager.setReverseLayout(true);    // newest items appear at top
        layoutBillManager.setStackFromEnd(true);    // optional, usually false
        rvElcBillList.setLayoutManager(layoutBillManager);

        tvNoBillRecord = view.findViewById(R.id.tvNoBillRecord);
        layoutBillHeaderRow = view.findViewById(R.id.layoutBillHeaderRow);

        loadElcBillRecyclerList();

        if (firebaseBillsRecyclerAdapter != null) {
            rvElcBillList.setAdapter(firebaseBillsRecyclerAdapter);
            firebaseBillsRecyclerAdapter.startListening();
        }


        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (firebaseBillsRecyclerAdapter != null && firebaseBillsRecyclerAdapter.getSnapshots().isListening()) {
            firebaseBillsRecyclerAdapter.stopListening();
        }
    }

    private void loadElcBillRecyclerList(){

        FirebaseRecyclerOptions<Bills> bill_options = new FirebaseRecyclerOptions.Builder<Bills>()
                .setQuery(ebillsReference, Bills.class)
                .build();

        firebaseBillsRecyclerAdapter = new FirebaseRecyclerAdapter<Bills, BillsFragment.BillsViewHolder>(bill_options) {
            @Override
            protected void onBindViewHolder(@NonNull BillsFragment.BillsViewHolder holder, int position, @NonNull Bills model) {
                // Bind your data here
                String billDate = model.getEbill_date();
                holder.setBillAmount(model.getEbill_amount());
                holder.setBillDate(billDate);
                holder.setBillTime(model.getEbill_time());
                holder.setBillPaymentMode(model.getPayment_mode());
                holder.setBillUnitPaidTill(model.getPaid_upto());
                holder.setBillUnitUsed(model.getUnits_used());
                // etc.

                // Extract current month-year
                String[] parts = billDate.split(" ");
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
                    Bills nextModel = getItem(position + 1);
                    String[] nextParts = nextModel.getEbill_date().split(" ");
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

                // Long press to delete
                holder.itemView.setOnLongClickListener(v -> {
                    new MaterialAlertDialogBuilder(v.getContext())
                            .setTitle("Delete Electricity Bill?")
                            .setMessage("Are you sure you want to delete this bill record?")
                            .setPositiveButton("Delete", (dialog, which) -> {
                                // 🔑 Call your delete function here
                                deleteElcBillRecord(getRef(position).getKey());
                            })
                            .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                            .show();

                    return true; // ✅ consume the long press
                });

            }

            @NonNull
            @Override
            public BillsFragment.BillsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.single_ebill_layout, parent, false);
                return new BillsFragment.BillsViewHolder(view);
            }

            @Override
            public void onDataChanged() {
                super.onDataChanged();

                int itemCount = getItemCount();
                if (itemCount == 0) {
                    tvNoBillRecord.setVisibility(View.VISIBLE);
                    layoutBillHeaderRow.setVisibility(View.GONE);
                } else {
                    tvNoBillRecord.setVisibility(View.GONE);
                    layoutBillHeaderRow.setVisibility(View.VISIBLE);
                }
            }

        };

        rvElcBillList.setAdapter(firebaseBillsRecyclerAdapter);

    }

    private void deleteElcBillRecord(String ebill_id) {
        ebillsReference.child(ebill_id).removeValue()
                .addOnSuccessListener(aVoid -> {
                    // ✅ Record deleted successfully
                    Toast.makeText(getContext(), "E-bill Deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // ❌ Handle failure
                    Toast.makeText(getContext(), "E-bill Not Deleted", Toast.LENGTH_SHORT).show();
                });
    }

    public static class BillsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public BillsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

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

    }
}