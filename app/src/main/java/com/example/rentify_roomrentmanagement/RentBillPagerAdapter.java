package com.example.rentify_roomrentmanagement;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.rentify_roomrentmanagement.fragments.BillsFragment;
import com.example.rentify_roomrentmanagement.fragments.RentsFragment;

public class RentBillPagerAdapter extends FragmentStateAdapter {

    private String room_id;
    public RentBillPagerAdapter(@NonNull FragmentActivity fragmentActivity, String room_id) {
        super(fragmentActivity);
        this.room_id = room_id;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment;
        Bundle bundle = new Bundle();
        bundle.putString("room_id", room_id);
        switch (position){
            case 0:
                fragment = new RentsFragment();
                break;
            case 1:
                fragment = new BillsFragment();
                break;
            default:
                fragment = new RentsFragment();
                break;
        }
        fragment.setArguments(bundle); // pass the roomId to fragment
        return fragment;
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
