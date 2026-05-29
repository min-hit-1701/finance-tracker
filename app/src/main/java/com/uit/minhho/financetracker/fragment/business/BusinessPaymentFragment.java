package com.uit.minhho.financetracker.fragment.business;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.data.remote.BusinessApiClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BusinessPaymentFragment extends Fragment {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private BusinessApiClient apiClient;

    public BusinessPaymentFragment() {
        super(R.layout.fragment_business_payment);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiClient = new BusinessApiClient(requireContext());
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
            String amount = safeText(amountInput);
            String note = safeText(noteInput);

            if (TextUtils.isEmpty(receiver)) {
                Toast.makeText(requireContext(), R.string.business_payment_error_receiver, Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(account)) {
                Toast.makeText(requireContext(), R.string.business_payment_error_account, Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(amount)) {
                Toast.makeText(requireContext(), R.string.business_payment_error_amount, Toast.LENGTH_SHORT).show();
                return;
            }
            double parsedAmount;
            try {
                parsedAmount = Double.parseDouble(amount.replace(",", "").replace(" ", ""));
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), R.string.business_payment_error_amount, Toast.LENGTH_SHORT).show();
                return;
            }
            if (parsedAmount <= 0) {
                Toast.makeText(requireContext(), R.string.business_payment_error_amount, Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(note)) {
                note = getString(R.string.business_payment_default_note);
            }

            confirmButton.setEnabled(false);
            String finalNote = note;
            Context appContext = requireContext().getApplicationContext();
            executorService.execute(() -> {
                BusinessApiClient.ApiResult result = apiClient.createPayment(receiver, account, parsedAmount, finalNote);
                Activity activity = getActivity();
                if (!isAdded() || activity == null) {
                    return;
                }

                activity.runOnUiThread(() -> {
                    if (!isAdded()) {
                        return;
                    }
                    confirmButton.setEnabled(true);
                    Toast.makeText(appContext, result.message, Toast.LENGTH_SHORT).show();

                    if (!result.success) {
                        return;
                    }

                    summaryText.setText(getString(
                            R.string.business_payment_summary_format,
                            receiver,
                            amount,
                            account,
                            finalNote
                    ));
                    receiverInput.setText("");
                    accountInput.setText("");
                    amountInput.setText("");
                    noteInput.setText("");
                });
            });
        });

        backButton.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
    }

    private String safeText(EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
