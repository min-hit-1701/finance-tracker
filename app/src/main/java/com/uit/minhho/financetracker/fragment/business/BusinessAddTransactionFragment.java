package com.uit.minhho.financetracker.fragment.business;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.appbar.MaterialToolbar;
import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.data.remote.BusinessApiClient;

import java.util.Calendar;
import java.util.Locale;

public class BusinessAddTransactionFragment extends Fragment {

    private EditText etAmount, etPartner, etDate, etNote;
    private AutoCompleteTextView spinnerCategory;
    private MaterialButtonToggleGroup typeToggleGroup;
    private BusinessApiClient businessApiClient;
    private int selectedYear;
    private int selectedMonth;
    private int selectedDay;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_business_add_transaction, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupCategorySpinner();
        setupDatePicker();

        MaterialButton saveButton = view.findViewById(R.id.btn_save);
        view.findViewById(R.id.btn_save).setOnClickListener(v -> {
            if (validateInputs()) {
                saveButton.setEnabled(false);
                saveTransaction(saveButton);
            }
        });
    }

    private void initViews(View view) {
        businessApiClient = new BusinessApiClient(requireContext());
        etAmount = view.findViewById(R.id.et_amount);
        etPartner = view.findViewById(R.id.et_partner);
        etDate = view.findViewById(R.id.et_date);
        etNote = view.findViewById(R.id.et_note);
        spinnerCategory = view.findViewById(R.id.spinner_category);
        typeToggleGroup = view.findViewById(R.id.toggle_group_type);

        Calendar calendar = Calendar.getInstance();
        selectedYear = calendar.get(Calendar.YEAR);
        selectedMonth = calendar.get(Calendar.MONTH);
        selectedDay = calendar.get(Calendar.DAY_OF_MONTH);

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());
    }

    private void setupCategorySpinner() {
        String[] categories = {
                getString(R.string.business_tx_inventory),
                getString(R.string.business_tx_payroll),
                getString(R.string.business_tx_rent),
                getString(R.string.business_tx_advert),
                getString(R.string.cat_other)
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, categories);
        spinnerCategory.setAdapter(adapter);
    }

    private void setupDatePicker() {
        etDate.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                    (view1, year1, monthOfYear, dayOfMonth) -> {
                        selectedYear = year1;
                        selectedMonth = monthOfYear;
                        selectedDay = dayOfMonth;
                        String date = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1;
                        etDate.setText(date);
                    }, year, month, day);
            datePickerDialog.show();
        });
    }

    private void saveTransaction(MaterialButton saveButton) {
        double amount = Double.parseDouble(etAmount.getText().toString().trim());
        String partner = etPartner.getText().toString().trim();
        String category = spinnerCategory.getText().toString().trim();
        String note = etNote.getText() == null ? "" : etNote.getText().toString().trim();
        boolean isIncome = typeToggleGroup.getCheckedButtonId() == R.id.btn_revenue;
        String timestamp = String.format(
                Locale.US,
                "%04d-%02d-%02d 00:00:00",
                selectedYear,
                selectedMonth + 1,
                selectedDay
        );

        Context appContext = requireContext().getApplicationContext();
        new Thread(() -> {
            BusinessApiClient.ApiResult result = businessApiClient.createTransaction(
                    amount,
                    partner,
                    category,
                    timestamp,
                    note,
                    isIncome
            );

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
        }).start();
    }

    private boolean validateInputs() {
        if (etAmount.getText().toString().trim().isEmpty()) {
            etAmount.setError("Vui lòng nhập số tiền");
            return false;
        }
        try {
            double amount = Double.parseDouble(etAmount.getText().toString().trim());
            if (amount <= 0) {
                etAmount.setError("Số tiền phải lớn hơn 0");
                return false;
            }
        } catch (NumberFormatException exception) {
            etAmount.setError("Số tiền không hợp lệ");
            return false;
        }
        if (etPartner.getText().toString().trim().isEmpty()) {
            etPartner.setError("Vui lòng nhập tên đối tác");
            return false;
        }
        if (spinnerCategory.getText().toString().trim().isEmpty()) {
            spinnerCategory.setError("Vui lòng chọn danh mục");
            return false;
        }
        return true;
    }
}
