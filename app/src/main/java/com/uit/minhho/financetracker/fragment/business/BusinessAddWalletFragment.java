package com.uit.minhho.financetracker.fragment.business;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
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

public class BusinessAddWalletFragment extends Fragment {

    public static final String REQUEST_KEY = "request_add_business_wallet";
    public static final String KEY_NAME = "wallet_name";
    public static final String KEY_BALANCE = "wallet_balance";
    public static final String KEY_NOTE = "wallet_note";
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private BusinessApiClient apiClient;

    public BusinessAddWalletFragment() {
        super(R.layout.fragment_business_add_wallet);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiClient = new BusinessApiClient(requireContext());
        TextInputLayout nameLayout = view.findViewById(R.id.til_add_wallet_name);
        TextInputLayout balanceLayout = view.findViewById(R.id.til_add_wallet_balance);
        EditText nameInput = view.findViewById(R.id.et_add_wallet_name);
        EditText balanceInput = view.findViewById(R.id.et_add_wallet_balance);
        EditText noteInput = view.findViewById(R.id.et_add_wallet_note);
        MaterialButton saveButton = view.findViewById(R.id.btn_save_new_wallet);

        saveButton.setOnClickListener(v -> {
            nameLayout.setError(null);
            balanceLayout.setError(null);

            String name = safeText(nameInput);
            String balance = safeText(balanceInput);
            String note = safeText(noteInput);

            if (TextUtils.isEmpty(name)) {
                nameLayout.setError(getString(R.string.business_wallet_error_name));
                return;
            }
            if (TextUtils.isEmpty(balance)) {
                balanceLayout.setError(getString(R.string.business_wallet_error_balance));
                return;
            }
            double parsedBalance;
            try {
                parsedBalance = Double.parseDouble(balance.replace(",", "").replace(" ", ""));
            } catch (NumberFormatException e) {
                balanceLayout.setError(getString(R.string.business_wallet_error_balance));
                return;
            }
            if (TextUtils.isEmpty(note)) {
                note = getString(R.string.business_note_default);
            }

            saveButton.setEnabled(false);
            String finalNote = note;
            Context appContext = requireContext().getApplicationContext();
            executorService.execute(() -> {
                BusinessApiClient.ApiResult result = apiClient.createWallet(name, parsedBalance, finalNote);
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

        view.findViewById(R.id.btn_cancel_new_wallet).setOnClickListener(v ->
                getParentFragmentManager().popBackStack()
        );
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
