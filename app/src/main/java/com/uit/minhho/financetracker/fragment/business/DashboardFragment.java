package com.uit.minhho.financetracker.fragment.business;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DashboardFragment extends Fragment {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private BusinessApiClient apiClient;
    private TextView balanceText;
    private TextView incomeShortText;
    private TextView expenseShortText;
    private TextView revenueText;
    private TextView expenseText;
    private TextView profitText;
    private final List<BusinessTransaction> recentTransactions = new ArrayList<>();
    private BusinessTransactionAdapter transactionAdapter;

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
        revenueText = view.findViewById(R.id.tv_business_revenue_value);
        expenseText = view.findViewById(R.id.tv_business_expense_value);
        profitText = view.findViewById(R.id.tv_business_profit_value);
        setupRecentTransactions(view);
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

    private void setupRecentTransactions(View view) {
        LinearLayout container = view.findViewById(R.id.business_recent_container);
        if (container.getChildCount() > 1) {
            container.removeViews(1, container.getChildCount() - 1);
        }

        RecyclerView recyclerView = new RecyclerView(requireContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setNestedScrollingEnabled(false);
        transactionAdapter = new BusinessTransactionAdapter(recentTransactions);
        recyclerView.setAdapter(transactionAdapter);
        container.addView(recyclerView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
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
                    double profit = income - expense;
                    balanceText.setText(formatMoney(summary.data.totalBalance));
                    incomeShortText.setText("Thu: " + formatMoney(income));
                    expenseShortText.setText("Chi: " + formatMoney(expense));
                    revenueText.setText(formatMoney(income));
                    expenseText.setText(formatMoney(expense));
                    profitText.setText(formatMoney(profit));
                }

                recentTransactions.clear();
                recentTransactions.addAll(transactions);
                transactionAdapter.notifyDataSetChanged();
            });
        });
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
