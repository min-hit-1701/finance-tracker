package com.uit.minhho.financetracker.fragment.personal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;
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

public class WalletFragment extends Fragment implements WalletAdapter.OnWalletClickListener {

    private List<Wallet> walletList;
    private WalletAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_personal_wallet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rv = view.findViewById(R.id.rv_wallets);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        walletList = new ArrayList<>();
        walletList.add(new Wallet("1", "Ví tiền mặt", 2500000, "Tiền mặt"));
        walletList.add(new Wallet("2", "Techcombank", 8200000, "Ngân hàng"));
        walletList.add(new Wallet("3", "MoMo", 1750000, "Ví điện tử"));

        adapter = new WalletAdapter(walletList, this);
        rv.setAdapter(adapter);

        // KẾT NỐI: Hiển thị dialog thêm ví từ file dialog_add_wallet.xml
        view.findViewById(R.id.btn_add_another_card).setOnClickListener(v -> showAddWalletDialog());
    }

    private void showAddWalletDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_personal_add_wallet, null);
        AutoCompleteTextView spinnerBank = dialogView.findViewById(R.id.spinner_bank_type);
        
        String[] banks = {"Vietcombank", "Techcombank", "MB Bank", "MoMo", "ZaloPay"};
        ArrayAdapter<String> bankAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, banks);
        spinnerBank.setAdapter(bankAdapter);

        new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setPositiveButton("Thêm ví", (dialog, which) -> {
                    // Xử lý thêm ví mới vào danh sách (Demo)
                    walletList.add(new Wallet(UUID.randomUUID().toString(), "Ví mới", 0, "Ngân hàng"));
                    adapter.notifyItemInserted(walletList.size() - 1);
                    Toast.makeText(getContext(), "Đã thêm ví mới", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onWalletClick(Wallet wallet) {
        Toast.makeText(getContext(), "Chi tiết ví: " + wallet.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onWalletLongClick(Wallet wallet) {
        Toast.makeText(getContext(), "Tùy chọn cho ví: " + wallet.getName(), Toast.LENGTH_SHORT).show();
    }
}