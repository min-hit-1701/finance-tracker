package com.uit.minhho.financetracker.fragment.personal;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.adapter.personal.WalletAdapter;
import com.uit.minhho.financetracker.model.personal.Wallet;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WalletFragment extends Fragment {

    private WalletAdapter adapter;
    private List<Wallet> walletList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_personal_wallet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        walletList = getInitialWallets();
        
        RecyclerView recyclerView = view.findViewById(R.id.rv_wallets);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        adapter = new WalletAdapter(walletList, position -> {
            walletList.remove(position);
            adapter.notifyItemRemoved(position);
            adapter.notifyItemRangeChanged(position, walletList.size());
        });
        
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.btn_add_another_card).setOnClickListener(v -> showAddCardDialog());
    }

    private void showAddCardDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_wallet, null);
        EditText etHolderName = dialogView.findViewById(R.id.et_holder_name);
        EditText etCardNumber = dialogView.findViewById(R.id.et_card_number);
        AutoCompleteTextView spinnerBank = dialogView.findViewById(R.id.spinner_bank_type);

        String[] banks = {"Vietcombank", "Techcombank", "MB Bank", "MoMo", "ZaloPay"};
        ArrayAdapter<String> bankAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, banks);
        spinnerBank.setAdapter(bankAdapter);

        new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setPositiveButton("Verify", (dialog, which) -> {
                    String name = etHolderName.getText().toString().trim();
                    String number = etCardNumber.getText().toString().trim();
                    String bank = spinnerBank.getText().toString();

                    if (!name.isEmpty() && !number.isEmpty()) {
                        addWallet(bank.isEmpty() ? name : bank, number);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addWallet(String bankName, String number) {
        Wallet newWallet = new Wallet(
                number, // Using card number as ID for now
                bankName,
                0.0, // Initial balance
                "Bank"
        );
        walletList.add(newWallet);
        adapter.notifyItemInserted(walletList.size() - 1);
    }

    private List<Wallet> getInitialWallets() {
        List<Wallet> list = new ArrayList<>();
        list.add(new Wallet("3884", "Vietcombank", 8500000, "Bank"));
        list.add(new Wallet("1234", "MoMo Wallet", 1450000, "E-Wallet"));
        return list;
    }
}
