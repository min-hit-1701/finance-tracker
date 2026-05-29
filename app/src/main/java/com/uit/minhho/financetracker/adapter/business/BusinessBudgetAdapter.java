package com.uit.minhho.financetracker.adapter.business;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.model.business.BusinessBudgetItem;

import java.util.List;

public class BusinessBudgetAdapter extends RecyclerView.Adapter<BusinessBudgetAdapter.ViewHolder> {

    private final List<BusinessBudgetItem> items;

    public BusinessBudgetAdapter(List<BusinessBudgetItem> items) {
        this.items = items;
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
        BusinessBudgetItem item = items.get(position);
        holder.titleText.setText(item.getName());
        holder.usageText.setText(
                holder.itemView.getResources().getString(
                        R.string.business_budget_usage_format,
                        item.getUsed(),
                        item.getLimit()
                )
        );
        holder.progressBar.setProgress(item.getProgress());
    }

    @Override
    public int getItemCount() {
        return items.size();
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
