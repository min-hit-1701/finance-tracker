package com.uit.minhho.financetracker.fragment.business;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.tabs.TabLayout;
import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.data.remote.BusinessApiClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
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
    private TextView chartNoteText;
    private TextView selectedPeriodText;
    private Button previousPeriodButton;
    private Button nextPeriodButton;
    private PeriodMode selectedPeriod = PeriodMode.MONTH;
    private int periodOffset = 0;

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
        chartNoteText = view.findViewById(R.id.tv_business_report_chart_note);
        selectedPeriodText = view.findViewById(R.id.tv_business_report_selected_period);
        previousPeriodButton = view.findViewById(R.id.btn_business_report_previous_period);
        nextPeriodButton = view.findViewById(R.id.btn_business_report_next_period);
        setupPeriodTabs(view);
        setupPeriodNavigation();
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
            List<BusinessApiClient.ReportTransaction> rows = apiClient.getReportTransactions();
            Activity activity = getActivity();
            if (!isAdded() || activity == null) {
                return;
            }

            activity.runOnUiThread(() -> {
                if (!isAdded()) {
                    return;
                }
                PeriodRange periodRange = periodRangeForLatestTransaction(rows);
                if (chartNoteText != null) {
                    chartNoteText.setText(periodRange == null ? "Chưa có dữ liệu dòng tiền" : periodRange.note);
                }
                updatePeriodNavigation(periodRange);

                double income = 0.0;
                double expense = 0.0;
                Map<String, CashFlowBucket> cashFlowByPeriod = new TreeMap<>();
                List<BusinessApiClient.ReportTransaction> filteredRows = new ArrayList<>();
                for (BusinessApiClient.ReportTransaction row : rows) {
                    if (periodRange == null || row.timestamp < periodRange.start || row.timestamp >= periodRange.end) {
                        continue;
                    }
                    filteredRows.add(row);
                    String bucketKey = bucketKey(row.timestamp, selectedPeriod);
                    CashFlowBucket bucket = cashFlowByPeriod.containsKey(bucketKey)
                            ? cashFlowByPeriod.get(bucketKey)
                            : new CashFlowBucket();
                    if (row.income) {
                        income += row.amount;
                        bucket.income += row.amount;
                    } else {
                        expense += row.amount;
                        bucket.expense += row.amount;
                    }
                    cashFlowByPeriod.put(bucketKey, bucket);
                }

                revenueText.setText(formatMoney(income));
                expenseText.setText(formatMoney(expense));
                profitText.setText(formatMoney(income - expense));
                setupBarChart(barChart, cashFlowByPeriod);
                setupPieChart(pieChart, filteredRows);
            });
        });
    }

    private void setupPeriodTabs(View view) {
        TabLayout tabs = view.findViewById(R.id.tab_business_report_periods);
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedPeriod = periodFromPosition(tab.getPosition());
                periodOffset = 0;
                loadReport();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                onTabSelected(tab);
            }
        });
        TabLayout.Tab monthTab = tabs.getTabAt(1);
        if (monthTab != null) {
            monthTab.select();
        }
    }

    private void setupPeriodNavigation() {
        if (previousPeriodButton != null) {
            previousPeriodButton.setOnClickListener(v -> {
                periodOffset--;
                loadReport();
            });
        }
        if (nextPeriodButton != null) {
            nextPeriodButton.setOnClickListener(v -> {
                if (periodOffset < 0) {
                    periodOffset++;
                    loadReport();
                }
            });
        }
    }

    private void setupBarChart(BarChart chart, Map<String, CashFlowBucket> cashFlowByPeriod) {
        if (chart == null) {
            return;
        }
        if (cashFlowByPeriod.isEmpty()) {
            chart.clear();
            chart.setNoDataText("Chưa có dữ liệu thu chi");
            chart.invalidate();
            return;
        }

        List<BarEntry> incomeEntries = new ArrayList<>();
        List<BarEntry> expenseEntries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int index = 0;
        double max = 0.0;
        for (Map.Entry<String, CashFlowBucket> entry : cashFlowByPeriod.entrySet()) {
            CashFlowBucket bucket = entry.getValue();
            incomeEntries.add(new BarEntry(index, (float) bucket.income));
            expenseEntries.add(new BarEntry(index, (float) bucket.expense));
            labels.add(bucketLabel(entry.getKey(), selectedPeriod));
            max = Math.max(max, Math.max(bucket.income, bucket.expense));
            index++;
        }

        BarDataSet incomeSet = new BarDataSet(incomeEntries, "Thu");
        incomeSet.setColor(requireContext().getColor(R.color.income_green));
        incomeSet.setValueTextSize(11f);
        incomeSet.setValueFormatter(new NonZeroMoneyFormatter());

        BarDataSet expenseSet = new BarDataSet(expenseEntries, "Chi");
        expenseSet.setColor(requireContext().getColor(R.color.expense_red));
        expenseSet.setValueTextSize(11f);
        expenseSet.setValueFormatter(new NonZeroMoneyFormatter());

        float groupSpace = 0.20f;
        float barSpace = 0.05f;
        float barWidth = 0.35f;
        BarData data = new BarData(incomeSet, expenseSet);
        data.setBarWidth(barWidth);
        chart.clear();
        chart.setData(data);
        data.groupBars(0f, groupSpace, barSpace);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(true);

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setCenterAxisLabels(true);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum(labels.size());

        chart.getAxisLeft().setAxisMinimum(0f);
        chart.getAxisLeft().setAxisMaximum((float) (max <= 0.0 ? 1.0 : max * 1.15));
        chart.getAxisLeft().setValueFormatter(new LargeValueFormatter());
        chart.getAxisLeft().setDrawGridLines(true);
        chart.getAxisRight().setEnabled(false);
        chart.setScaleEnabled(false);
        chart.setPinchZoom(false);
        chart.setDoubleTapToZoomEnabled(false);
        data.notifyDataChanged();
        chart.notifyDataSetChanged();
        chart.animateY(700);
        chart.invalidate();
    }

    private PeriodRange periodRangeForLatestTransaction(List<BusinessApiClient.ReportTransaction> rows) {
        long latestTimestamp = 0L;
        for (BusinessApiClient.ReportTransaction row : rows) {
            latestTimestamp = Math.max(latestTimestamp, row.timestamp);
        }
        if (latestTimestamp <= 0L) {
            return null;
        }

        Calendar start = Calendar.getInstance(Locale.US);
        start.setFirstDayOfWeek(Calendar.MONDAY);
        start.setMinimalDaysInFirstWeek(1);
        start.setTimeInMillis(latestTimestamp);
        clearTime(start);

        switch (selectedPeriod) {
            case WEEK:
                int dayOfWeek = start.get(Calendar.DAY_OF_WEEK);
                int offset = (dayOfWeek - Calendar.MONDAY + 7) % 7;
                start.add(Calendar.DAY_OF_MONTH, -offset);
                start.add(Calendar.DAY_OF_MONTH, periodOffset * 7);
                Calendar weekEnd = (Calendar) start.clone();
                weekEnd.add(Calendar.DAY_OF_MONTH, 7);
                return new PeriodRange(start.getTimeInMillis(), weekEnd.getTimeInMillis(),
                        "Dòng tiền " + weekTitle(start),
                        weekTitle(start));
            case QUARTER:
                int quarter = start.get(Calendar.MONTH) / 3 + 1;
                start.set(Calendar.MONTH, (quarter - 1) * 3);
                start.set(Calendar.DAY_OF_MONTH, 1);
                start.add(Calendar.MONTH, periodOffset * 3);
                Calendar quarterEnd = (Calendar) start.clone();
                quarterEnd.add(Calendar.MONTH, 3);
                int selectedQuarter = start.get(Calendar.MONTH) / 3 + 1;
                return new PeriodRange(start.getTimeInMillis(), quarterEnd.getTimeInMillis(),
                        String.format(Locale.US, "Dòng tiền quý %d năm %d", selectedQuarter, start.get(Calendar.YEAR)),
                        String.format(Locale.US, "Quý %d/%d", selectedQuarter, start.get(Calendar.YEAR)));
            case YEAR:
                start.set(Calendar.MONTH, Calendar.JANUARY);
                start.set(Calendar.DAY_OF_MONTH, 1);
                start.add(Calendar.YEAR, periodOffset);
                Calendar yearEnd = (Calendar) start.clone();
                yearEnd.add(Calendar.YEAR, 1);
                return new PeriodRange(start.getTimeInMillis(), yearEnd.getTimeInMillis(),
                        String.format(Locale.US, "Dòng tiền năm %d", start.get(Calendar.YEAR)),
                        String.format(Locale.US, "Năm %d", start.get(Calendar.YEAR)));
            case MONTH:
            default:
                start.set(Calendar.DAY_OF_MONTH, 1);
                start.add(Calendar.MONTH, periodOffset);
                Calendar monthEnd = (Calendar) start.clone();
                monthEnd.add(Calendar.MONTH, 1);
                return new PeriodRange(start.getTimeInMillis(), monthEnd.getTimeInMillis(),
                        String.format(Locale.US, "Dòng tiền tháng %d/%d", start.get(Calendar.MONTH) + 1, start.get(Calendar.YEAR)),
                        String.format(Locale.US, "Tháng %d/%d", start.get(Calendar.MONTH) + 1, start.get(Calendar.YEAR)));
        }
    }

    private void updatePeriodNavigation(PeriodRange periodRange) {
        if (selectedPeriodText != null) {
            selectedPeriodText.setText(periodRange == null ? "Chưa có dữ liệu" : periodRange.title);
        }
        if (nextPeriodButton != null) {
            nextPeriodButton.setEnabled(periodOffset < 0);
        }
    }

    private String weekTitle(Calendar weekStart) {
        Calendar label = (Calendar) weekStart.clone();
        label.setFirstDayOfWeek(Calendar.MONDAY);
        label.setMinimalDaysInFirstWeek(1);
        return String.format(Locale.US, "Tuần %d, tháng %d/%d",
                label.get(Calendar.WEEK_OF_MONTH),
                label.get(Calendar.MONTH) + 1,
                label.get(Calendar.YEAR));
    }

    private PeriodMode periodFromPosition(int position) {
        switch (position) {
            case 0:
                return PeriodMode.WEEK;
            case 2:
                return PeriodMode.QUARTER;
            case 3:
                return PeriodMode.YEAR;
            case 1:
            default:
                return PeriodMode.MONTH;
        }
    }

    private String bucketKey(long timestamp, PeriodMode periodMode) {
        String pattern = periodMode == PeriodMode.QUARTER || periodMode == PeriodMode.YEAR
                ? "yyyy-MM"
                : "yyyy-MM-dd";
        return new SimpleDateFormat(pattern, Locale.US).format(new java.util.Date(timestamp));
    }

    private String bucketLabel(String key, PeriodMode periodMode) {
        if (periodMode == PeriodMode.QUARTER || periodMode == PeriodMode.YEAR) {
            String[] parts = key.split("-");
            return parts.length == 2 ? parts[1] + "/" + parts[0].substring(2) : key;
        }
        String[] parts = key.split("-");
        return parts.length == 3 ? parts[2] + "/" + parts[1] : key;
    }

    private void clearTime(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private void setupPieChart(PieChart chart, List<BusinessApiClient.ReportTransaction> rows) {
        if (chart == null) {
            return;
        }

        Map<String, Double> expensesByLabel = new HashMap<>();
        Map<String, Double> incomeByLabel = new HashMap<>();
        for (BusinessApiClient.ReportTransaction row : rows) {
            String label = reportLabel(row.note);
            Map<String, Double> target = row.income ? incomeByLabel : expensesByLabel;
            double current = target.containsKey(label) ? target.get(label) : 0.0;
            target.put(label, current + row.amount);
        }

        List<PieEntry> entries = new ArrayList<>();
        boolean showingIncome = expensesByLabel.isEmpty() && !incomeByLabel.isEmpty();
        Map<String, Double> pieRows = showingIncome ? incomeByLabel : expensesByLabel;
        for (Map.Entry<String, Double> entry : pieRows.entrySet()) {
            entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
        }

        if (entries.isEmpty()) {
            chart.clear();
            chart.setCenterText("Chưa có\ndữ liệu");
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
        chart.setCenterText(showingIncome ? "Doanh thu\ntrong kỳ" : getString(R.string.business_expense_center_text));
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

    private enum PeriodMode {
        WEEK,
        MONTH,
        QUARTER,
        YEAR
    }

    private static class PeriodRange {
        final long start;
        final long end;
        final String note;
        final String title;

        PeriodRange(long start, long end, String note, String title) {
            this.start = start;
            this.end = end;
            this.note = note;
            this.title = title;
        }
    }

    private static class CashFlowBucket {
        double income;
        double expense;
    }

    private class NonZeroMoneyFormatter extends ValueFormatter {
        @Override
        public String getBarLabel(BarEntry barEntry) {
            return barEntry.getY() <= 0f ? "" : new LargeValueFormatter().getBarLabel(barEntry);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
