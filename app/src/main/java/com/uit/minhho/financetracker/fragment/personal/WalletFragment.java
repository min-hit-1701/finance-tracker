package com.uit.minhho.financetracker.fragment.personal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.adapter.personal.WalletAdapter;
import com.uit.minhho.financetracker.model.personal.Wallet;
import java.util.ArrayList;
import java.util.List;

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

        view.findViewById(R.id.btn_add_another_card).setOnClickListener(v -> {
            // Demo add wallet behavior
            Toast.makeText(getContext(), "Tính năng thêm ví đang được phát triển", Toast.LENGTH_SHORT).show();
        });
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