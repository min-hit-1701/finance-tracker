package com.uit.minhho.financetracker.fragment.personal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.adapter.personal.BudgetAdapter;
import com.uit.minhho.financetracker.model.personal.Budget;

import java.util.ArrayList;
import java.util.List;

public class BudgetFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_personal_budget, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.rv_budgets);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(new BudgetAdapter(getFakeBudgets()));

        view.findViewById(R.id.fab_add_budget).setOnClickListener(v -> {
            // Handle add budget
        });
    }

    private List<Budget> getFakeBudgets() {
        List<Budget> list = new ArrayList<>();
        // Normal budget
        list.add(new Budget("1", "Food & Dining", 5000000, 3200000));
        // Over budget case
        list.add(new Budget("2", "Shopping", 2000000, 2500000));
        // Near limit case
        list.add(new Budget("3", "Transport", 1000000, 950000));
        
        return list;
    }
}
