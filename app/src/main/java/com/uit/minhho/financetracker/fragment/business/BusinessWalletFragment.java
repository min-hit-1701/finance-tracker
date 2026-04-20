package com.uit.minhho.financetracker.fragment.business;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.adapter.business.BusinessWalletAdapter;
import com.uit.minhho.financetracker.model.business.BusinessWallet;

import java.util.ArrayList;
import java.util.List;

public class BusinessWalletFragment extends Fragment {

    private final List<BusinessWallet> wallets = new ArrayList<>();
    private BusinessWalletAdapter walletAdapter;

    public BusinessWalletFragment() {
        super(R.layout.fragment_business_wallet);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getParentFragmentManager().setFragmentResultListener(
                BusinessAddWalletFragment.REQUEST_KEY,
                getViewLifecycleOwner(),
                (requestKey, result) -> {
                    String name = result.getString(BusinessAddWalletFragment.KEY_NAME, "");
                    String balance = result.getString(BusinessAddWalletFragment.KEY_BALANCE, "");
                    String note = result.getString(BusinessAddWalletFragment.KEY_NOTE, getString(R.string.business_note_default));

                    wallets.add(0, new BusinessWallet(
                            name,
                            getString(R.string.business_wallet_balance_format, balance),
                            note
                    ));
                    walletAdapter.notifyDataSetChanged();
                    Toast.makeText(requireContext(), R.string.business_wallet_save_success, Toast.LENGTH_SHORT).show();
                }
        );

        RecyclerView recyclerView = view.findViewById(R.id.rv_business_wallets);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        if (wallets.isEmpty()) {
            wallets.add(new BusinessWallet(
                    getString(R.string.business_wallet_main_cash),
                    getString(R.string.business_wallet_main_cash_amount),
                    getString(R.string.business_wallet_updated)
            ));
            wallets.add(new BusinessWallet(
                    getString(R.string.business_wallet_bank_account),
                    getString(R.string.business_wallet_bank_account_amount),
                    getString(R.string.business_wallet_updated)
            ));
            wallets.add(new BusinessWallet(
                    getString(R.string.business_wallet_e_wallet),
                    getString(R.string.business_wallet_e_wallet_amount),
                    getString(R.string.business_wallet_updated)
            ));
        }

        walletAdapter = new BusinessWalletAdapter(wallets);
        recyclerView.setAdapter(walletAdapter);

        view.findViewById(R.id.btn_add_business_wallet).setOnClickListener(v ->
                openChildScreen(new BusinessAddWalletFragment())
        );
        view.findViewById(R.id.btn_back_business_wallet).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );
    }

    private void openChildScreen(Fragment fragment) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragment_container_business, fragment)
                .addToBackStack(null)
                .commit();
    }
}
