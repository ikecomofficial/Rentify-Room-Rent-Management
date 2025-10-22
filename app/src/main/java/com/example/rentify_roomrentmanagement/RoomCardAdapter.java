package com.example.rentify_roomrentmanagement;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.NumberFormat;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class RoomCardAdapter extends RecyclerView.Adapter<RoomCardAdapter.RoomViewHolder> {

    private Context context;
    private List<Rooms> roomList;
    private  GradientDrawable gradientDrawable;
    private OnItemClickListener listener;

    public RoomCardAdapter(Context context, List<Rooms> roomList) {
        this.context = context;
        this.roomList = roomList;
    }

    // Define the interface for the callback
    public interface OnItemClickListener {
        void onItemClick(Rooms room);
    }

    // Add a public method to set the listener from the Activity
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public static class RoomViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoomName, tvRentAmount, tvTenantName, tvTenantPhone, tvLastPaidUnit, tvRoomStatus;
        CircleImageView cimgTenantProfile;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoomName = itemView.findViewById(R.id.tvRoomName);
            tvRentAmount = itemView.findViewById(R.id.tvRoomRent);
            tvTenantName = itemView.findViewById(R.id.tvTenantName);
            tvTenantPhone = itemView.findViewById(R.id.tvTenantNumber);
            cimgTenantProfile = itemView.findViewById(R.id.imgTenantProfile);
            tvRoomStatus = itemView.findViewById(R.id.tvRoomStatus);
            tvLastPaidUnit = itemView.findViewById(R.id.tvLastPaidUnit);
        }
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_room_layout, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        Rooms room = roomList.get(position);

        holder.tvRoomName.setText(room.getRoom_name());
        String rentFormat = NumberFormat.getNumberInstance().format(room.getRoom_rent());
        holder.tvRentAmount.setText(rentFormat);

        if (room.getTenant_name() != null && room.getTenant_phone() != null && room.getThumb_tenant_url() != null) {
            holder.tvTenantName.setText(room.getTenant_name());
            holder.tvTenantPhone.setText(room.getTenant_phone());
            Glide.with(context).load(room.getThumb_tenant_url())
                    .placeholder(R.drawable.ic_tenant_profile_default)
                    .into(holder.cimgTenantProfile);
        } else {
            holder.tvTenantName.setText("No Tenant Added");
            holder.tvTenantPhone.setText("Click (Add Tenant)");
            holder.cimgTenantProfile.setImageResource(R.drawable.ic_no_tenant_profile_default);
        }

        gradientDrawable = (GradientDrawable) holder.tvRoomStatus.getBackground();
        if (Boolean.TRUE.equals(room.isIs_occupied())){
            holder.tvRoomStatus.setText("Occupied");
            gradientDrawable.setColor(Color.parseColor("#CB5CAF6E"));
        } else {
            holder.tvRoomStatus.setText("Vacant");
            gradientDrawable.setColor(Color.parseColor("#C0F6695E"));
        }
        holder.tvLastPaidUnit.setText(String.valueOf(room.getLast_unit_paid()));


        /* if (Boolean.TRUE.equals(room.isIs_rent_paid())){
            holder.tvRentStatus.setText("Paid");
            holder.tvRentStatus.setTextColor(Color.parseColor("#CB5CAF6E"));
        } else {
            holder.tvRentStatus.setText("Unpaid");
            holder.tvRentStatus.setTextColor(Color.parseColor("#C0F6695E"));
        }

         */

        // Click on room card - goes to Room Details Activity
        // Set the OnClickListener on the entire item view
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(room);
            }
        });
    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }
}
