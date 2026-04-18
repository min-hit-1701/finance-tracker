package com.uit.minhho.financetracker.fragment.personal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.adapter.personal.CategoryAdapter;
import com.uit.minhho.financetracker.model.personal.Category;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CategoryFragment extends Fragment {

    private CategoryAdapter adapter;
    private List<Category> categoryList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_personal_category, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        categoryList = getInitialCategories();
        
        RecyclerView recyclerView = view.findViewById(R.id.rv_categories);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        adapter = new CategoryAdapter(categoryList);
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.fab_add_category).setOnClickListener(v -> showAddCategoryDialog());
    }

    private void showAddCategoryDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_category, null);
        EditText etName = dialogView.findViewById(R.id.et_category_name);
        MaterialButtonToggleGroup toggleType = dialogView.findViewById(R.id.toggle_category_type);

        new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    if (!name.isEmpty()) {
                        boolean isIncome = toggleType.getCheckedButtonId() == R.id.btn_type_income;
                        addCategory(name, isIncome);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addCategory(String name, boolean isIncome) {
        // Create a new category with a random ID and a default icon
        Category newCategory = new Category(
                UUID.randomUUID().toString(),
                name,
                isIncome ? android.R.drawable.ic_menu_call : android.R.drawable.ic_menu_gallery,
                isIncome
        );
        
        categoryList.add(newCategory);
        adapter.notifyItemInserted(categoryList.size() - 1);
    }

    private List<Category> getInitialCategories() {
        List<Category> list = new ArrayList<>();
        // Expenses
        list.add(new Category("1", "Food", android.R.drawable.ic_menu_gallery, false));
        list.add(new Category("2", "Transport", android.R.drawable.ic_menu_directions, false));
        list.add(new Category("3", "Shopping", android.R.drawable.ic_menu_send, false));
        list.add(new Category("4", "Bills", android.R.drawable.ic_menu_agenda, false));
        
        // Income
        list.add(new Category("5", "Salary", android.R.drawable.ic_menu_call, true));
//        list.add(new Category("6", "Bonus", android.R.drawable.ic_menu_star, true));
        
        return list;
    }
}
