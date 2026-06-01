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
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.data.local.entity.BusinessContact;
import com.uit.minhho.financetracker.data.local.entity.Transaction;
import com.uit.minhho.financetracker.data.local.entity.Wallet;
import com.uit.minhho.financetracker.viewmodel.BusinessViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class BusinessAddTransactionFragment extends Fragment {

    private EditText etAmount;
    private AutoCompleteTextView autoPartner;
    private EditText etDate;
    private EditText etNote;
    private AutoCompleteTextView spinnerCategory;
    private AutoCompleteTextView spinnerWallet;
    private MaterialButtonToggleGroup toggleType;
    private boolean isIncome = false;
    private long selectedTimestamp;
    private BusinessViewModel businessViewModel;
    private List<String> partnerNames = new ArrayList<>();
    private List<BusinessContact> allContacts = new ArrayList<>();
    private List<Wallet> allWallets = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_business_add_transaction, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        businessViewModel = new ViewModelProvider(requireActivity()).get(BusinessViewModel.class);
        selectedTimestamp = System.currentTimeMillis();

        initViews(view);
        setupCategorySpinner();
        setupDatePicker();
        setupToggle();
        observeWallets();
        observeContacts();

        view.findViewById(R.id.btn_save).setOnClickListener(v -> onSaveTransaction());
        view.findViewById(R.id.btn_add_partner).setOnClickListener(v -> openAddContactFragment());

        getParentFragmentManager().setFragmentResultListener(
                BusinessAddContactFragment.REQUEST_KEY,
                getViewLifecycleOwner(),
                (requestKey, result) -> {
                    String name = result.getString(BusinessAddContactFragment.KEY_NAME, "");
                    String type = result.getString(BusinessAddContactFragment.KEY_TYPE, "");
                    String note = result.getString(BusinessAddContactFragment.KEY_NOTE, getString(R.string.business_note_default));
                    businessViewModel.addBusinessContact(name, type, note);
                    autoPartner.setText(name, false);
                });

        businessViewModel.getOperationMessage().observe(getViewLifecycleOwner(), message -> {
            if (message == null || message.trim().isEmpty()) {
                return;
            }
            if (BusinessViewModel.EVENT_BUSINESS_TRANSACTION_SAVED.equals(message)) {
                Toast.makeText(requireContext(), R.string.business_transaction_saved, Toast.LENGTH_SHORT).show();
                businessViewModel.clearOperationMessage();
                getParentFragmentManager().popBackStack();
                return;
            }
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            businessViewModel.clearOperationMessage();
        });
    }

    private void initViews(View view) {
        etAmount = view.findViewById(R.id.et_amount);
        autoPartner = view.findViewById(R.id.et_partner);
        etDate = view.findViewById(R.id.et_date);
        etNote = view.findViewById(R.id.et_note);
        spinnerCategory = view.findViewById(R.id.spinner_category);
        spinnerWallet = view.findViewById(R.id.spinner_wallet);
        toggleType = view.findViewById(R.id.toggle_group_type);

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());
    }

    private void observeWallets() {
        businessViewModel.getBusinessWallets().observe(getViewLifecycleOwner(), wallets -> {
            allWallets.clear();
            if (wallets != null) {
                allWallets.addAll(wallets);
            }
            List<String> walletNames = new ArrayList<>();
            for (Wallet w : allWallets) {
                walletNames.add(w.getName());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    walletNames
            );
            spinnerWallet.setAdapter(adapter);
            if (!walletNames.isEmpty()) {
                spinnerWallet.setText(walletNames.get(0), false);
            }
        });
    }

    private void observeContacts() {
        businessViewModel.getBusinessContacts().observe(getViewLifecycleOwner(), contacts -> {
            allContacts.clear();
            partnerNames.clear();
            if (contacts != null) {
                allContacts.addAll(contacts);
                for (BusinessContact c : contacts) {
                    partnerNames.add(c.getName());
                }
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    partnerNames
            );
            autoPartner.setAdapter(adapter);
        });
    }

    private void setupCategorySpinner() {
        String[] categories = {
                getString(R.string.business_tx_inventory),
                getString(R.string.business_tx_payroll),
                getString(R.string.business_tx_rent),
                getString(R.string.business_tx_advert),
                getString(R.string.cat_other)
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                categories
        );
        spinnerCategory.setAdapter(adapter);
        spinnerCategory.setText(categories[0], false);
    }

    private void setupDatePicker() {
        updateDateField(selectedTimestamp);
        etDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(selectedTimestamp);
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                    (view1, year1, monthOfYear, dayOfMonth) -> {
                        Calendar selected = Calendar.getInstance();
                        selected.set(Calendar.YEAR, year1);
                        selected.set(Calendar.MONTH, monthOfYear);
                        selected.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        selected.set(Calendar.HOUR_OF_DAY, 12);
                        selected.set(Calendar.MINUTE, 0);
                        selected.set(Calendar.SECOND, 0);
                        selected.set(Calendar.MILLISECOND, 0);
                        selectedTimestamp = selected.getTimeInMillis();
                        updateDateField(selectedTimestamp);
                    }, year, month, day);
            datePickerDialog.show();
        });
    }

    private void setupToggle() {
        toggleType.addOnButtonCheckedListener((group, checkedId, checked) -> {
            if (!checked) {
                return;
            }
            isIncome = checkedId == R.id.btn_revenue;
            MaterialButton btnRevenue = group.findViewById(R.id.btn_revenue);
            MaterialButton btnExpense = group.findViewById(R.id.btn_expense);
            if (btnRevenue != null && btnExpense != null) {
                btnRevenue.setChecked(checkedId == R.id.btn_revenue);
                btnExpense.setChecked(checkedId == R.id.btn_expense);
            }
        });
    }

    private void openAddContactFragment() {
        getParentFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragment_container_business, new BusinessAddContactFragment())
                .addToBackStack(null)
                .commit();
    }

    private void onSaveTransaction() {
        String amountText = textOf(etAmount);
        String partner = textOf(autoPartner);
        String category = textOf(spinnerCategory);
        String note = textOf(etNote);
        String walletName = textOf(spinnerWallet);

        if (amountText.isEmpty()) {
            etAmount.setError(getString(R.string.business_error_amount_required));
            return;
        }
        if (partner.isEmpty()) {
            autoPartner.setError(getString(R.string.business_error_partner_required));
            return;
        }
        if (category.isEmpty()) {
            spinnerCategory.setError(getString(R.string.business_error_category_required));
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException exception) {
            etAmount.setError(getString(R.string.business_error_amount_invalid));
            return;
        }

        if (amount <= 0) {
            etAmount.setError(getString(R.string.business_error_amount_invalid));
            return;
        }

        int walletId = 0;
        if (!walletName.isEmpty()) {
            for (Wallet w : allWallets) {
                if (walletName.equals(w.getName())) {
                    walletId = w.getId();
                    break;
                }
            }
        }

        boolean partnerExists = false;
        for (String name : partnerNames) {
            if (name.equalsIgnoreCase(partner)) {
                partnerExists = true;
                break;
            }
        }
        if (!partnerExists) {
            businessViewModel.addBusinessContact(partner, getString(R.string.business_note_default), getString(R.string.business_note_default));
        }

        String notePayload = category + " | " + partner + " | " + note;
        Transaction transaction = new Transaction(
                amount,
                selectedTimestamp,
                notePayload,
                0,
                walletId,
                0,
                isIncome,
                true
        );
        businessViewModel.addBusinessTransaction(transaction);
    }

    private void updateDateField(long timestamp) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timestamp);
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH) + 1;
        int year = c.get(Calendar.YEAR);
        etDate.setText(day + "/" + month + "/" + year);
    }

    private String textOf(EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private String textOf(AutoCompleteTextView editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }
}
