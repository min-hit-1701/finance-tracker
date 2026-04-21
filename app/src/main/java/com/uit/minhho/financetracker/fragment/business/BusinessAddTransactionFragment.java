package com.uit.minhho.financetracker.fragment.business;

import android.app.DatePickerDialog;
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

import com.google.android.material.appbar.MaterialToolbar;
import com.uit.minhho.financetracker.R;

import java.util.Calendar;

public class BusinessAddTransactionFragment extends Fragment {

    private EditText etAmount, etPartner, etDate, etNote;
    private AutoCompleteTextView spinnerCategory;

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

        view.findViewById(R.id.btn_save).setOnClickListener(v -> {
            if (validateInputs()) {
                // Demo logic: Show success and go back
                Toast.makeText(requireContext(), R.string.business_saved_success, Toast.LENGTH_SHORT).show();
                getParentFragmentManager().popBackStack();
            }
        });
    }

    private void initViews(View view) {
        etAmount = view.findViewById(R.id.et_amount);
        etPartner = view.findViewById(R.id.et_partner);
        etDate = view.findViewById(R.id.et_date);
        etNote = view.findViewById(R.id.et_note);
        spinnerCategory = view.findViewById(R.id.spinner_category);

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
                        String date = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1;
                        etDate.setText(date);
                    }, year, month, day);
            datePickerDialog.show();
        });
    }

    private boolean validateInputs() {
        if (etAmount.getText().toString().trim().isEmpty()) {
            etAmount.setError("Vui lòng nhập số tiền");
            return false;
        }
        if (etPartner.getText().toString().trim().isEmpty()) {
            etPartner.setError("Vui lòng nhập tên đối tác");
            return false;
        }
        return true;
    }
}
