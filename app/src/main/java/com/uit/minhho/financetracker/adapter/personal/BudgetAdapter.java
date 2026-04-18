package com.uit.minhho.financetracker.adapter.personal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.model.personal.Budget;

import java.text.DecimalFormat;
import java.util.List;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.ViewHolder> {

    private final List<Budget> items;
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    public BudgetAdapter(List<Budget> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_personal_budget, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Budget item = items.get(position);
        holder.categoryName.setText(item.getCategoryName());
        holder.statusText.setText(item.getProgressPercent() + "%");
        holder.progressBar.setProgress(item.getProgressPercent());
        
        holder.spentText.setText(formatter.format(item.getSpentAmount()));
        holder.limitText.setText(formatter.format(item.getLimitAmount()));
        
        double remaining = item.getLimitAmount() - item.getSpentAmount();
        if (remaining >= 0) {
            holder.remainingText.setText(holder.itemView.getContext().getString(R.string.budget_remaining) + ": " + formatter.format(remaining));
            holder.remainingText.setTextColor(holder.itemView.getResources().getColor(R.color.text_secondary, null));
            holder.progressBar.setIndicatorColor(holder.itemView.getResources().getColor(R.color.brand_primary, null));
        } else {
            holder.remainingText.setText(holder.itemView.getContext().getString(R.string.budget_over) + ": " + formatter.format(Math.abs(remaining)));
            holder.remainingText.setTextColor(holder.itemView.getResources().getColor(R.color.expense_red, null));
            holder.progressBar.setIndicatorColor(holder.itemView.getResources().getColor(R.color.expense_red, null));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView categoryName;
        TextView statusText;
        LinearProgressIndicator progressBar;
        TextView spentText;
        TextView limitText;
        TextView remainingText;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.budget_category_name);
            statusText = itemView.findViewById(R.id.budget_status_text);
            progressBar = itemView.findViewById(R.id.budget_progress_bar);
            spentText = itemView.findViewById(R.id.budget_spent_text);
            limitText = itemView.findViewById(R.id.budget_limit_text);
            remainingText = itemView.findViewById(R.id.budget_remaining_text);
        }
    }
}
