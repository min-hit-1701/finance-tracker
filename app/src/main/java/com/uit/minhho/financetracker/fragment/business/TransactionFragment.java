package com.uit.minhho.financetracker.fragment.business;

import android.app.AlertDialog;
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
import com.uit.minhho.financetracker.adapter.business.BusinessTransactionAdapter;
import com.uit.minhho.financetracker.data.local.entity.Transaction;
import com.uit.minhho.financetracker.model.business.BusinessTransaction;
import com.uit.minhho.financetracker.viewmodel.BusinessViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionFragment extends Fragment {
    private final List<BusinessTransaction> transactions = new ArrayList<>();
    private BusinessTransactionAdapter adapter;
    private BusinessViewModel businessViewModel;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

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
        adapter = new BusinessTransactionAdapter(transactions, this::confirmDeleteTransaction);
        recyclerView.setAdapter(adapter);

        businessViewModel.getBusinessTransactions().observe(getViewLifecycleOwner(), this::onTransactionsLoaded);

        view.findViewById(R.id.btn_add_transaction).setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right,
                                        android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                    .replace(R.id.fragment_container_business, new BusinessAddTransactionFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void onTransactionsLoaded(List<Transaction> list) {
        transactions.clear();
        if (list != null) {
            for (Transaction tx : list) {
                String title = tx.getNote();
                if (title == null || title.isEmpty()) title = "Giao dịch doanh nghiệp";
                String displayTime = dateFormat.format(new Date(tx.getTimestamp()));
                String amount = (tx.isIncome() ? "+" : "-") + String.format(Locale.getDefault(), "%,.0f đ", tx.getAmount());
                transactions.add(new BusinessTransaction(
                        tx.getId(),
                        title,
                        displayTime,
                        amount,
                        tx.isIncome(),
                        tx.getAmount(),
                        tx.getTimestamp()
                ));
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void confirmDeleteTransaction(BusinessTransaction transaction) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_transaction_title)
                .setMessage(R.string.delete_transaction_message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_delete, (dialog, which) -> deleteTransaction(transaction))
                .show();
    }

    private void deleteTransaction(BusinessTransaction transaction) {
        new Thread(() -> {
            Transaction tx = new Transaction(transaction.getRawAmount(), transaction.getTimestamp(),
                    transaction.getTitle(), 0, 0, transaction.isIncome(), true, 0);
            tx.setId(transaction.getId());
            businessViewModel.deleteBusinessTransaction(tx);
        }).start();
    }
}
