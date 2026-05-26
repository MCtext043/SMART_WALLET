package com.example.smartwallet.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class AuthPagerAdapter extends FragmentStateAdapter {
    public AuthPagerAdapter(@NonNull AppCompatActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) return new LoginFragment();
        return new RegisterFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}








