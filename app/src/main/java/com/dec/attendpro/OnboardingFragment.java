package com.dec.attendpro;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class OnboardingFragment extends Fragment {
    private static final String ARG_TITLE = "title";
    private static final String ARG_DESC = "desc";
    private static final String ARG_IMAGE = "image";

    public static OnboardingFragment newInstance(String title, String desc, int imageResId) {
        OnboardingFragment fragment = new OnboardingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_DESC, desc);
        args.putInt(ARG_IMAGE, imageResId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_onboarding, container, false);
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        TextView tvDesc = view.findViewById(R.id.tvDescription);
        ImageView ivOnboarding = view.findViewById(R.id.ivOnboarding);

        if (getArguments() != null) {
            tvTitle.setText(getArguments().getString(ARG_TITLE));
            tvDesc.setText(getArguments().getString(ARG_DESC));
            ivOnboarding.setImageResource(getArguments().getInt(ARG_IMAGE));
        }
        return view;
    }
}
