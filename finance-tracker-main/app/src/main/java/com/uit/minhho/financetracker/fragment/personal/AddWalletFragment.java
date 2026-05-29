package com.uit.minhho.financetracker.fragment.personal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.uit.minhho.financetracker.R;

public class AddWalletFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_personal_add_wallet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());

        setupBankSpinner(view);

        view.findViewById(R.id.btn_save_card).setOnClickListener(v -> {
            // Logic lưu ví mới
            getParentFragmentManager().popBackStack();
        });
    }

    private void setupBankSpinner(View view) {
        AutoCompleteTextView spinnerBank = view.findViewById(R.id.spinner_bank_type);
        String[] banks = {"Vietcombank", "Techcombank", "MB Bank", "MoMo", "ZaloPay", "Tiền mặt"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, banks);
        spinnerBank.setAdapter(adapter);
    }
}