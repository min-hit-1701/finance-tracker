package com.uit.minhho.financetracker.fragment.personal;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.data.local.entity.Category;
import com.uit.minhho.financetracker.data.local.entity.Transaction;
import com.uit.minhho.financetracker.viewmodel.CategoryViewModel;
import com.uit.minhho.financetracker.viewmodel.TransactionViewModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportFragment extends Fragment {
    private BarChart barChart;
    private PieChart pieChart;
    private TextView incomeText;
    private TextView expenseText;
    private TextView netText;
    private TransactionViewModel transactionViewModel;
    private CategoryViewModel categoryViewModel;
    private List<Transaction> transactions = new ArrayList<>();
    private Map<Integer, Category> categoriesById = new HashMap<>();

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
        transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
        categoryViewModel = new ViewModelProvider(requireActivity()).get(CategoryViewModel.class);

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

    private void renderReport() {
        double income = 0.0;
        double expense = 0.0;
        Map<Integer, Double> expensesByCategory = new HashMap<>();

        for (Transaction transaction : transactions) {
            if (transaction.isIncome()) {
                income += transaction.getAmount();
            } else {
                expense += transaction.getAmount();
                double current = expensesByCategory.containsKey(transaction.getCategoryId())
                        ? expensesByCategory.get(transaction.getCategoryId())
                        : 0.0;
                expensesByCategory.put(transaction.getCategoryId(), current + transaction.getAmount());
            }
        }

        incomeText.setText(formatMoney(income));
        expenseText.setText(formatMoney(expense));
        netText.setText(formatMoney(income - expense));
        setupBarChart(barChart, income, expense);
        setupPieChart(pieChart, expensesByCategory);
    }

    private void setupBarChart(BarChart chart, double income, double expense) {
        if (chart == null) return;
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, (float) income));
        entries.add(new BarEntry(1f, (float) expense));

        BarDataSet dataSet = new BarDataSet(entries, "Thu - Chi (đ)");
        dataSet.setColors(new int[]{R.color.income_green, R.color.expense_red}, getContext());
        dataSet.setValueTextSize(12f);
        dataSet.setValueFormatter(new LargeValueFormatter());
        
        BarData data = new BarData(dataSet);
        chart.setData(data);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        
        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"Thu nhập", "Chi tiêu"}));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        
        chart.getAxisLeft().setDrawGridLines(false);
        chart.getAxisRight().setEnabled(false);
        chart.setFitBars(true);
        chart.animateY(1000);
        chart.invalidate();
    }

    private void setupPieChart(PieChart chart, Map<Integer, Double> expensesByCategory) {
        if (chart == null) return;
        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        for (Map.Entry<Integer, Double> entry : expensesByCategory.entrySet()) {
            Category category = categoriesById.get(entry.getKey());
            String name = category == null ? "Danh mục #" + entry.getKey() : category.getName();
            entries.add(new PieEntry(entry.getValue().floatValue(), name));
            colors.add(categoryColor(category));
        }

        if (entries.isEmpty()) {
            chart.clear();
            chart.setCenterText("Chưa có\nchi tiêu");
            chart.invalidate();
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);
        
        PieData data = new PieData(dataSet);
        chart.setData(data);
        chart.setCenterText("Chi tiêu\nTháng này");
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

    private String formatMoney(double amount) {
        return String.format(Locale.US, "%,.0f đ", amount);
    }
}
