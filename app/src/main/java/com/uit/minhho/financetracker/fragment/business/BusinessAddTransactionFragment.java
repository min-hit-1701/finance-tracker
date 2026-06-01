package com.uit.minhho.financetracker.fragment.business;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.appbar.MaterialToolbar;
import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.data.remote.BusinessApiClient;
import com.uit.minhho.financetracker.model.business.BusinessEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BusinessAddTransactionFragment extends DialogFragment {

    public static final String REQUEST_KEY = "request_business_transaction_saved";
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final List<BusinessApiClient.WalletOption> walletOptions = new ArrayList<>();
    private EditText etAmount, etDate, etNote;
    private AutoCompleteTextView spinnerPartner, spinnerWallet, spinnerCategory;
    private MaterialButtonToggleGroup typeToggleGroup;
    private BusinessApiClient businessApiClient;
    private int selectedWalletId;

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
        setupAutomaticTimestampField();
        loadBusinessOptions();

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
        spinnerPartner = view.findViewById(R.id.et_partner);
        spinnerWallet = view.findViewById(R.id.spinner_wallet);
        etDate = view.findViewById(R.id.et_date);
        etNote = view.findViewById(R.id.et_note);
        spinnerCategory = view.findViewById(R.id.spinner_category);
        typeToggleGroup = view.findViewById(R.id.toggle_group_type);

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> closeForm());
    }

    private void setupCategorySpinner() {
        typeToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                spinnerCategory.setText("", false);
                loadCategoryOptions(checkedId == R.id.btn_revenue);
            }
        });
        loadCategoryOptions(typeToggleGroup.getCheckedButtonId() == R.id.btn_revenue);
    }

    private void loadCategoryOptions(boolean income) {
        executorService.execute(() -> {
            List<String> categories = businessApiClient.getBusinessCategoryNames(income);
            Activity activity = getActivity();
            if (!isAdded() || activity == null) {
                return;
            }

            activity.runOnUiThread(() -> {
                if (!isAdded()) {
                    return;
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, categories);
                spinnerCategory.setAdapter(adapter);
                if (!categories.isEmpty() && spinnerCategory.getText().toString().trim().isEmpty()) {
                    spinnerCategory.setText(categories.get(0), false);
                }
            });
        });
    }

    private void loadBusinessOptions() {
        executorService.execute(() -> {
            List<BusinessEntity> entities = businessApiClient.getBusinessEntities();
            List<BusinessApiClient.WalletOption> wallets = businessApiClient.getWalletOptions();
            Activity activity = getActivity();
            if (!isAdded() || activity == null) {
                return;
            }

            activity.runOnUiThread(() -> {
                if (!isAdded()) {
                    return;
                }
                bindPartnerOptions(entities);
                bindWalletOptions(wallets);
            });
        });
    }

    private void bindPartnerOptions(List<BusinessEntity> entities) {
        List<String> partnerNames = new ArrayList<>();
        for (BusinessEntity entity : entities) {
            if (entity.getName() != null && !entity.getName().trim().isEmpty()) {
                partnerNames.add(entity.getName());
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, partnerNames);
        spinnerPartner.setAdapter(adapter);
        if (!partnerNames.isEmpty() && spinnerPartner.getText().toString().trim().isEmpty()) {
            spinnerPartner.setText(partnerNames.get(0), false);
        }
    }

    private void bindWalletOptions(List<BusinessApiClient.WalletOption> wallets) {
        walletOptions.clear();
        walletOptions.addAll(wallets);

        List<String> walletNames = new ArrayList<>();
        for (BusinessApiClient.WalletOption wallet : walletOptions) {
            walletNames.add(wallet.displayName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, walletNames);
        spinnerWallet.setAdapter(adapter);
        spinnerWallet.setOnItemClickListener((parent, view, position, id) ->
                selectedWalletId = walletIdForDisplay(parent.getItemAtPosition(position).toString())
        );
        if (!walletOptions.isEmpty() && spinnerWallet.getText().toString().trim().isEmpty()) {
            BusinessApiClient.WalletOption wallet = walletOptions.get(0);
            selectedWalletId = wallet.id;
            spinnerWallet.setText(wallet.displayName(), false);
        }
    }

    private void setupAutomaticTimestampField() {
        etDate.setText(currentTimestampText());
        etDate.setFocusable(false);
        etDate.setClickable(false);
    }

    private void saveTransaction(MaterialButton saveButton) {
        double amount = Double.parseDouble(etAmount.getText().toString().trim());
        String partner = spinnerPartner.getText().toString().trim();
        String category = spinnerCategory.getText().toString().trim();
        String note = etNote.getText() == null ? "" : etNote.getText().toString().trim();
        boolean isIncome = typeToggleGroup.getCheckedButtonId() == R.id.btn_revenue;
        String timestamp = currentTimestampText();

        Context appContext = requireContext().getApplicationContext();
        new Thread(() -> {
            BusinessApiClient.ApiResult result = businessApiClient.createTransaction(
                    amount,
                    partner,
                    category,
                    timestamp,
                    note,
                    isIncome,
                    selectedWalletId
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
                    getParentFragmentManager().setFragmentResult(REQUEST_KEY, new Bundle());
                    closeForm();
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
        if (spinnerPartner.getText().toString().trim().isEmpty()) {
            spinnerPartner.setError("Vui lòng chọn đối tác/khách hàng");
            return false;
        }
        if (selectedWalletId <= 0) {
            selectedWalletId = walletIdForDisplay(spinnerWallet.getText().toString().trim());
        }
        if (selectedWalletId <= 0) {
            spinnerWallet.setError("Vui lòng chọn tài khoản nguồn");
            return false;
        }
        if (spinnerCategory.getText().toString().trim().isEmpty()) {
            spinnerCategory.setError("Vui lòng chọn danh mục");
            return false;
        }
        return true;
    }

    private int walletIdForDisplay(String display) {
        for (BusinessApiClient.WalletOption wallet : walletOptions) {
            if (wallet.displayName().equals(display) || wallet.name.equals(display)) {
                return wallet.id;
            }
        }
        return 0;
    }

    private String currentTimestampText() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());
    }

    private void closeForm() {
        if (getDialog() != null) {
            dismiss();
            return;
        }
        getParentFragmentManager().popBackStack();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() == null) {
            return;
        }
        Window window = getDialog().getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
