package com.uit.minhho.financetracker.fragment.personal;

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
import androidx.lifecycle.ViewModelProvider;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
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
import com.uit.minhho.financetracker.data.local.entity.Category;
import com.uit.minhho.financetracker.data.local.entity.Transaction;
import com.uit.minhho.financetracker.viewmodel.CategoryViewModel;
import com.uit.minhho.financetracker.viewmodel.TransactionViewModel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class ReportFragment extends Fragment {
    private BarChart barChart;
    private PieChart pieChart;
    private TextView incomeText;
    private TextView expenseText;
    private TextView netText;
    private TextView chartNoteText;
    private TextView selectedPeriodText;
    private Button previousPeriodButton;
    private Button nextPeriodButton;
    private TransactionViewModel transactionViewModel;
    private CategoryViewModel categoryViewModel;
    private List<Transaction> transactions = new ArrayList<>();
    private Map<Integer, Category> categoriesById = new HashMap<>();
    private PeriodMode selectedPeriod = PeriodMode.MONTH;
    private int periodOffset = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_personal_report, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        barChart = view.findViewById(R.id.bar_chart_comparison);
        pieChart = view.findViewById(R.id.pie_chart_distribution);
        incomeText = view.findViewById(R.id.tv_report_total_income);
        expenseText = view.findViewById(R.id.tv_report_total_expense);
        netText = view.findViewById(R.id.tv_report_net_income);
        chartNoteText = view.findViewById(R.id.tv_report_chart_note);
        selectedPeriodText = view.findViewById(R.id.tv_report_selected_period);
        previousPeriodButton = view.findViewById(R.id.btn_report_previous_period);
        nextPeriodButton = view.findViewById(R.id.btn_report_next_period);
        transactionViewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);
        categoryViewModel = new ViewModelProvider(requireActivity()).get(CategoryViewModel.class);
        setupPeriodTabs(view);
        setupPeriodNavigation();

        transactionViewModel.getTransactions(false).observe(getViewLifecycleOwner(), rows -> {
            transactions = rows == null ? new ArrayList<>() : rows;
            renderReport();
        });
        categoryViewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            categoriesById = new HashMap<>();
            if (categories != null) {
                for (Category category : categories) {
                    categoriesById.put(category.getId(), category);
                }
            }
            renderReport();
        });
        transactionViewModel.refreshTransactions(false);
        categoryViewModel.refreshCategories();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (transactionViewModel != null) {
            transactionViewModel.refreshTransactions(false);
        }
        if (categoryViewModel != null) {
            categoryViewModel.refreshCategories();
        }
    }

    private void setupPeriodTabs(View view) {
        TabLayout tabs = view.findViewById(R.id.tab_report_periods);
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedPeriod = periodFromPosition(tab.getPosition());
                periodOffset = 0;
                renderReport();
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
                renderReport();
            });
        }
        if (nextPeriodButton != null) {
            nextPeriodButton.setOnClickListener(v -> {
                if (periodOffset < 0) {
                    periodOffset++;
                    renderReport();
                }
            });
        }
    }

    private void renderReport() {
        PeriodRange periodRange = periodRangeForLatestTransaction();
        if (chartNoteText != null) {
            chartNoteText.setText(periodRange == null ? "Chưa có dữ liệu dòng tiền" : periodRange.note);
        }
        updatePeriodNavigation(periodRange);

        double income = 0.0;
        double expense = 0.0;
        Map<String, Double> categoryTotals = new HashMap<>();
        Map<String, CashFlowBucket> cashFlowByPeriod = new TreeMap<>();

        for (Transaction transaction : transactions) {
            if (periodRange == null || transaction.getTimestamp() < periodRange.start || transaction.getTimestamp() >= periodRange.end) {
                continue;
            }

            String bucketKey = bucketKey(transaction.getTimestamp(), selectedPeriod);
            CashFlowBucket bucket = cashFlowByPeriod.containsKey(bucketKey)
                    ? cashFlowByPeriod.get(bucketKey)
                    : new CashFlowBucket();
            if (transaction.isIncome()) {
                income += transaction.getAmount();
                bucket.income += transaction.getAmount();
            } else {
                expense += transaction.getAmount();
                bucket.expense += transaction.getAmount();
            }
            String categoryLabel = categoryLabel(transaction.getCategoryId());
            double categoryTotal = categoryTotals.containsKey(categoryLabel) ? categoryTotals.get(categoryLabel) : 0.0;
            categoryTotals.put(categoryLabel, categoryTotal + transaction.getAmount());
            cashFlowByPeriod.put(bucketKey, bucket);
        }

        incomeText.setText(formatMoney(income));
        expenseText.setText(formatMoney(expense));
        netText.setText(formatMoney(income - expense));
        setupBarChart(barChart, cashFlowByPeriod);
        setupPieChart(pieChart, categoryTotals);
    }

    private void setupBarChart(BarChart chart, Map<String, CashFlowBucket> cashFlowByPeriod) {
        if (chart == null) return;
        if (cashFlowByPeriod.isEmpty()) {
            chart.clear();
            chart.setNoDataText("Chưa có dữ liệu dòng tiền");
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

    private PeriodRange periodRangeForLatestTransaction() {
        long latestTimestamp = 0L;
        for (Transaction transaction : transactions) {
            latestTimestamp = Math.max(latestTimestamp, transaction.getTimestamp());
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

    private void setupPieChart(PieChart chart, Map<String, Double> categoryTotals) {
        if (chart == null) return;
        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
            colors.add(categoryColorByLabel(entry.getKey()));
        }

        if (entries.isEmpty()) {
            chart.clear();
            chart.setCenterText("Chưa có\ndữ liệu");
            chart.invalidate();
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);
        
        PieData data = new PieData(dataSet);
        chart.setData(data);
        chart.setCenterText("Phân loại\ntrong kỳ");
        chart.setCenterTextSize(16f);
        chart.setHoleRadius(50f);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setVerticalAlignment(com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM);
        chart.getLegend().setHorizontalAlignment(com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER);
        chart.animateXY(1000, 1000);
        chart.invalidate();
    }

    private int categoryColor(Category category) {
        if (category != null && category.getColorHex() != null) {
            try {
                return Color.parseColor(category.getColorHex());
            } catch (IllegalArgumentException ignored) {
            }
        }
        return requireContext().getColor(R.color.cat_other);
    }

    private String categoryLabel(int categoryId) {
        Category category = categoriesById.get(categoryId);
        if (category == null || category.getName() == null || category.getName().trim().isEmpty()) {
            return "Chưa phân loại";
        }
        return category.getName().trim();
    }

    private int categoryColorByLabel(String label) {
        for (Category category : categoriesById.values()) {
            if (category.getName() != null && category.getName().trim().equals(label)) {
                return categoryColor(category);
            }
        }
        return requireContext().getColor(R.color.cat_other);
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
}
