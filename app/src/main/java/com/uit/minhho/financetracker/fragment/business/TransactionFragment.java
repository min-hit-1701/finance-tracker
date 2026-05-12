package com.uit.minhho.financetracker.fragment.business;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.adapter.business.BusinessTransactionAdapter;
import com.uit.minhho.financetracker.viewmodel.BusinessViewModel;

import java.util.ArrayList;

public class TransactionFragment extends Fragment {

    private BusinessTransactionAdapter transactionAdapter;
    private BusinessViewModel businessViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_business_transaction, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        businessViewModel = new ViewModelProvider(requireActivity()).get(BusinessViewModel.class);

        RecyclerView recyclerView = view.findViewById(R.id.business_transaction_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        transactionAdapter = new BusinessTransactionAdapter(new ArrayList<>());
        recyclerView.setAdapter(transactionAdapter);

        businessViewModel.getBusinessTransactions().observe(getViewLifecycleOwner(), transactions -> {
            transactionAdapter.submitItems(transactions);
            View emptyView = view.findViewById(R.id.tv_business_transaction_empty);
            if (emptyView != null) {
                emptyView.setVisibility(transactions == null || transactions.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });

        view.findViewById(R.id.btn_add_transaction).setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right,
                            android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                    .replace(R.id.fragment_container_business, new BusinessAddTransactionFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }
}
