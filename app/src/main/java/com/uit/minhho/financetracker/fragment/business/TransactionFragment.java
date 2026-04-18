package com.uit.minhho.financetracker.fragment.business;

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
import com.uit.minhho.financetracker.model.business.BusinessTransaction;

import java.util.ArrayList;
import java.util.List;

public class TransactionFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_business_transaction, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView recyclerView = view.findViewById(R.id.business_transaction_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(new BusinessTransactionAdapter(fakeTransactions()));
    }

    private List<BusinessTransaction> fakeTransactions() {
        List<BusinessTransaction> list = new ArrayList<>();
        list.add(new BusinessTransaction(
                getString(R.string.fake_tx_1_title),
                getString(R.string.fake_tx_1_subtitle),
                "-250,000",
                false
        ));
        list.add(new BusinessTransaction(
                getString(R.string.fake_tx_2_title),
                getString(R.string.fake_tx_2_subtitle),
                "+6,500,000",
                true
        ));
        list.add(new BusinessTransaction(
                getString(R.string.fake_tx_3_title),
                getString(R.string.fake_tx_3_subtitle),
                "-780,000",
                false
        ));
        list.add(new BusinessTransaction(
                getString(R.string.fake_tx_4_title),
                getString(R.string.fake_tx_4_subtitle),
                "+3,200,000",
                true
        ));
        return list;
    }
}
