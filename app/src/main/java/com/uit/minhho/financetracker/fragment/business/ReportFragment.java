package com.uit.minhho.financetracker.fragment.business;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.LargeValueFormatter;
import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.data.remote.BusinessApiClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReportFragment extends Fragment {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private BusinessApiClient apiClient;
    private BarChart barChart;
    private PieChart pieChart;
    private TextView revenueText;
    private TextView expenseText;
    private TextView profitText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_business_report, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        apiClient = new BusinessApiClient(requireContext());
        barChart = view.findViewById(R.id.bar_chart_business);
        pieChart = view.findViewById(R.id.pie_chart_business);
        revenueText = view.findViewById(R.id.tv_business_report_revenue);
        expenseText = view.findViewById(R.id.tv_business_report_expense);
        profitText = view.findViewById(R.id.tv_business_report_profit);
        loadReport();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (apiClient != null) {
            loadReport();
        }
    }

    private void loadReport() {
        executorService.execute(() -> {
            BusinessApiClient.ApiResult<BusinessApiClient.Summary> summary = apiClient.getSummary();
            List<BusinessApiClient.ReportTransaction> rows = apiClient.getReportTransactions();
            Activity activity = getActivity();
            if (!isAdded() || activity == null) {
                return;
            }

            activity.runOnUiThread(() -> {
                if (!isAdded()) {
                    return;
                }
                double income = summary.success && summary.data != null ? summary.data.totalIncome : 0.0;
                double expense = summary.success && summary.data != null ? summary.data.totalExpense : 0.0;
                revenueText.setText(formatMoney(income));
                expenseText.setText(formatMoney(expense));
                profitText.setText(formatMoney(income - expense));
                setupBarChart(barChart, income, expense);
                setupPieChart(pieChart, rows);
            });
        });
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
        chart.animateY(900);
        chart.invalidate();
    }

    private void setupPieChart(PieChart chart, List<BusinessApiClient.ReportTransaction> rows) {
        if (chart == null) {
            return;
        }

        Map<String, Double> expensesByLabel = new HashMap<>();
        for (BusinessApiClient.ReportTransaction row : rows) {
            if (row.income) {
                continue;
            }
            String label = reportLabel(row.note);
            double current = expensesByLabel.containsKey(label) ? expensesByLabel.get(label) : 0.0;
            expensesByLabel.put(label, current + row.amount);
        }

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : expensesByLabel.entrySet()) {
            entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
        }

        if (entries.isEmpty()) {
            chart.clear();
            chart.setCenterText("Chưa có\nchi phí");
            chart.invalidate();
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        int[] colors = {
                requireContext().getResources().getColor(R.color.cat_utility, null),
                requireContext().getResources().getColor(R.color.cat_shopping, null),
                requireContext().getResources().getColor(R.color.cat_salary, null),
                requireContext().getResources().getColor(R.color.cat_other, null)
        };
        dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);

        PieData data = new PieData(dataSet);
        chart.setData(data);
        chart.setCenterText(getString(R.string.business_expense_center_text));
        chart.setCenterTextSize(15f);
        chart.setHoleRadius(52f);
        chart.setEntryLabelTextSize(10f);
        chart.getDescription().setEnabled(false);
        Legend legend = chart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setWordWrapEnabled(true);
        legend.setFormSize(10f);
        legend.setTextSize(11f);
        legend.setXEntrySpace(10f);
        chart.animateXY(900, 900);
        chart.invalidate();
    }

    private String reportLabel(String note) {
        if (note == null || note.trim().isEmpty()) {
            return getString(R.string.cat_other);
        }
        String clean = note.trim();
        int separator = clean.indexOf(" - ");
        return separator > 0 ? clean.substring(0, separator) : clean;
    }

    private String formatMoney(double amount) {
        return String.format(Locale.US, "%,.0f đ", amount);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
