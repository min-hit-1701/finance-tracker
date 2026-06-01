package com.uit.minhho.financetracker.fragment.personal;

import android.app.AlertDialog;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.adapter.personal.WalletAdapter;
import com.uit.minhho.financetracker.data.local.entity.Wallet;
import com.uit.minhho.financetracker.viewmodel.WalletViewModel;
import java.util.ArrayList;
import java.util.List;

public class WalletFragment extends Fragment implements WalletAdapter.OnWalletClickListener {

    private List<Wallet> walletList;
    private WalletAdapter adapter;
    private WalletViewModel walletViewModel;

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
        adapter = new WalletAdapter(walletList, this);
        rv.setAdapter(adapter);

        walletViewModel = new ViewModelProvider(requireActivity()).get(WalletViewModel.class);
        walletViewModel.getPersonalWallets().observe(getViewLifecycleOwner(), wallets -> adapter.setWallets(wallets));

        // CẬP NHẬT: Mở AddWalletFragment thay vì hiện Dialog
        view.findViewById(R.id.btn_add_another_card).setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, 
                                       android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                    .replace(R.id.fragment_container, new AddWalletFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (walletViewModel != null) {
            walletViewModel.refreshPersonalWallets();
        }
    }

    @Override
    public void onWalletClick(Wallet wallet) {
        Toast.makeText(getContext(), "Chi tiết ví: " + wallet.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onWalletLongClick(Wallet wallet) {
        Context appContext = requireContext().getApplicationContext();
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa ví")
                .setMessage("Bạn có chắc muốn xóa \"" + wallet.getName() + "\" không?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (dialog, which) -> {
                    walletViewModel.delete(wallet, (success, message) -> {
                        Activity activity = getActivity();
                        if (!isAdded() || activity == null) {
                            return;
                        }

                        activity.runOnUiThread(() ->
                                Toast.makeText(appContext, message, Toast.LENGTH_SHORT).show()
                        );
                    });
                })
                .show();
    }
}
