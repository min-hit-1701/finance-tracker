package com.uit.minhho.financetracker.adapter.personal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.data.local.entity.Category;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private final List<Category> items;
    private final DecimalFormat moneyFormatter = new DecimalFormat("#,### đ");
    private Map<Integer, Double> categoryTotals = new HashMap<>();
    private OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
        void onCategoryLongClick(Category category);
    }

    public CategoryAdapter(List<Category> items, OnCategoryClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void setCategories(List<Category> categories) {
        items.clear();
        if (categories != null) {
            items.addAll(categories);
        }
        notifyDataSetChanged();
    }

    public void setCategoryTotals(Map<Integer, Double> totals) {
        categoryTotals = totals == null ? new HashMap<>() : totals;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_personal_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category item = items.get(position);
        holder.nameText.setText(item.getName());

        String typeLabel = holder.itemView.getContext().getString(
                item.isIncome() ? R.string.category_income : R.string.category_expense
        );
        holder.typeText.setText(typeLabel);

        double total = categoryTotals.containsKey(item.getId()) ? categoryTotals.get(item.getId()) : 0.0;
        String totalPrefix = item.isIncome() ? "Đã nhận: " : "Đã dùng: ";
        holder.totalText.setText(totalPrefix + moneyFormatter.format(total));

        int iconRes = R.drawable.ic_other;
        String name = item.getName().toLowerCase();

        if (name.contains("ăn uống")) iconRes = R.drawable.ic_food;
        else if (name.contains("di chuyển")) iconRes = R.drawable.ic_transport;
        else if (name.contains("mua sắm")) iconRes = R.drawable.ic_shopping;
        else if (name.contains("hóa đơn") || name.contains("tiện ích")) iconRes = R.drawable.ic_utility;
        else if (name.contains("giải trí")) iconRes = R.drawable.ic_entertainment;
        else if (name.contains("sức khỏe")) iconRes = R.drawable.ic_health;
        else if (name.contains("giáo dục")) iconRes = R.drawable.ic_education;
        else if (name.contains("lương")) iconRes = R.drawable.ic_salary;
        else if (name.contains("đầu tư")) iconRes = R.drawable.ic_investment;
        else if (name.contains("nhà cửa")) iconRes = R.drawable.ic_utility;

        holder.iconView.setImageResource(iconRes);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onCategoryClick(item);
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onCategoryLongClick(item);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, typeText, totalText;
        ImageView iconView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.category_name);
            typeText = itemView.findViewById(R.id.category_type);
            totalText = itemView.findViewById(R.id.category_total);
            iconView = itemView.findViewById(R.id.category_icon);
        }
    }
}
