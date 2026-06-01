package com.uit.minhho.financetracker.fragment.business;

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
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.tabs.TabLayout;
import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.data.local.entity.Transaction;
import com.uit.minhho.financetracker.viewmodel.BusinessViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class ReportFragment extends Fragment {
    private BusinessViewModel businessViewModel;
    private BarChart barChart;
    private PieChart pieChart;
    private TextView revenueText, expenseText, profitText, chartNoteText, selectedPeriodText;
    private Button previousPeriodButton, nextPeriodButton;
    private PeriodMode selectedPeriod = PeriodMode.MONTH;
    private int periodOffset = 0;
    private List<Transaction> allTransactions = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_business_report, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        businessViewModel = new ViewModelProvider(requireActivity()).get(BusinessViewModel.class);
        barChart = view.findViewById(R.id.bar_chart_business);
        pieChart = view.findViewById(R.id.pie_chart_business);
        revenueText = view.findViewById(R.id.tv_business_report_revenue);
        expenseText = view.findViewById(R.id.tv_business_report_expense);
        profitText = view.findViewById(R.id.tv_business_report_profit);
        chartNoteText = view.findViewById(R.id.tv_business_report_chart_note);
        selectedPeriodText = view.findViewById(R.id.tv_business_report_selected_period);
        previousPeriodButton = view.findViewById(R.id.btn_business_report_previous_period);
        nextPeriodButton = view.findViewById(R.id.btn_business_report_next_period);

        businessViewModel.getBusinessTransactions().observe(getViewLifecycleOwner(), txns -> {
            allTransactions.clear();
            if (txns != null) allTransactions.addAll(txns);
            loadReport();
        });

        setupPeriodTabs(view);
        setupPeriodNavigation();
    }

    private void loadReport() {
        List<Transaction> filtered = new ArrayList<>();
        PeriodRange periodRange = periodRangeForTransactions(allTransactions);
        if (chartNoteText != null) {
            chartNoteText.setText(periodRange == null ? "Chưa có dữ liệu dòng tiền" : periodRange.note);
        }
        updatePeriodNavigation(periodRange);

        double income = 0, expense = 0;
        Map<String, CashFlowBucket> cashFlowByPeriod = new TreeMap<>();
        for (Transaction tx : allTransactions) {
            if (periodRange == null || tx.getTimestamp() < periodRange.start || tx.getTimestamp() >= periodRange.end)
                continue;
            filtered.add(tx);
            String key = bucketKey(tx.getTimestamp(), selectedPeriod);
            CashFlowBucket bucket = cashFlowByPeriod.getOrDefault(key, new CashFlowBucket());
            if (tx.isIncome()) { income += tx.getAmount(); bucket.income += tx.getAmount(); }
            else { expense += tx.getAmount(); bucket.expense += tx.getAmount(); }
            cashFlowByPeriod.put(key, bucket);
        }
        revenueText.setText(formatMoney(income));
        expenseText.setText(formatMoney(expense));
        profitText.setText(formatMoney(income - expense));
        setupBarChart(barChart, cashFlowByPeriod);
        setupPieChart(pieChart, filtered);
    }

    private void setupPeriodTabs(View view) {
        TabLayout tabs = view.findViewById(R.id.tab_business_report_periods);
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) { selectedPeriod = periodFromPosition(tab.getPosition()); periodOffset = 0; loadReport(); }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) { onTabSelected(tab); }
        });
        TabLayout.Tab monthTab = tabs.getTabAt(1);
        if (monthTab != null) monthTab.select();
    }

    private void setupPeriodNavigation() {
        if (previousPeriodButton != null) previousPeriodButton.setOnClickListener(v -> { periodOffset--; loadReport(); });
        if (nextPeriodButton != null) nextPeriodButton.setOnClickListener(v -> { if (periodOffset < 0) { periodOffset++; loadReport(); } });
    }

    private PeriodRange periodRangeForTransactions(List<Transaction> list) {
        long latest = 0;
        for (Transaction tx : list) latest = Math.max(latest, tx.getTimestamp());
        if (latest <= 0) return null;
        Calendar start = Calendar.getInstance(Locale.US);
        start.setTimeInMillis(latest);
        start.set(Calendar.HOUR_OF_DAY, 0); start.set(Calendar.MINUTE, 0); start.set(Calendar.SECOND, 0); start.set(Calendar.MILLISECOND, 0);
        switch (selectedPeriod) {
            case WEEK: start.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY); start.add(Calendar.DAY_OF_MONTH, periodOffset * 7);
                Calendar we = (Calendar) start.clone(); we.add(Calendar.DAY_OF_MONTH, 7);
                return new PeriodRange(start.getTimeInMillis(), we.getTimeInMillis(), "Dòng tiền " + weekTitle(start), weekTitle(start));
            case QUARTER: int q = start.get(Calendar.MONTH) / 3 + 1; start.set(Calendar.MONTH, (q-1)*3); start.set(Calendar.DAY_OF_MONTH, 1); start.add(Calendar.MONTH, periodOffset * 3);
                Calendar qe = (Calendar) start.clone(); qe.add(Calendar.MONTH, 3); int sq = start.get(Calendar.MONTH)/3+1;
                return new PeriodRange(start.getTimeInMillis(), qe.getTimeInMillis(), String.format(Locale.US, "Dòng tiền quý %d năm %d", sq, start.get(Calendar.YEAR)), String.format(Locale.US, "Quý %d/%d", sq, start.get(Calendar.YEAR)));
            case YEAR: start.set(Calendar.MONTH, Calendar.JANUARY); start.set(Calendar.DAY_OF_MONTH, 1); start.add(Calendar.YEAR, periodOffset);
                Calendar ye = (Calendar) start.clone(); ye.add(Calendar.YEAR, 1);
                return new PeriodRange(start.getTimeInMillis(), ye.getTimeInMillis(), String.format(Locale.US, "Dòng tiền năm %d", start.get(Calendar.YEAR)), String.format(Locale.US, "Năm %d", start.get(Calendar.YEAR)));
            default: start.set(Calendar.DAY_OF_MONTH, 1); start.add(Calendar.MONTH, periodOffset);
                Calendar me = (Calendar) start.clone(); me.add(Calendar.MONTH, 1);
                return new PeriodRange(start.getTimeInMillis(), me.getTimeInMillis(), String.format(Locale.US, "Dòng tiền tháng %d/%d", start.get(Calendar.MONTH)+1, start.get(Calendar.YEAR)), String.format(Locale.US, "Tháng %d/%d", start.get(Calendar.MONTH)+1, start.get(Calendar.YEAR)));
        }
    }

    private String weekTitle(Calendar ws) {
        Calendar c = (Calendar) ws.clone(); c.setFirstDayOfWeek(Calendar.MONDAY);
        return String.format(Locale.US, "tuần %d, tháng %d/%d", c.get(Calendar.WEEK_OF_MONTH), c.get(Calendar.MONTH)+1, c.get(Calendar.YEAR));
    }

    private void updatePeriodNavigation(PeriodRange r) {
        if (selectedPeriodText != null) selectedPeriodText.setText(r == null ? "Chưa có dữ liệu" : r.title);
        if (nextPeriodButton != null) nextPeriodButton.setEnabled(periodOffset < 0);
    }

    private PeriodMode periodFromPosition(int p) {
        switch (p) { case 0: return PeriodMode.WEEK; case 2: return PeriodMode.QUARTER; case 3: return PeriodMode.YEAR; default: return PeriodMode.MONTH; }
    }

    private String bucketKey(long ts, PeriodMode mode) {
        return new SimpleDateFormat(mode == PeriodMode.QUARTER || mode == PeriodMode.YEAR ? "yyyy-MM" : "yyyy-MM-dd", Locale.US).format(ts);
    }
    private String bucketLabel(String key, PeriodMode mode) {
        if (mode == PeriodMode.QUARTER || mode == PeriodMode.YEAR) { String[] p = key.split("-"); return p.length == 2 ? p[1]+"/"+p[0].substring(2) : key; }
        String[] p = key.split("-"); return p.length == 3 ? p[2]+"/"+p[1] : key;
    }

    private void setupBarChart(BarChart chart, Map<String, CashFlowBucket> data) {
        if (chart == null || data.isEmpty()) { if (chart != null) { chart.clear(); chart.setNoDataText("Chưa có dữ liệu"); chart.invalidate(); } return; }
        List<BarEntry> inc = new ArrayList<>(), exp = new ArrayList<>(); List<String> lbls = new ArrayList<>();
        int i = 0; double max = 0;
        for (Map.Entry<String, CashFlowBucket> e : data.entrySet()) {
            inc.add(new BarEntry(i, (float) e.getValue().income)); exp.add(new BarEntry(i, (float) e.getValue().expense));
            lbls.add(bucketLabel(e.getKey(), selectedPeriod)); max = Math.max(max, Math.max(e.getValue().income, e.getValue().expense)); i++;
        }
        BarDataSet iSet = new BarDataSet(inc, "Thu"); iSet.setColor(getResources().getColor(R.color.income_green, null)); iSet.setValueTextSize(11f); iSet.setValueFormatter(new NonZeroMoneyFormatter());
        BarDataSet eSet = new BarDataSet(exp, "Chi"); eSet.setColor(getResources().getColor(R.color.expense_red, null)); eSet.setValueTextSize(11f); eSet.setValueFormatter(new NonZeroMoneyFormatter());
        BarData bd = new BarData(iSet, eSet); bd.setBarWidth(0.35f); chart.clear(); chart.setData(bd); bd.groupBars(0f, 0.20f, 0.05f);
        chart.getDescription().setEnabled(false); chart.getLegend().setEnabled(true);
        XAxis xa = chart.getXAxis(); xa.setValueFormatter(new IndexAxisValueFormatter(lbls)); xa.setPosition(XAxis.XAxisPosition.BOTTOM); xa.setCenterAxisLabels(true); xa.setDrawGridLines(false); xa.setGranularity(1f); xa.setAxisMinimum(0f); xa.setAxisMaximum(lbls.size());
        chart.getAxisLeft().setAxisMinimum(0f); chart.getAxisLeft().setAxisMaximum((float) (max <= 0 ? 1.0 : max * 1.15)); chart.getAxisLeft().setValueFormatter(new LargeValueFormatter()); chart.getAxisLeft().setDrawGridLines(true);
        chart.getAxisRight().setEnabled(false); chart.setScaleEnabled(false); chart.setPinchZoom(false); chart.setDoubleTapToZoomEnabled(false);
        bd.notifyDataChanged(); chart.notifyDataSetChanged(); chart.animateY(700); chart.invalidate();
    }

    private void setupPieChart(PieChart chart, List<Transaction> rows) {
        if (chart == null) return;
        Map<String, Double> expMap = new HashMap<>(), incMap = new HashMap<>();
        for (Transaction tx : rows) {
            String label = reportLabel(tx.getNote());
            Map<String, Double> tgt = tx.isIncome() ? incMap : expMap;
            tgt.put(label, tgt.getOrDefault(label, 0.0) + tx.getAmount());
        }
        List<PieEntry> entries = new ArrayList<>(); boolean showInc = expMap.isEmpty() && !incMap.isEmpty();
        Map<String, Double> pieRows = showInc ? incMap : expMap;
        for (Map.Entry<String, Double> e : pieRows.entrySet()) entries.add(new PieEntry(e.getValue().floatValue(), e.getKey()));
        if (entries.isEmpty()) { chart.clear(); chart.setCenterText("Chưa có\ndữ liệu"); chart.invalidate(); return; }
        PieDataSet ds = new PieDataSet(entries, "");
        ds.setColors(new int[]{getResources().getColor(R.color.cat_utility, null), getResources().getColor(R.color.cat_shopping, null), getResources().getColor(R.color.cat_salary, null), getResources().getColor(R.color.cat_other, null)});
        ds.setValueTextColor(android.graphics.Color.WHITE); ds.setValueTextSize(12f);
        PieData pd = new PieData(ds); chart.setData(pd);
        chart.setCenterText(showInc ? "Doanh thu\ntrong kỳ" : "Chi tiêu\ntrong kỳ"); chart.setCenterTextSize(15f); chart.setHoleRadius(52f); chart.setEntryLabelTextSize(10f);
        chart.getDescription().setEnabled(false);
        Legend lg = chart.getLegend(); lg.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM); lg.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER); lg.setWordWrapEnabled(true); lg.setFormSize(10f); lg.setTextSize(11f); lg.setXEntrySpace(10f);
        chart.animateXY(900, 900); chart.invalidate();
    }

    private String reportLabel(String note) {
        if (note == null || note.trim().isEmpty()) return getString(R.string.cat_other);
        String clean = note.trim(); int sep = clean.indexOf(" | "); return sep > 0 ? clean.substring(0, sep) : clean;
    }

    private String formatMoney(double amount) { return String.format(Locale.US, "%,.0f đ", amount); }

    private enum PeriodMode { WEEK, MONTH, QUARTER, YEAR }
    private static class PeriodRange { final long start, end; final String note, title; PeriodRange(long s, long e, String n, String t) { start=s; end=e; note=n; title=t; } }
    private static class CashFlowBucket { double income, expense; }
    private class NonZeroMoneyFormatter extends ValueFormatter {
        @Override public String getBarLabel(BarEntry barEntry) { return barEntry.getY() <= 0f ? "" : new LargeValueFormatter().getBarLabel(barEntry); }
    }
}
