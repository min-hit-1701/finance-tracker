package com.uit.minhho.financetracker.adapter.personal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.model.personal.PersonalTransaction;
import java.util.List;

public class PersonalTransactionAdapter extends RecyclerView.Adapter<PersonalTransactionAdapter.ViewHolder> {

    private final List<PersonalTransaction> items;

    public PersonalTransactionAdapter(List<PersonalTransaction> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_personal_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PersonalTransaction item = items.get(position);
        
        holder.tvTitle.setText(item.getTitle());
        holder.tvSubtitle.setText(item.getSubtitle());
        holder.tvAmount.setText(item.getAmount());
        
        int colorRes = item.isIncome() ? R.color.income_green : R.color.expense_red;
        holder.tvAmount.setTextColor(holder.itemView.getContext().getResources().getColor(colorRes, null));

        int iconRes = R.drawable.ic_other;
        if (item.getIconType() != null) {
            switch (item.getIconType().toLowerCase()) {
                case "food":
                    iconRes = R.drawable.ic_food;
                    break;
                case "salary":
                    iconRes = R.drawable.ic_salary;
                    break;
                case "transport":
                    iconRes = R.drawable.ic_transport;
                    break;
                case "shopping":
                    iconRes = R.drawable.ic_shopping;
                    break;
                case "utility":
                case "home":
                case "bill":
                    iconRes = R.drawable.ic_utility;
                    break;
                case "entertainment":
                    iconRes = R.drawable.ic_entertainment;
                    break;
                case "health":
                    iconRes = R.drawable.ic_health;
                    break;
                case "education":
                    iconRes = R.drawable.ic_education;
                    break;
                case "investment":
                    iconRes = R.drawable.ic_investment;
                    break;
            }
        }
        
        holder.ivIcon.setImageResource(iconRes);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSubtitle, tvAmount;
        ImageView ivIcon;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvSubtitle = itemView.findViewById(R.id.tv_subtitle);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            ivIcon = itemView.findViewById(R.id.iv_category_icon);
        }
    }
}