package com.uit.minhho.financetracker.fragment.personal;

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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_personal_report, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupBarChart(view.findViewById(R.id.bar_chart_comparison));
        setupPieChart(view.findViewById(R.id.pie_chart_distribution));
    }

    private void setupBarChart(BarChart chart) {
        if (chart == null) return;
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, 12000000f)); // Thu nhập
        entries.add(new BarEntry(1f, 4500000f));  // Chi tiêu

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

    private void setupPieChart(PieChart chart) {
        if (chart == null) return;
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(2200000f, "Ăn uống"));
        entries.add(new PieEntry(1200000f, "Di chuyển"));
        entries.add(new PieEntry(1000000f, "Mua sắm"));
        entries.add(new PieEntry(100000f, "Khác"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        int[] colors = {
            getContext().getResources().getColor(R.color.cat_food, null),
            getContext().getResources().getColor(R.color.cat_transport, null),
            getContext().getResources().getColor(R.color.cat_shopping, null),
            getContext().getResources().getColor(R.color.cat_other, null)
        };
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
}