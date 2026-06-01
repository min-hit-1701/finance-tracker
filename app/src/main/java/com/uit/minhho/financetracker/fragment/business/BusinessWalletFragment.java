package com.uit.minhho.financetracker.fragment.business;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.data.local.entity.Wallet;
import com.uit.minhho.financetracker.viewmodel.BusinessViewModel;

import java.text.DecimalFormat;
import java.util.List;

public class BusinessWalletFragment extends Fragment {

    private BusinessViewModel businessViewModel;
    private LinearLayout itemsContainer;
    private final DecimalFormat moneyFormatter = new DecimalFormat("#,###");

    public BusinessWalletFragment() {
        super(R.layout.fragment_business_wallet);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        businessViewModel = new ViewModelProvider(requireActivity()).get(BusinessViewModel.class);
        itemsContainer = view.findViewById(R.id.wallet_items_container);

        businessViewModel.getBusinessWallets().observe(getViewLifecycleOwner(), this::renderWallets);

        view.findViewById(R.id.btn_add_business_wallet).setOnClickListener(v ->
                openChildScreen(new BusinessAddWalletFragment())
        );
        view.findViewById(R.id.btn_back_business_wallet).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );
    }

    private void renderWallets(List<Wallet> wallets) {
        itemsContainer.removeAllViews();
        if (wallets == null || wallets.isEmpty()) return;
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (Wallet w : wallets) {
            View itemView = inflater.inflate(R.layout.item_business_wallet, itemsContainer, false);
            TextView nameText = itemView.findViewById(R.id.tv_business_wallet_name);
            TextView balanceText = itemView.findViewById(R.id.tv_business_wallet_balance);
            nameText.setText(w.getName());
            balanceText.setText(getString(R.string.business_wallet_balance_format, moneyFormatter.format(w.getBalance())));
            itemsContainer.addView(itemView);
        }
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
