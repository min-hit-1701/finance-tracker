package com.uit.minhho.financetracker.adapter.business;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.model.business.BusinessTransaction;

import java.util.List;

public class BusinessTransactionAdapter extends RecyclerView.Adapter<BusinessTransactionAdapter.ViewHolder> {

    public interface OnTransactionActionListener {
        void onTransactionClick(BusinessTransaction transaction);
        void onTransactionLongClick(BusinessTransaction transaction);
    }

    private final List<BusinessTransaction> items;
    private final OnTransactionActionListener actionListener;

    public BusinessTransactionAdapter(List<BusinessTransaction> items) {
        this(items, null);
    }

    public BusinessTransactionAdapter(List<BusinessTransaction> items, OnTransactionActionListener actionListener) {
        this.items = items;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_business_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BusinessTransaction item = items.get(position);
        holder.titleText.setText(item.getTitle());
        holder.subtitleText.setText(item.getSubtitle());
        holder.amountText.setText(item.getAmount());
        holder.amountText.setTextColor(holder.itemView.getResources().getColor(
                item.isIncome() ? R.color.income_green : R.color.expense_red,
                null
        ));
        holder.itemView.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onTransactionClick(item);
            }
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (actionListener != null) {
                actionListener.onTransactionLongClick(item);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        TextView subtitleText;
        TextView amountText;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.tx_title);
            subtitleText = itemView.findViewById(R.id.tx_subtitle);
            amountText = itemView.findViewById(R.id.tx_amount);
        }
    }
}
