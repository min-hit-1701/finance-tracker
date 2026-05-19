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

public class BusinessAddWalletFragment extends Fragment {

    public static final String REQUEST_KEY = "request_add_business_wallet";
    public static final String KEY_NAME = "wallet_name";
    public static final String KEY_BALANCE = "wallet_balance";
    public static final String KEY_NOTE = "wallet_note";

    public BusinessAddWalletFragment() {
        super(R.layout.fragment_business_add_wallet);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextInputLayout nameLayout = view.findViewById(R.id.til_add_wallet_name);
        TextInputLayout balanceLayout = view.findViewById(R.id.til_add_wallet_balance);
        EditText nameInput = view.findViewById(R.id.et_add_wallet_name);
        EditText balanceInput = view.findViewById(R.id.et_add_wallet_balance);
        EditText noteInput = view.findViewById(R.id.et_add_wallet_note);

        view.findViewById(R.id.btn_save_new_wallet).setOnClickListener(v -> {
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
            if (TextUtils.isEmpty(note)) {
                note = getString(R.string.business_note_default);
            }

            Bundle result = new Bundle();
            result.putString(KEY_NAME, name);
            result.putString(KEY_BALANCE, balance);
            result.putString(KEY_NOTE, note);
            getParentFragmentManager().setFragmentResult(REQUEST_KEY, result);
            getParentFragmentManager().popBackStack();
        });

        view.findViewById(R.id.btn_cancel_new_wallet).setOnClickListener(v ->
                getParentFragmentManager().popBackStack()
        );
    }

    private String safeText(EditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }
}
