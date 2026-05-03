package com.dec.attendpro.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.dec.attendpro.OnboardingFragment;
import com.dec.attendpro.R;

public class OnboardingAdapter extends FragmentStateAdapter {

    public OnboardingAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return OnboardingFragment.newInstance(
                        "Face Recognition",
                        "Mark attendance effortlessly using AI-powered face recognition technology.",
                        R.drawable.ic_face_recognition);
            case 1:
                return OnboardingFragment.newInstance(
                        "Real-time Analytics",
                        "Get instant insights into student attendance trends and performance.",
                        R.drawable.ic_analytics);
            case 2:
                return OnboardingFragment.newInstance(
                        "Smart Alerts",
                        "Receive automated notifications for low attendance and important updates.",
                        R.drawable.ic_notifications);
            default:
                return OnboardingFragment.newInstance("", "", 0);
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
