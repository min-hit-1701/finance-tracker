package com.uit.minhho.financetracker.adapter.personal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.model.personal.Wallet;

import java.util.List;

public class WalletAdapter extends RecyclerView.Adapter<WalletAdapter.ViewHolder> {

    private final List<Wallet> items;
    private OnWalletDeleteListener deleteListener;

    public interface OnWalletDeleteListener {
        void onDelete(int position);
    }

    public WalletAdapter(List<Wallet> items, OnWalletDeleteListener deleteListener) {
        this.items = items;
        this.deleteListener = deleteListener;
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
        
        // In a real app, you would map item.getType() or bank name to an icon
        // For now, let's use a generic card icon or based on name
        if (item.getName().toLowerCase().contains("momo")) {
            holder.iconImage.setImageResource(android.R.drawable.presence_online); // Dummy icon
        } else {
            holder.iconImage.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        holder.nameText.setText("Account");
        // Masking the ID to look like a card number suffix
        String maskedNumber = "**** **** **** " + (item.getId().length() > 4 ? item.getId().substring(item.getId().length() - 4) : "3884");
        holder.accountNumberText.setText(item.getName() + " " + maskedNumber);

        holder.deleteButton.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iconImage;
        TextView nameText;
        TextView accountNumberText;
        ImageButton deleteButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            iconImage = itemView.findViewById(R.id.wallet_icon);
            nameText = itemView.findViewById(R.id.wallet_name);
            accountNumberText = itemView.findViewById(R.id.wallet_account_number);
            deleteButton = itemView.findViewById(R.id.btn_delete_wallet);
        }
    }
}
