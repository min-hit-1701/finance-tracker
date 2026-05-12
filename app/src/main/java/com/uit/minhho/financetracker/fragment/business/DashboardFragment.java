package com.uit.minhho.financetracker.fragment.business;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.data.local.entity.Transaction;
import com.uit.minhho.financetracker.viewmodel.BusinessViewModel;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    private static final int MONTH_COUNT = 6;

    private final DecimalFormat amountFormatter = new DecimalFormat("#,###");
    private final SimpleDateFormat subtitleDateFormatter = new SimpleDateFormat("dd/MM", Locale.getDefault());

    private BusinessViewModel businessViewModel;

    private TextView totalBalance;
    private TextView incomeShort;
    private TextView expenseShort;
    private TextView revenueValue;
    private TextView expenseValue;
    private TextView profitValue;
    private BarChart monthlyChart;
    private TextView recentEmpty;

    private LinearLayout recentLayout1;
    private LinearLayout recentLayout2;
    private LinearLayout recentLayout3;
    private TextView recentTitle1;
    private TextView recentTitle2;
    private TextView recentTitle3;
    private TextView recentSubtitle1;
    private TextView recentSubtitle2;
    private TextView recentSubtitle3;
    private TextView recentAmount1;
    private TextView recentAmount2;
    private TextView recentAmount3;

    private LiveData<Double> monthIncomeLiveData;
    private LiveData<Double> monthExpenseLiveData;

    private final double[] monthIncome = new double[MONTH_COUNT];
    private final double[] monthExpense = new double[MONTH_COUNT];

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_business_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        businessViewModel = new ViewModelProvider(requireActivity()).get(BusinessViewModel.class);

        bindViews(view);
        bindOverview();
        bindRecentTransactions();
        bindMonthlyCashFlowChart();

        View paymentButton = view.findViewById(R.id.btn_send_business_payment);
        paymentButton.setOnClickListener(v -> openChildScreen(new BusinessPaymentFragment()));

        view.findViewById(R.id.btn_open_all_transactions).setOnClickListener(v ->
                openChildScreen(new TransactionFragment())
        );
    }

    private void bindViews(View view) {
        totalBalance = view.findViewById(R.id.tv_business_total_balance_value);
        incomeShort = view.findViewById(R.id.tv_business_income_short);
        expenseShort = view.findViewById(R.id.tv_business_expense_short);
        revenueValue = view.findViewById(R.id.tv_business_revenue_value);
        expenseValue = view.findViewById(R.id.tv_business_expense_value);
        profitValue = view.findViewById(R.id.tv_business_profit_value);
        monthlyChart = view.findViewById(R.id.chart_business_monthly_cash_flow);
        recentEmpty = view.findViewById(R.id.tv_business_recent_empty);

        recentLayout1 = view.findViewById(R.id.layout_business_recent_1);
        recentLayout2 = view.findViewById(R.id.layout_business_recent_2);
        recentLayout3 = view.findViewById(R.id.layout_business_recent_3);
        recentTitle1 = view.findViewById(R.id.tv_business_recent_title_1);
        recentTitle2 = view.findViewById(R.id.tv_business_recent_title_2);
        recentTitle3 = view.findViewById(R.id.tv_business_recent_title_3);
        recentSubtitle1 = view.findViewById(R.id.tv_business_recent_subtitle_1);
        recentSubtitle2 = view.findViewById(R.id.tv_business_recent_subtitle_2);
        recentSubtitle3 = view.findViewById(R.id.tv_business_recent_subtitle_3);
        recentAmount1 = view.findViewById(R.id.tv_business_recent_amount_1);
        recentAmount2 = view.findViewById(R.id.tv_business_recent_amount_2);
        recentAmount3 = view.findViewById(R.id.tv_business_recent_amount_3);
    }

    private void bindOverview() {
        final double[] incomeHolder = {0};
        final double[] expenseHolder = {0};

        businessViewModel.getBusinessTotalBalance().observe(getViewLifecycleOwner(), balance -> {
            double safeBalance = balance == null ? 0 : balance;
            totalBalance.setText(formatMoney(safeBalance));
        });

        businessViewModel.getBusinessIncome().observe(getViewLifecycleOwner(), income -> {
            incomeHolder[0] = income == null ? 0 : income;
            incomeShort.setText(getString(R.string.business_income_short_dynamic, formatCompact(incomeHolder[0])));
            revenueValue.setText(formatMoney(incomeHolder[0]));
            profitValue.setText(formatMoney(incomeHolder[0] - expenseHolder[0]));
        });

        businessViewModel.getBusinessExpense().observe(getViewLifecycleOwner(), expense -> {
            expenseHolder[0] = expense == null ? 0 : expense;
            expenseShort.setText(getString(R.string.business_expense_short_dynamic, formatCompact(expenseHolder[0])));
            expenseValue.setText(formatMoney(expenseHolder[0]));
            profitValue.setText(formatMoney(incomeHolder[0] - expenseHolder[0]));
        });
    }

    private void bindRecentTransactions() {
        businessViewModel.getRecentBusinessTransactions(3).observe(getViewLifecycleOwner(), transactions -> {
            if (transactions == null || transactions.isEmpty()) {
                recentLayout1.setVisibility(View.GONE);
                recentLayout2.setVisibility(View.GONE);
                recentLayout3.setVisibility(View.GONE);
                recentEmpty.setVisibility(View.VISIBLE);
                return;
            }
            recentEmpty.setVisibility(View.GONE);
            bindRecentItem(transactions, 0, recentLayout1, recentTitle1, recentSubtitle1, recentAmount1);
            bindRecentItem(transactions, 1, recentLayout2, recentTitle2, recentSubtitle2, recentAmount2);
            bindRecentItem(transactions, 2, recentLayout3, recentTitle3, recentSubtitle3, recentAmount3);
        });
    }

    private void bindRecentItem(
            List<Transaction> transactions,
            int index,
            LinearLayout container,
            TextView title,
            TextView subtitle,
            TextView amount
    ) {
        if (index >= transactions.size()) {
            container.setVisibility(View.GONE);
            return;
        }

        Transaction transaction = transactions.get(index);
        container.setVisibility(View.VISIBLE);
        title.setText(resolveTitle(transaction));
        subtitle.setText(resolveSubtitle(transaction));
        amount.setText(formatSignedAmount(transaction));
        amount.setTextColor(requireContext().getColor(
                transaction.isIncome() ? R.color.income_green : R.color.expense_red
        ));
    }

    private String resolveTitle(Transaction transaction) {
        String note = transaction.getNote();
        if (note == null || note.trim().isEmpty()) {
            return transaction.isIncome()
                    ? getString(R.string.business_tx_default_income)
                    : getString(R.string.business_tx_default_expense);
        }
        String[] parts = note.split("\\|");
        if (parts.length == 0 || parts[0].trim().isEmpty()) {
            return transaction.isIncome()
                    ? getString(R.string.business_tx_default_income)
                    : getString(R.string.business_tx_default_expense);
        }
        return parts[0].trim();
    }

    private String resolveSubtitle(Transaction transaction) {
        String dateText = subtitleDateFormatter.format(new Date(transaction.getTimestamp()));
        String note = transaction.getNote();
        if (note == null || note.trim().isEmpty()) {
            return getString(R.string.business_tx_subtitle_date_only, dateText);
        }
        String[] parts = note.split("\\|");
        if (parts.length > 1 && !parts[1].trim().isEmpty()) {
            return getString(R.string.business_tx_subtitle_partner_date, parts[1].trim(), dateText);
        }
        return getString(R.string.business_tx_subtitle_date_only, dateText);
    }

    private String formatSignedAmount(Transaction transaction) {
        String sign = transaction.isIncome() ? "+" : "-";
        return sign + amountFormatter.format(transaction.getAmount()) + " đ";
    }

    private void bindMonthlyCashFlowChart() {
        setupMonthlyChartStyle();
        for (int i = 0; i < MONTH_COUNT; i++) {
            observeMonth(i);
        }
    }

    private void observeMonth(int index) {
        long[] range = buildMonthRange(index);
        long from = range[0];
        long to = range[1];

        LiveData<Double> incomeLiveData = businessViewModel.getBusinessIncomeByPeriod(from, to);
        LiveData<Double> expenseLiveData = businessViewModel.getBusinessExpenseByPeriod(from, to);

        incomeLiveData.observe(getViewLifecycleOwner(), value -> {
            monthIncome[index] = value == null ? 0 : value;
            renderMonthlyChart();
        });

        expenseLiveData.observe(getViewLifecycleOwner(), value -> {
            monthExpense[index] = value == null ? 0 : value;
            renderMonthlyChart();
        });
    }

    private long[] buildMonthRange(int indexFromOldest) {
        Calendar start = Calendar.getInstance();
        start.set(Calendar.DAY_OF_MONTH, 1);
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);

        int monthOffset = indexFromOldest - (MONTH_COUNT - 1);
        start.add(Calendar.MONTH, monthOffset);

        Calendar end = (Calendar) start.clone();
        end.add(Calendar.MONTH, 1);
        end.add(Calendar.MILLISECOND, -1);

        return new long[]{start.getTimeInMillis(), end.getTimeInMillis()};
    }

    private void setupMonthlyChartStyle() {
        monthlyChart.getDescription().setEnabled(false);
        monthlyChart.setTouchEnabled(false);
        monthlyChart.setDragEnabled(false);
        monthlyChart.setScaleEnabled(false);
        monthlyChart.setPinchZoom(false);
        monthlyChart.setDrawGridBackground(false);
        monthlyChart.setDrawBarShadow(false);

        XAxis xAxis = monthlyChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(requireContext().getColor(R.color.text_secondary));

        monthlyChart.getAxisLeft().setDrawGridLines(false);
        monthlyChart.getAxisLeft().setAxisMinimum(0f);
        monthlyChart.getAxisLeft().setTextColor(requireContext().getColor(R.color.text_secondary));

        monthlyChart.getAxisRight().setEnabled(false);

        Legend legend = monthlyChart.getLegend();
        legend.setEnabled(true);
        legend.setTextColor(requireContext().getColor(R.color.text_secondary));
        legend.setFormSize(10f);
    }

    private void renderMonthlyChart() {
        List<BarEntry> incomeEntries = new ArrayList<>();
        List<BarEntry> expenseEntries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < MONTH_COUNT; i++) {
            incomeEntries.add(new BarEntry(i, (float) monthIncome[i]));
            expenseEntries.add(new BarEntry(i, (float) monthExpense[i]));
            labels.add(buildMonthLabel(i));
        }

        BarDataSet incomeSet = new BarDataSet(incomeEntries, getString(R.string.business_revenue));
        incomeSet.setColor(requireContext().getColor(R.color.income_green));

        BarDataSet expenseSet = new BarDataSet(expenseEntries, getString(R.string.business_expense));
        expenseSet.setColor(requireContext().getColor(R.color.expense_red));

        float groupSpace = 0.28f;
        float barSpace = 0.04f;
        float barWidth = 0.32f;

        BarData data = new BarData(incomeSet, expenseSet);
        data.setBarWidth(barWidth);
        data.setDrawValues(false);

        XAxis xAxis = monthlyChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum(0f + data.getGroupWidth(groupSpace, barSpace) * MONTH_COUNT);

        monthlyChart.setData(data);
        monthlyChart.groupBars(0f, groupSpace, barSpace);
        monthlyChart.getAxisLeft().setValueFormatter(new CompactMoneyFormatter());
        monthlyChart.invalidate();
    }

    private String buildMonthLabel(int indexFromOldest) {
        Calendar c = Calendar.getInstance();
        int monthOffset = indexFromOldest - (MONTH_COUNT - 1);
        c.add(Calendar.MONTH, monthOffset);
        int month = c.get(Calendar.MONTH) + 1;
        return getString(R.string.business_month_label_format, month);
    }

    private String formatMoney(double amount) {
        return amountFormatter.format(amount) + " đ";
    }

    private String formatCompact(double amount) {
        return amountFormatter.format(amount);
    }

    private void openChildScreen(Fragment fragment) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragment_container_business, fragment)
                .addToBackStack(null)
                .commit();
    }

    private static class CompactMoneyFormatter extends ValueFormatter {
        @Override
        public String getFormattedValue(float value) {
            if (value >= 1_000_000f) {
                return String.format(Locale.getDefault(), "%.1fM", value / 1_000_000f);
            }
            if (value >= 1_000f) {
                return String.format(Locale.getDefault(), "%.0fK", value / 1_000f);
            }
            return String.format(Locale.getDefault(), "%.0f", value);
        }
    }
}
