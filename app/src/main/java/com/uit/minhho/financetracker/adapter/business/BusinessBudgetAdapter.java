package com.uit.minhho.financetracker.adapter.business;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.data.local.entity.Budget;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class BusinessBudgetAdapter extends RecyclerView.Adapter<BusinessBudgetAdapter.ViewHolder> {

    private final List<Budget> items;
    private final DecimalFormat amountFormatter = new DecimalFormat("#,###");

    public BusinessBudgetAdapter(List<Budget> items) {
        this.items = new ArrayList<>(items);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_business_budget, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Budget item = items.get(position);
        String budgetName = item.getName() == null || item.getName().trim().isEmpty()
                ? holder.itemView.getResources().getString(R.string.business_budget_default_name)
                : item.getName();
        holder.titleText.setText(budgetName);
        holder.usageText.setText(
                holder.itemView.getResources().getString(
                        R.string.business_budget_usage_format,
                        amountFormatter.format(item.getSpentAmount()),
                        amountFormatter.format(item.getLimitAmount())
                )
        );
        holder.progressBar.setProgress(getProgress(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void submitItems(List<Budget> budgets) {
        items.clear();
        if (budgets != null) {
            items.addAll(budgets);
        }
        notifyDataSetChanged();
    }

    private int getProgress(Budget budget) {
        if (budget.getLimitAmount() <= 0) {
            return 0;
        }
        return Math.min(100, (int) ((budget.getSpentAmount() * 100f) / budget.getLimitAmount()));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView titleText;
        final TextView usageText;
        final ProgressBar progressBar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.tv_business_budget_name);
            usageText = itemView.findViewById(R.id.tv_business_budget_usage);
            progressBar = itemView.findViewById(R.id.pb_business_budget);
        }
    }
}
