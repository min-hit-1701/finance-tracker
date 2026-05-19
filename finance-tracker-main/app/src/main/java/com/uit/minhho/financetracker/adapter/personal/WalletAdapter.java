package com.uit.minhho.financetracker.adapter.personal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.model.personal.Wallet;
import java.text.DecimalFormat;
import java.util.List;

public class WalletAdapter extends RecyclerView.Adapter<WalletAdapter.ViewHolder> {

    private final List<Wallet> items;
    private final OnWalletClickListener listener;

    public interface OnWalletClickListener {
        void onWalletClick(Wallet wallet);
        void onWalletLongClick(Wallet wallet);
    }

    public WalletAdapter(List<Wallet> items, OnWalletClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_personal_wallet, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Wallet item = items.get(position);
        
        holder.nameText.setText(item.getName());
        holder.typeText.setText(item.getType());
        
        DecimalFormat formatter = new DecimalFormat("#,### đ");
        holder.balanceText.setText(formatter.format(item.getBalance()));

        int iconRes = R.drawable.ic_wallet;
        if (item.getType().contains("Techcombank") || item.getType().contains("Ngân hàng")) {
            iconRes = R.drawable.ic_wallet; // Should have bank icon
        } else if (item.getType().contains("MoMo")) {
            iconRes = R.drawable.ic_wallet; // Should have e-wallet icon
        }

        holder.itemView.setOnClickListener(v -> listener.onWalletClick(item));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onWalletLongClick(item);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, typeText, balanceText;
        ImageView iconView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.wallet_name);
            typeText = itemView.findViewById(R.id.wallet_account_number); // Reusing ID for type/subtitle
            balanceText = itemView.findViewById(R.id.tv_wallet_balance); // Added in layout logic
            iconView = itemView.findViewById(R.id.iv_wallet_icon);
        }
    }
}