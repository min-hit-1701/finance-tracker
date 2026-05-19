package com.uit.minhho.financetracker.fragment.personal;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.uit.minhho.financetracker.R;

import java.util.Calendar;

public class AddTransactionFragment extends Fragment {

    private EditText etDate, etAmount, etNote;
    private AutoCompleteTextView spinnerCategory, spinnerWallet;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_personal_add_transaction, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupSpinners();
        setupDatePicker();

        view.findViewById(R.id.btn_save).setOnClickListener(v -> {
            // Logic to save transaction
            getParentFragmentManager().popBackStack();
        });
    }

    private void initViews(View view) {
        etDate = view.findViewById(R.id.et_date);
        etAmount = view.findViewById(R.id.et_amount);
        etNote = view.findViewById(R.id.et_note);
        spinnerCategory = view.findViewById(R.id.spinner_category);
        spinnerWallet = view.findViewById(R.id.spinner_wallet);

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());
    }

    private void setupSpinners() {
        // Fake categories for spinner
        String[] categories = {"Food", "Transport", "Shopping", "Salary", "Bonus"};
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, categories);
        spinnerCategory.setAdapter(catAdapter);

        // Fake wallets for spinner
        String[] wallets = {"Cash", "Vietcombank", "MoMo Wallet"};
        ArrayAdapter<String> walletAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, wallets);
        spinnerWallet.setAdapter(walletAdapter);
    }

    private void setupDatePicker() {
        etDate.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                    (view, year1, monthOfYear, dayOfMonth) -> etDate.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1),
                    year, month, day);
            datePickerDialog.show();
        });
    }
}
