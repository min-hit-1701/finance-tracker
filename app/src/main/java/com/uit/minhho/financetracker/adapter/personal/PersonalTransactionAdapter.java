package com.uit.minhho.financetracker.adapter.personal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.model.personal.PersonalTransaction;

import java.text.DecimalFormat;
import java.util.List;

public class PersonalTransactionAdapter extends RecyclerView.Adapter<PersonalTransactionAdapter.ViewHolder> {

    private final List<PersonalTransaction> items;
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    public PersonalTransactionAdapter(List<PersonalTransaction> items) {
        this.items = items;
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
        PersonalTransaction item = items.get(position);
        holder.titleText.setText(item.getTitle());
        holder.subtitleText.setText(item.getDate() + " • " + item.getNote());
        
        String prefix = item.isIncome() ? "+" : "-";
        holder.amountText.setText(prefix + formatter.format(item.getAmount()) + " VND");
        
        holder.amountText.setTextColor(holder.itemView.getResources().getColor(
                item.isIncome() ? R.color.income_green : R.color.expense_red,
                null
        ));
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
