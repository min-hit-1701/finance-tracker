package com.uit.minhho.financetracker.adapter.business;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.data.local.entity.BusinessContact;

import java.util.ArrayList;
import java.util.List;

public class BusinessEntityAdapter extends RecyclerView.Adapter<BusinessEntityAdapter.ViewHolder> {

    private final List<BusinessContact> items = new ArrayList<>();

    public BusinessEntityAdapter() {
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_business_entity, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BusinessContact item = items.get(position);
        holder.nameText.setText(item.getName());
        holder.detailText.setText(
                holder.itemView.getResources().getString(
                        R.string.business_item_detail_format,
                        item.getType(),
                        item.getNote()
                )
        );
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateData(List<BusinessContact> newList) {
        items.clear();
        if (newList != null) {
            items.addAll(newList);
        }
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView nameText;
        final TextView detailText;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.tv_business_name);
            detailText = itemView.findViewById(R.id.tv_business_detail);
        }
    }
}
