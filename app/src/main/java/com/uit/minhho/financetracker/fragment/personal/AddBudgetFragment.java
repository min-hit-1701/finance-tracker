package com.uit.minhho.financetracker.fragment.personal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.uit.minhho.financetracker.R;

public class AddBudgetFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_personal_add_budget, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());

        setupSpinners(view);

        view.findViewById(R.id.btn_save_budget).setOnClickListener(v -> {
            // Logic lưu ngân sách mới
            getParentFragmentManager().popBackStack();
        });
    }

    private void setupSpinners(View view) {
        // Category Spinner
        AutoCompleteTextView spinnerCat = view.findViewById(R.id.spinner_category);
        String[] categories = {"Ăn uống", "Di chuyển", "Mua sắm", "Giải trí", "Khác"};
        spinnerCat.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, categories));

        // Period Spinner
        AutoCompleteTextView spinnerPeriod = view.findViewById(R.id.spinner_period);
        String[] periods = {"Tuần này", "Tháng này", "Năm nay"};
        spinnerPeriod.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, periods));
    }
}