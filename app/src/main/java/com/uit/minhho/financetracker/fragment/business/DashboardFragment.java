package com.uit.minhho.financetracker.fragment.business;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.data.remote.BusinessApiClient;
import com.uit.minhho.financetracker.model.business.BusinessTransaction;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DashboardFragment extends Fragment {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private BusinessApiClient apiClient;
    private TextView balanceText;
    private TextView incomeShortText;
    private TextView expenseShortText;
    private TextView totalTransactionsText;
    private TextView incomeCountText;
    private TextView expenseCountText;
    private TextView largestTransactionText;
    private TextView avgPerDayText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_business_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiClient = new BusinessApiClient(requireContext());
        balanceText = view.findViewById(R.id.tv_business_total_balance);
        incomeShortText = view.findViewById(R.id.tv_business_income_short);
        expenseShortText = view.findViewById(R.id.tv_business_expense_short);
        totalTransactionsText = view.findViewById(R.id.tv_business_total_transactions);
        incomeCountText = view.findViewById(R.id.tv_business_income_count);
        expenseCountText = view.findViewById(R.id.tv_business_expense_count);
        largestTransactionText = view.findViewById(R.id.tv_business_largest_transaction);
        avgPerDayText = view.findViewById(R.id.tv_business_avg_per_day);
        loadDashboardData();

        View paymentButton = view.findViewById(R.id.btn_send_business_payment);

        paymentButton.setOnClickListener(v -> openChildScreen(new BusinessPaymentFragment()));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (apiClient != null) {
            loadDashboardData();
        }
    }

    private void loadDashboardData() {
        executorService.execute(() -> {
            BusinessApiClient.ApiResult<BusinessApiClient.Summary> summary = apiClient.getSummary();
            List<BusinessTransaction> transactions = apiClient.getTransactions();
            Activity activity = getActivity();
            if (!isAdded() || activity == null) {
                return;
            }

            activity.runOnUiThread(() -> {
                if (!isAdded()) {
                    return;
                }
                if (summary.success && summary.data != null) {
                    double income = summary.data.totalIncome;
                    double expense = summary.data.totalExpense;
                    balanceText.setText(formatMoney(summary.data.totalBalance));
                    incomeShortText.setText("Thu: " + formatMoney(income));
                    expenseShortText.setText("Chi: " + formatMoney(expense));
                }

                renderActivityStats(transactions);
            });
        });
    }

    private void renderActivityStats(List<BusinessTransaction> transactions) {
        int incomeCount = 0;
        int expenseCount = 0;
        double largest = 0.0;
        double totalAmount = 0.0;
        Set<Long> activeDays = new HashSet<>();

        for (BusinessTransaction transaction : transactions) {
            if (transaction.isIncome()) {
                incomeCount++;
            } else {
                expenseCount++;
            }
            double amount = transaction.getRawAmount();
            largest = Math.max(largest, amount);
            totalAmount += amount;
            if (transaction.getTimestamp() > 0L) {
                activeDays.add(transaction.getTimestamp() / 86_400_000L);
            }
        }

        int dayCount = activeDays.isEmpty() ? 1 : activeDays.size();
        double averagePerDay = transactions.isEmpty() ? 0.0 : totalAmount / dayCount;

        totalTransactionsText.setText(String.valueOf(transactions.size()));
        incomeCountText.setText(String.valueOf(incomeCount));
        expenseCountText.setText(String.valueOf(expenseCount));
        largestTransactionText.setText(formatMoney(largest));
        avgPerDayText.setText(formatMoney(averagePerDay));
    }

    private String formatMoney(double amount) {
        return String.format(Locale.US, "%,.0f đ", amount);
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
