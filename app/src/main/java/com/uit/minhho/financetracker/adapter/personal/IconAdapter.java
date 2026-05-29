package com.uit.minhho.financetracker.adapter.personal;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.uit.minhho.financetracker.R;
import java.util.List;

public class IconAdapter extends RecyclerView.Adapter<IconAdapter.ViewHolder> {

    private final List<Integer> icons;
    private final OnIconClickListener listener;

    public interface OnIconClickListener {
        void onIconClick(int iconResId);
    }

    public IconAdapter(List<Integer> icons, OnIconClickListener listener) {
        this.icons = icons;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_icon_picker, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int iconResId = icons.get(position);
        holder.ivIcon.setImageResource(iconResId);
        int tint = holder.itemView.getContext().getColor(R.color.brand_primary);
        ImageViewCompat.setImageTintList(holder.ivIcon, ColorStateList.valueOf(tint));
        holder.itemView.setOnClickListener(v -> listener.onIconClick(iconResId));
    }

    @Override
    public int getItemCount() {
        return icons.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_icon);
        }
    }
}