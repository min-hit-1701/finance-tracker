package com.uit.minhho.financetracker.adapter.business;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.model.business.BusinessWallet;

import java.util.List;

public class BusinessWalletAdapter extends RecyclerView.Adapter<BusinessWalletAdapter.ViewHolder> {

    private final List<BusinessWallet> items;

    public BusinessWalletAdapter(List<BusinessWallet> items) {
        this.items = items;
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
        BusinessWallet item = items.get(position);
        holder.nameText.setText(item.getName());
        holder.balanceText.setText(item.getBalance());
        holder.noteText.setText(
                holder.itemView.getResources().getString(R.string.business_wallet_note_format, item.getNote())
        );
    }

    @Override
    public int getItemCount() {
        return items.size();
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
