package com.uit.minhho.financetracker.fragment.personal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.adapter.personal.CategoryAdapter;
import com.uit.minhho.financetracker.data.local.entity.Category;
import com.uit.minhho.financetracker.viewmodel.CategoryViewModel;

import java.util.ArrayList;
import java.util.List;

public class CategoryFragment extends Fragment {

    private CategoryAdapter adapter;
    private CategoryViewModel categoryViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_personal_category, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        List<Category> categoryList = new ArrayList<>();

        RecyclerView recyclerView = view.findViewById(R.id.rv_categories);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        adapter = new CategoryAdapter(categoryList, category -> {
            // Xu ly khi nguoi dung nhan vao mot danh muc
        });
        recyclerView.setAdapter(adapter);

        categoryViewModel = new ViewModelProvider(requireActivity()).get(CategoryViewModel.class);
        categoryViewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> adapter.setCategories(categories));
        categoryViewModel.getCategoryUsageTotals().observe(getViewLifecycleOwner(), totals -> adapter.setCategoryTotals(totals));

        view.findViewById(R.id.fab_add_category).setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right,
                            android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                    .replace(R.id.fragment_container, new AddCategoryFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (categoryViewModel != null) {
            categoryViewModel.refreshCategories();
        }
    }
}
