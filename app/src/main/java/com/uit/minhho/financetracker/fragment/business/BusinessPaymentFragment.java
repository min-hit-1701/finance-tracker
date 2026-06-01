package com.uit.minhho.financetracker.fragment.business;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.viewmodel.BusinessViewModel;

public class BusinessPaymentFragment extends Fragment {

    private BusinessViewModel businessViewModel;

    public BusinessPaymentFragment() {
        super(R.layout.fragment_business_payment);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        businessViewModel = new ViewModelProvider(requireActivity()).get(BusinessViewModel.class);

        EditText receiverInput = view.findViewById(R.id.et_payment_receiver);
        EditText accountInput = view.findViewById(R.id.et_payment_account);
        EditText amountInput = view.findViewById(R.id.et_payment_amount);
        EditText noteInput = view.findViewById(R.id.et_payment_note);
        TextView summaryText = view.findViewById(R.id.tv_payment_summary);
        MaterialButton confirmButton = view.findViewById(R.id.btn_confirm_payment);
        MaterialButton backButton = view.findViewById(R.id.btn_back_from_payment);

        confirmButton.setOnClickListener(v -> {
            String receiver = safeText(receiverInput);
            String account = safeText(accountInput);
            String amountText = safeText(amountInput);
            String note = safeText(noteInput);

            if (TextUtils.isEmpty(receiver)) {
                Toast.makeText(requireContext(), R.string.business_payment_error_receiver, Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(account)) {
                Toast.makeText(requireContext(), R.string.business_payment_error_account, Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(amountText)) {
                Toast.makeText(requireContext(), R.string.business_payment_error_amount, Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(note)) {
                note = getString(R.string.business_payment_default_note);
            }

            double amount;
            try {
                amount = Double.parseDouble(amountText);
            } catch (NumberFormatException exception) {
                Toast.makeText(requireContext(), R.string.business_error_amount_invalid, Toast.LENGTH_SHORT).show();
                return;
            }

            if (amount <= 0) {
                Toast.makeText(requireContext(), R.string.business_error_amount_invalid, Toast.LENGTH_SHORT).show();
                return;
            }

            summaryText.setText(getString(
                    R.string.business_payment_summary_format,
                    receiver,
                    amountText,
                    account,
                    note
            ));

            businessViewModel.createBusinessPayment(receiver, account, amount, note, System.currentTimeMillis());
        });

        businessViewModel.getOperationMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.trim().isEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                businessViewModel.clearOperationMessage();
            }
        });

        backButton.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
    }

    private String safeText(EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }
}
