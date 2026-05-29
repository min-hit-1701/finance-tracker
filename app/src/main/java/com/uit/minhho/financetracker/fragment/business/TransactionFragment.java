package com.uit.minhho.financetracker.fragment.business;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.adapter.business.BusinessTransactionAdapter;
import com.uit.minhho.financetracker.data.remote.BusinessApiClient;
import com.uit.minhho.financetracker.model.business.BusinessTransaction;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TransactionFragment extends Fragment {
    private final List<BusinessTransaction> transactions = new ArrayList<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private BusinessApiClient apiClient;
    private BusinessTransactionAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_business_transaction, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiClient = new BusinessApiClient(requireContext());
        RecyclerView recyclerView = view.findViewById(R.id.business_transaction_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new BusinessTransactionAdapter(transactions);
        recyclerView.setAdapter(adapter);
        loadTransactions();

        // KẾT NỐI: Mở màn hình Thêm giao dịch khi nhấn vào nút "Thêm giao dịch mới"
        view.findViewById(R.id.btn_add_transaction).setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, 
                                       android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                    .replace(R.id.fragment_container_business, new BusinessAddTransactionFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            loadTransactions();
        }
    }

    private void loadTransactions() {
        executorService.execute(() -> {
            List<BusinessTransaction> loadedTransactions = apiClient.getTransactions();
            Activity activity = getActivity();
            if (!isAdded() || activity == null) {
                return;
            }

            activity.runOnUiThread(() -> {
                if (!isAdded()) {
                    return;
                }
                transactions.clear();
                transactions.addAll(loadedTransactions);
                adapter.notifyDataSetChanged();
            });
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
