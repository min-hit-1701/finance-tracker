package com.uit.minhho.financetracker.adapter.personal;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.model.personal.PersonalTransaction;
import java.util.List;

public class PersonalTransactionAdapter extends RecyclerView.Adapter<PersonalTransactionAdapter.ViewHolder> {

    public interface OnTransactionActionListener {
        void onTransactionClick(PersonalTransaction transaction);
        void onTransactionLongClick(PersonalTransaction transaction);
    }

    private final List<PersonalTransaction> items;
    private final OnTransactionActionListener actionListener;

    public PersonalTransactionAdapter(List<PersonalTransaction> items) {
        this(items, null);
    }

    public PersonalTransactionAdapter(List<PersonalTransaction> items, OnTransactionActionListener actionListener) {
        this.items = items;
        this.actionListener = actionListener;
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
        
        // Màu sắc cho số tiền (Xanh cho thu nhập, Đỏ cho chi tiêu)
        int amountColorRes = item.isIncome() ? R.color.income_green : R.color.expense_red;
        holder.tvAmount.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), amountColorRes));

        // Logic gán Icon và Màu nền (Đồng bộ với Category)
        int iconRes = R.drawable.ic_other;
        int colorRes = R.color.cat_other;
        
        if (item.getIconType() != null) {
            String type = item.getIconType().toLowerCase();
            try {
                int parsedIcon = Integer.parseInt(type);
                if (parsedIcon > 0) {
                    iconRes = parsedIcon;
                }
            } catch (NumberFormatException ignored) {
            }

            if (type.contains("food")) {
                iconRes = R.drawable.ic_food;
                colorRes = R.color.cat_food;
            } else if (type.contains("salary")) {
                iconRes = R.drawable.ic_salary;
                colorRes = R.color.cat_salary;
            } else if (type.contains("transport")) {
                iconRes = R.drawable.ic_transport;
                colorRes = R.color.cat_transport;
            } else if (type.contains("shopping")) {
                iconRes = R.drawable.ic_shopping;
                colorRes = R.color.cat_shopping;
            } else if (type.contains("utility")) {
                iconRes = R.drawable.ic_utility;
                colorRes = R.color.cat_utility;
            }
        }
        
        holder.ivIcon.setImageResource(iconRes);
        
        // Nâng cấp: Đổi màu nền icon động để tạo vẻ Premium giống Business
        int bgColor = ContextCompat.getColor(holder.itemView.getContext(), colorRes);
        holder.iconContainer.setBackgroundTintList(ColorStateList.valueOf(bgColor));
        // Đặt icon màu trắng để nổi bật trên nền màu
        holder.ivIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.white)));
        holder.itemView.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onTransactionClick(item);
            }
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (actionListener != null) {
                actionListener.onTransactionLongClick(item);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSubtitle, tvAmount;
        ImageView ivIcon;
        View iconContainer;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvSubtitle = itemView.findViewById(R.id.tv_subtitle);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            ivIcon = itemView.findViewById(R.id.iv_category_icon);
            iconContainer = itemView.findViewById(R.id.icon_container);
        }
    }
}
