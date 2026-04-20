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

        RecyclerView rv = view.findViewById(R.id.rv_budgets);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        List<Budget> list = new ArrayList<>();
        // Dữ liệu thực tế theo yêu cầu UX
        list.add(new Budget("1", "Ăn uống", 3000000, 2200000));
        list.add(new Budget("2", "Di chuyển", 1000000, 1200000));
        list.add(new Budget("3", "Mua sắm", 2000000, 1000000));
        list.add(new Budget("4", "Giải trí", 1500000, 1400000));

        rv.setAdapter(new BudgetAdapter(list));
    }
}