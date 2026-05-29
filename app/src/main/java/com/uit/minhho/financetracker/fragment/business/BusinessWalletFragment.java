package com.uit.minhho.financetracker.fragment.business;

import android.app.Activity;
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
import com.uit.minhho.financetracker.data.remote.BusinessApiClient;
import com.uit.minhho.financetracker.model.business.BusinessWallet;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BusinessWalletFragment extends Fragment {

    private final List<BusinessWallet> wallets = new ArrayList<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private BusinessApiClient apiClient;
    private BusinessWalletAdapter walletAdapter;

    public BusinessWalletFragment() {
        super(R.layout.fragment_business_wallet);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiClient = new BusinessApiClient(requireContext());
        RecyclerView recyclerView = view.findViewById(R.id.rv_business_wallets);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        walletAdapter = new BusinessWalletAdapter(wallets);
        recyclerView.setAdapter(walletAdapter);
        loadWallets();

        view.findViewById(R.id.btn_add_business_wallet).setOnClickListener(v ->
                openChildScreen(new BusinessAddWalletFragment())
        );
        view.findViewById(R.id.btn_back_business_wallet).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );
    }

    @Override
    public void onResume() {
        super.onResume();
        if (walletAdapter != null) {
            loadWallets();
        }
    }

    private void loadWallets() {
        executorService.execute(() -> {
            List<BusinessWallet> loadedWallets = apiClient.getWallets();
            Activity activity = getActivity();
            if (!isAdded() || activity == null) {
                return;
            }

            activity.runOnUiThread(() -> {
                if (!isAdded()) {
                    return;
                }
                wallets.clear();
                wallets.addAll(loadedWallets);
                walletAdapter.notifyDataSetChanged();
            });
        });
    }

    private void openChildScreen(Fragment fragment) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragment_container_business, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
