package com.uit.minhho.financetracker.fragment.business;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.data.remote.BusinessApiClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BusinessAddBudgetFragment extends Fragment {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private BusinessApiClient apiClient;

    public BusinessAddBudgetFragment() {
        super(R.layout.fragment_business_add_budget);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiClient = new BusinessApiClient(requireContext());
        TextInputLayout nameLayout = view.findViewById(R.id.til_add_budget_name);
        TextInputLayout categoryLayout = view.findViewById(R.id.til_add_budget_category);
        TextInputLayout limitLayout = view.findViewById(R.id.til_add_budget_limit);
        EditText nameInput = view.findViewById(R.id.et_add_budget_name);
        AutoCompleteTextView categoryInput = view.findViewById(R.id.spinner_add_budget_category);
        EditText limitInput = view.findViewById(R.id.et_add_budget_limit);
        MaterialButton saveButton = view.findViewById(R.id.btn_save_new_budget);
        setupCategorySpinner(categoryInput);

        saveButton.setOnClickListener(v -> {
            nameLayout.setError(null);
            categoryLayout.setError(null);
            limitLayout.setError(null);

            String name = safeText(nameInput);
            String category = safeText(categoryInput);
            String limitText = safeText(limitInput);

            if (TextUtils.isEmpty(name)) {
                nameLayout.setError(getString(R.string.business_budget_error_name));
                return;
            }
            if (TextUtils.isEmpty(category)) {
                categoryLayout.setError(getString(R.string.business_budget_error_category));
                return;
            }
            if (TextUtils.isEmpty(limitText)) {
                limitLayout.setError(getString(R.string.business_budget_error_limit));
                return;
            }

            int limit;
            try {
                limit = Integer.parseInt(limitText);
            } catch (NumberFormatException exception) {
                limitLayout.setError(getString(R.string.business_budget_error_limit));
                return;
            }

            if (limit <= 0) {
                limitLayout.setError(getString(R.string.business_budget_error_limit));
                return;
            }

            saveButton.setEnabled(false);
            Context appContext = requireContext().getApplicationContext();
            int finalLimit = limit;
            executorService.execute(() -> {
                BusinessApiClient.ApiResult result = apiClient.createBusinessBudget(name, category, finalLimit);
                Activity activity = getActivity();
                if (!isAdded() || activity == null) {
                    return;
                }
                activity.runOnUiThread(() -> {
                    if (!isAdded()) {
                        return;
                    }
                    saveButton.setEnabled(true);
                    Toast.makeText(appContext, result.message, Toast.LENGTH_SHORT).show();
                    if (result.success) {
                        getParentFragmentManager().popBackStack();
                    }
                });
            });
        });

        view.findViewById(R.id.btn_cancel_new_budget).setOnClickListener(v ->
                getParentFragmentManager().popBackStack()
        );
    }

    private void setupCategorySpinner(AutoCompleteTextView categoryInput) {
        String[] categories = {
                getString(R.string.business_tx_inventory),
                getString(R.string.business_tx_payroll),
                getString(R.string.business_tx_rent),
                getString(R.string.business_tx_advert),
                getString(R.string.cat_other)
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, categories);
        categoryInput.setAdapter(adapter);
        if (categoryInput.getText().toString().trim().isEmpty()) {
            categoryInput.setText(categories[0], false);
        }
    }

    private String safeText(EditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
