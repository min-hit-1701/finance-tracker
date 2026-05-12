package com.uit.minhho.financetracker.fragment.business;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.adapter.business.BusinessWalletAdapter;
import com.uit.minhho.financetracker.viewmodel.BusinessViewModel;

import java.util.ArrayList;

public class BusinessWalletFragment extends Fragment {

    private BusinessWalletAdapter walletAdapter;
    private BusinessViewModel businessViewModel;

    public BusinessWalletFragment() {
        super(R.layout.fragment_business_wallet);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        businessViewModel = new ViewModelProvider(requireActivity()).get(BusinessViewModel.class);

        RecyclerView recyclerView = view.findViewById(R.id.rv_business_wallets);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        walletAdapter = new BusinessWalletAdapter(new ArrayList<>());
        recyclerView.setAdapter(walletAdapter);

        businessViewModel.getBusinessWallets().observe(getViewLifecycleOwner(), wallets -> {
            walletAdapter.submitItems(wallets);
            View emptyView = view.findViewById(R.id.tv_business_wallet_empty);
            if (emptyView != null) {
                emptyView.setVisibility(wallets == null || wallets.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });

        getParentFragmentManager().setFragmentResultListener(
                BusinessAddWalletFragment.REQUEST_KEY,
                getViewLifecycleOwner(),
                (requestKey, result) -> {
                    String name = result.getString(BusinessAddWalletFragment.KEY_NAME, "");
                    String balanceText = result.getString(BusinessAddWalletFragment.KEY_BALANCE, "0");
                    String note = result.getString(BusinessAddWalletFragment.KEY_NOTE, getString(R.string.business_note_default));

                    double balance;
                    try {
                        balance = Double.parseDouble(balanceText);
                    } catch (NumberFormatException exception) {
                        Toast.makeText(requireContext(), R.string.business_wallet_error_balance, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    businessViewModel.addBusinessWallet(name, balance, getString(R.string.business_wallet_type_default), note);
                }
        );

        businessViewModel.getOperationMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.trim().isEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                businessViewModel.clearOperationMessage();
            }
        });

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
