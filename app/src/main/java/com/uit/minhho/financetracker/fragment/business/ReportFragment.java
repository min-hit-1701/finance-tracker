package com.uit.minhho.financetracker.fragment.business;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

import java.util.ArrayList;
import java.util.List;

public class ReportFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_business_report, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupBarChart(view.findViewById(R.id.bar_chart_business));
        setupPieChart(view.findViewById(R.id.pie_chart_business));
    }

    private void setupBarChart(BarChart chart) {
        if (chart == null) {
            return;
        }

        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, 45200000f));
        entries.add(new BarEntry(1f, 12300000f));

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

    private void setupPieChart(PieChart chart) {
        if (chart == null) {
            return;
        }

        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(54f, getString(R.string.business_expense_operations)));
        entries.add(new PieEntry(23f, getString(R.string.business_expense_marketing)));
        entries.add(new PieEntry(18f, getString(R.string.business_expense_payroll)));
        entries.add(new PieEntry(5f, getString(R.string.cat_other)));

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
}
