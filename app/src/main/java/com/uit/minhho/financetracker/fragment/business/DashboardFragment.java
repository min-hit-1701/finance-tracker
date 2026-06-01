package com.uit.minhho.financetracker.fragment.business;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.data.local.entity.Transaction;
import com.uit.minhho.financetracker.viewmodel.BusinessViewModel;

import java.util.List;
import java.util.Locale;

public class DashboardFragment extends Fragment {
    private BusinessViewModel businessViewModel;
    private TextView balanceText, incomeShortText, expenseShortText;
    private TextView totalTransactionsText, incomeCountText, expenseCountText;
    private TextView largestTransactionText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_business_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        businessViewModel = new ViewModelProvider(requireActivity()).get(BusinessViewModel.class);
        balanceText = view.findViewById(R.id.tv_business_total_balance);
        incomeShortText = view.findViewById(R.id.tv_business_income_short);
        expenseShortText = view.findViewById(R.id.tv_business_expense_short);
        totalTransactionsText = view.findViewById(R.id.tv_business_total_transactions);
        incomeCountText = view.findViewById(R.id.tv_business_income_count);
        expenseCountText = view.findViewById(R.id.tv_business_expense_count);
        largestTransactionText = view.findViewById(R.id.tv_business_largest_transaction);

        businessViewModel.getBusinessTotalBalance().observe(getViewLifecycleOwner(), b -> {
            if (b != null) balanceText.setText(formatMoney(b));
        });
        businessViewModel.getBusinessIncome().observe(getViewLifecycleOwner(), i -> {
            if (i != null) incomeShortText.setText("Thu: " + formatMoney(i));
        });
        businessViewModel.getBusinessExpense().observe(getViewLifecycleOwner(), e -> {
            if (e != null) expenseShortText.setText("Chi: " + formatMoney(e));
        });
        businessViewModel.getBusinessTransactions().observe(getViewLifecycleOwner(), this::renderStats);
    }

    private void renderStats(List<Transaction> transactions) {
        int incomeCount = 0, expenseCount = 0;
        double largest = 0;

        if (transactions != null) {
            for (Transaction tx : transactions) {
                if (tx.isIncome()) {
                    incomeCount++;
                } else {
                    expenseCount++;
                    largest = Math.max(largest, tx.getAmount());
                }
            }
        }
        totalTransactionsText.setText(String.valueOf(transactions != null ? transactions.size() : 0));
        incomeCountText.setText(String.valueOf(incomeCount));
        expenseCountText.setText(String.valueOf(expenseCount));
        largestTransactionText.setText(formatMoney(largest));
    }

    private String formatMoney(double amount) {
        return String.format(Locale.US, "%,.0f đ", amount);
    }
}
