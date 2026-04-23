package com.uit.minhho.financetracker.fragment.business;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputLayout;
import com.uit.minhho.financetracker.R;

public class BusinessAddBudgetFragment extends Fragment {

    public static final String REQUEST_KEY = "request_add_business_budget";
    public static final String KEY_NAME = "budget_name";
    public static final String KEY_LIMIT = "budget_limit";
    public static final String KEY_USED = "budget_used";

    public BusinessAddBudgetFragment() {
        super(R.layout.fragment_business_add_budget);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextInputLayout nameLayout = view.findViewById(R.id.til_add_budget_name);
        TextInputLayout limitLayout = view.findViewById(R.id.til_add_budget_limit);
        TextInputLayout usedLayout = view.findViewById(R.id.til_add_budget_used);
        EditText nameInput = view.findViewById(R.id.et_add_budget_name);
        EditText limitInput = view.findViewById(R.id.et_add_budget_limit);
        EditText usedInput = view.findViewById(R.id.et_add_budget_used);

        view.findViewById(R.id.btn_save_new_budget).setOnClickListener(v -> {
            nameLayout.setError(null);
            limitLayout.setError(null);
            usedLayout.setError(null);

            String name = safeText(nameInput);
            String limitText = safeText(limitInput);
            String usedText = safeText(usedInput);

            if (TextUtils.isEmpty(name)) {
                nameLayout.setError(getString(R.string.business_budget_error_name));
                return;
            }
            if (TextUtils.isEmpty(limitText)) {
                limitLayout.setError(getString(R.string.business_budget_error_limit));
                return;
            }
            if (TextUtils.isEmpty(usedText)) {
                usedLayout.setError(getString(R.string.business_budget_error_used));
                return;
            }

            int limit;
            int used;
            try {
                limit = Integer.parseInt(limitText);
                used = Integer.parseInt(usedText);
            } catch (NumberFormatException exception) {
                limitLayout.setError(getString(R.string.business_budget_error_limit));
                return;
            }

            if (limit <= 0) {
                limitLayout.setError(getString(R.string.business_budget_error_limit));
                return;
            }
            if (used < 0) {
                usedLayout.setError(getString(R.string.business_budget_error_used));
                return;
            }

            Bundle result = new Bundle();
            result.putString(KEY_NAME, name);
            result.putInt(KEY_LIMIT, limit);
            result.putInt(KEY_USED, used);
            getParentFragmentManager().setFragmentResult(REQUEST_KEY, result);
            getParentFragmentManager().popBackStack();
        });

        view.findViewById(R.id.btn_cancel_new_budget).setOnClickListener(v ->
                getParentFragmentManager().popBackStack()
        );
    }

    private String safeText(EditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }
}
