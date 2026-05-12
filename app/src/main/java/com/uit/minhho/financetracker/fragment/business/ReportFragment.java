package com.uit.minhho.financetracker.fragment.business;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.LargeValueFormatter;
import com.google.android.material.tabs.TabLayout;
import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.data.local.entity.Transaction;
import com.uit.minhho.financetracker.viewmodel.BusinessViewModel;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ReportFragment extends Fragment {

    private final DecimalFormat amountFormatter = new DecimalFormat("#,###");
    private BusinessViewModel businessViewModel;
    private TextView tvRevenue;
    private TextView tvExpense;
    private TextView tvProfit;
    private BarChart barChart;
    private PieChart pieChart;
    private long fromTimestamp;
    private long toTimestamp;
    private LiveData<List<Transaction>> periodTransactionsLiveData;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_business_report, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        businessViewModel = new ViewModelProvider(requireActivity()).get(BusinessViewModel.class);

        tvRevenue = view.findViewById(R.id.tv_business_total_revenue_value);
        tvExpense = view.findViewById(R.id.tv_business_total_expense_value);
        tvProfit = view.findViewById(R.id.tv_business_total_profit_value);
        barChart = view.findViewById(R.id.bar_chart_business);
        pieChart = view.findViewById(R.id.pie_chart_business);

        setupDefaultPeriod();
        observeReportData();
        setupTabs(view.findViewById(R.id.tab_business_report_periods));
    }

    private void setupDefaultPeriod() {
        Calendar now = Calendar.getInstance();
        now.set(Calendar.DAY_OF_MONTH, 1);
        now.set(Calendar.HOUR_OF_DAY, 0);
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.SECOND, 0);
        now.set(Calendar.MILLISECOND, 0);
        fromTimestamp = now.getTimeInMillis();

        Calendar end = Calendar.getInstance();
        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);
        end.set(Calendar.MILLISECOND, 999);
        toTimestamp = end.getTimeInMillis();
    }

    private void setupTabs(TabLayout tabLayout) {
        if (tabLayout == null) {
            return;
        }
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                applyPeriodByTab(position);
                observeReportData();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void applyPeriodByTab(int position) {
        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();

        if (position == 0) {
            start.set(Calendar.DAY_OF_WEEK, start.getFirstDayOfWeek());
        } else if (position == 1) {
            start.set(Calendar.DAY_OF_MONTH, 1);
        } else if (position == 2) {
            int currentMonth = start.get(Calendar.MONTH);
            int quarterStart = (currentMonth / 3) * 3;
            start.set(Calendar.MONTH, quarterStart);
            start.set(Calendar.DAY_OF_MONTH, 1);
        } else {
            start.set(Calendar.DAY_OF_YEAR, 1);
        }

        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);

        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);
        end.set(Calendar.MILLISECOND, 999);

        fromTimestamp = start.getTimeInMillis();
        toTimestamp = end.getTimeInMillis();
    }

    private void observeReportData() {
        if (periodTransactionsLiveData != null) {
            periodTransactionsLiveData.removeObservers(getViewLifecycleOwner());
        }

        periodTransactionsLiveData = businessViewModel.getBusinessTransactionsByPeriod(fromTimestamp, toTimestamp);
        periodTransactionsLiveData.observe(getViewLifecycleOwner(), transactions -> {
            double income = 0;
            double expense = 0;
            if (transactions != null) {
                for (Transaction transaction : transactions) {
                    if (transaction.isIncome()) {
                        income += transaction.getAmount();
                    } else {
                        expense += transaction.getAmount();
                    }
                }
            }
            renderTotalsAndCharts(income, expense);
        });
    }

    private void renderTotalsAndCharts(double income, double expense) {
        tvRevenue.setText(formatMoney(income));
        tvExpense.setText(formatMoney(expense));
        tvProfit.setText(formatMoney(income - expense));
        setupBarChart(barChart, income, expense);
        setupPieChart(pieChart, income, expense);
    }

    private String formatMoney(double amount) {
        return amountFormatter.format(amount) + " đ";
    }

    private void setupBarChart(BarChart chart, double income, double expense) {
        if (chart == null) {
            return;
        }

        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, (float) income));
        entries.add(new BarEntry(1f, (float) expense));

        BarDataSet dataSet = new BarDataSet(entries, getString(R.string.business_report_trend_title));
        dataSet.setColors(new int[]{R.color.income_green, R.color.expense_red}, requireContext());
        dataSet.setValueTextSize(12f);
        dataSet.setValueFormatter(new LargeValueFormatter());

        BarData data = new BarData(dataSet);
        chart.setData(data);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{
                getString(R.string.business_total_revenue),
                getString(R.string.business_total_expense)
        }));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);

        chart.getAxisLeft().setDrawGridLines(false);
        chart.getAxisRight().setEnabled(false);
        chart.setFitBars(true);
        chart.animateY(600);
        chart.invalidate();
    }

    private void setupPieChart(PieChart chart, double income, double expense) {
        if (chart == null) {
            return;
        }

        float total = (float) (income + expense);
        if (total <= 0f) {
            total = 1f;
        }
        float incomePercent = (float) ((income * 100f) / total);
        float expensePercent = (float) ((expense * 100f) / total);

        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(Math.max(0.1f, incomePercent), getString(R.string.business_revenue)));
        entries.add(new PieEntry(Math.max(0.1f, expensePercent), getString(R.string.business_expense)));

        PieDataSet dataSet = new PieDataSet(entries, "");
        int[] colors = {
                requireContext().getResources().getColor(R.color.income_green, null),
                requireContext().getResources().getColor(R.color.expense_red, null)
        };
        dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);

        PieData data = new PieData(dataSet);
        chart.setData(data);
        chart.setCenterText(getString(R.string.business_report_center_text));
        chart.setCenterTextSize(15f);
        chart.setHoleRadius(52f);
        chart.setEntryLabelTextSize(11f);
        chart.getDescription().setEnabled(false);
        Legend legend = chart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setWordWrapEnabled(true);
        legend.setFormSize(10f);
        legend.setTextSize(11f);
        legend.setXEntrySpace(10f);
        chart.animateXY(600, 600);
        chart.invalidate();
    }
}
