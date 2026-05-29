package com.uit.minhho.financetracker.fragment.personal;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.data.local.entity.Budget;
import com.uit.minhho.financetracker.data.local.entity.Category;
import com.uit.minhho.financetracker.viewmodel.BudgetViewModel;
import com.uit.minhho.financetracker.viewmodel.CategoryViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class AddBudgetFragment extends Fragment {

    private BudgetViewModel budgetViewModel;
    private CategoryViewModel categoryViewModel;
    private TextInputEditText etBudgetAmount;
    private AutoCompleteTextView spinnerCategory;
    private AutoCompleteTextView spinnerPeriod;
    private final Map<String, Integer> categoryIdsByName = new LinkedHashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_personal_add_budget, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ánh xạ các view
        etBudgetAmount = view.findViewById(R.id.et_budget_amount);
        spinnerCategory = view.findViewById(R.id.spinner_category);
        spinnerPeriod = view.findViewById(R.id.spinner_period);

        budgetViewModel = new ViewModelProvider(this).get(BudgetViewModel.class);
        categoryViewModel = new ViewModelProvider(requireActivity()).get(CategoryViewModel.class);
        setupSpinners();
        categoryViewModel.refreshCategories();

        // Toolbar navigation
        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());
        }

        // Nút lưu
        View btnSave = view.findViewById(R.id.btn_save_budget);
        if (btnSave != null) {
            btnSave.setOnClickListener(v -> saveBudgetLogic());
        }
    }

    private void setupSpinners() {
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        spinnerCategory.setAdapter(catAdapter);
        categoryViewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            categoryIdsByName.clear();
            catAdapter.clear();
            if (categories != null) {
                for (Category category : categories) {
                    categoryIdsByName.put(category.getName(), category.getId());
                    catAdapter.add(category.getName());
                }
            }
            catAdapter.notifyDataSetChanged();
        });

        String[] periods = getResources().getStringArray(R.array.budget_periods);
        ArrayAdapter<String> periodAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, periods);
        spinnerPeriod.setAdapter(periodAdapter);
        spinnerPeriod.setText(periods.length > 1 ? periods[1] : "", false);
    }

    private void saveBudgetLogic() {
        String amountStr = etBudgetAmount.getText().toString().trim();
        String selectedCategory = spinnerCategory.getText().toString();
        String selectedPeriod = spinnerPeriod.getText().toString();

        if (selectedCategory.isEmpty()) {
            spinnerCategory.setError("Vui lòng chọn danh mục!");
            return;
        }

        Integer categoryId = categoryIdsByName.get(selectedCategory);
        if (categoryId == null) {
            spinnerCategory.setError("Danh mục chưa có trong database!");
            return;
        }

        if (amountStr.isEmpty()) {
            etBudgetAmount.setError("Vui lòng nhập số tiền ngân sách!");
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);

            double currentUsage = 0.0;
            String period = periodToDatabaseMonth(selectedPeriod);

            Budget newBudget = new Budget(categoryId, amount, currentUsage, period);
            View saveButton = getView() == null ? null : getView().findViewById(R.id.btn_save_budget);
            if (saveButton != null) {
                saveButton.setEnabled(false);
            }
            Context appContext = requireContext().getApplicationContext();
            budgetViewModel.insert(newBudget, (success, message) -> {
                Activity activity = getActivity();
                if (!isAdded() || activity == null) {
                    return;
                }

                activity.runOnUiThread(() -> {
                    if (!isAdded()) {
                        return;
                    }
                    if (saveButton != null) {
                        saveButton.setEnabled(true);
                    }
                    Toast.makeText(appContext, message, Toast.LENGTH_SHORT).show();
                    if (success) {
                        getParentFragmentManager().popBackStack();
                    }
                });
            });

        } catch (NumberFormatException e) {
            etBudgetAmount.setError("Số tiền nhập vào không hợp lệ!");
        }
    }

    private String periodToDatabaseMonth(String selectedPeriod) {
        return new SimpleDateFormat("yyyy-MM", Locale.US).format(new Date());
    }
}
