package com.uit.minhho.financetracker.fragment.business;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.uit.minhho.financetracker.R;

public class BusinessDetailFragment extends Fragment {

    public BusinessDetailFragment() {
        super(R.layout.fragment_business_detail);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.btn_open_report_from_detail).setOnClickListener(v ->
                openChildScreen(new ReportFragment())
        );

        view.findViewById(R.id.btn_back_from_business_detail).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );
    }

    private void openChildScreen(Fragment fragment) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragment_container_business, fragment)
                .addToBackStack(null)
                .commit();
    }
}
