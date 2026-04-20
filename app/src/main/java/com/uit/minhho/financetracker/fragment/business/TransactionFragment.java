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
                getString(R.string.business_tx_rent),
                getString(R.string.business_tx_rent_sub),
                getString(R.string.business_tx_minus_4m),
                false
        ));
        list.add(new BusinessTransaction(
                getString(R.string.business_tx_client),
                getString(R.string.business_tx_client_sub),
                getString(R.string.business_tx_plus_8_5m),
                true
        ));
        list.add(new BusinessTransaction(
                getString(R.string.business_tx_advert),
                getString(R.string.business_tx_advert_sub),
                getString(R.string.business_tx_minus_1_8m),
                false
        ));
        list.add(new BusinessTransaction(
                getString(R.string.business_tx_inventory),
                getString(R.string.business_tx_inventory_sub),
                getString(R.string.business_tx_minus_12_5m),
                false
        ));
        list.add(new BusinessTransaction(
                getString(R.string.business_tx_payroll),
                getString(R.string.business_tx_payroll_sub),
                getString(R.string.business_tx_minus_6_2m),
                false
        ));
        list.add(new BusinessTransaction(
                getString(R.string.business_tx_client_payment),
                getString(R.string.business_tx_client_payment_sub),
                getString(R.string.business_tx_plus_3_2m),
                true
        ));

        return list;
    }
}
