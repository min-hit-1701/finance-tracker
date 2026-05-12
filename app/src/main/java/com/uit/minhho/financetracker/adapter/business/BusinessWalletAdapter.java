package com.uit.minhho.financetracker.adapter.business;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.data.local.entity.Wallet;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class BusinessWalletAdapter extends RecyclerView.Adapter<BusinessWalletAdapter.ViewHolder> {

    private final List<Wallet> items;
    private final DecimalFormat amountFormatter = new DecimalFormat("#,###");

    public BusinessWalletAdapter(List<Wallet> items) {
        this.items = new ArrayList<>(items);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_business_wallet, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Wallet item = items.get(position);
        holder.nameText.setText(item.getName());
        holder.balanceText.setText(holder.itemView.getResources().getString(
                R.string.business_wallet_balance_format,
                amountFormatter.format(item.getBalance())
        ));
        String note = extractNote(item.getType());
        holder.noteText.setText(
                holder.itemView.getResources().getString(R.string.business_wallet_note_format, note)
        );
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void submitItems(List<Wallet> wallets) {
        items.clear();
        items.addAll(wallets);
        notifyDataSetChanged();
    }

    private String extractNote(String typeField) {
        if (typeField == null) {
            return "";
        }
        String[] parts = typeField.split("\\|", 2);
        if (parts.length < 2) {
            return typeField.trim();
        }
        return parts[1].trim();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView nameText;
        final TextView balanceText;
        final TextView noteText;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.tv_business_wallet_name);
            balanceText = itemView.findViewById(R.id.tv_business_wallet_balance);
            noteText = itemView.findViewById(R.id.tv_business_wallet_note);
        }
    }
}
