package com.uit.minhho.financetracker.adapter.personal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
    private final OnBudgetActionListener actionListener;

    public BudgetAdapter(List<Budget> items) {
        this(items, null);
    }

    public BudgetAdapter(List<Budget> items, OnBudgetActionListener actionListener) {
        this.items = items;
        this.actionListener = actionListener;
    }

    public void setBudgets(List<Budget> budgets) {
        items.clear();
        if (budgets != null) {
            items.addAll(budgets);
        }
        notifyDataSetChanged();
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
        Context context = holder.itemView.getContext();
        
        holder.categoryName.setText(item.getCategoryName());
        holder.statusText.setText(context.getString(R.string.budget_percent_format, item.getProgressPercent()));
        holder.progressBar.setProgress(Math.min(100, item.getProgressPercent()));
        
        holder.usageText.setText(context.getString(
                R.string.budget_spent_of,
                formatMoney(item.getSpentAmount()),
                formatMoney(item.getLimitAmount())
        ));
        holder.deleteButton.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onDeleteBudget(item);
            }
        });
        
        double remaining = item.getLimitAmount() - item.getSpentAmount();
        if (remaining >= 0) {
            holder.remainingText.setText(context.getString(R.string.budget_remaining, formatMoney(remaining)));
            holder.remainingText.setTextColor(context.getResources().getColor(R.color.text_secondary, null));
            holder.progressBar.setIndicatorColor(context.getResources().getColor(R.color.brand_primary, null));
        } else {
            holder.remainingText.setText(context.getString(R.string.budget_over, formatMoney(Math.abs(remaining))));
            holder.remainingText.setTextColor(context.getResources().getColor(R.color.expense_red, null));
            holder.progressBar.setIndicatorColor(context.getResources().getColor(R.color.expense_red, null));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView categoryName;
        TextView statusText;
        LinearProgressIndicator progressBar;
        TextView usageText;
        TextView remainingText;
        ImageButton deleteButton;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.budget_category_name);
            statusText = itemView.findViewById(R.id.budget_status_text);
            progressBar = itemView.findViewById(R.id.budget_progress_bar);
            usageText = itemView.findViewById(R.id.budget_usage_text);
            remainingText = itemView.findViewById(R.id.budget_remaining_text);
            deleteButton = itemView.findViewById(R.id.btn_delete_budget);
        }
    }

    public interface OnBudgetActionListener {
        void onDeleteBudget(Budget budget);
    }

    private String formatMoney(double amount) {
        return formatter.format(amount) + " đ";
    }
}
