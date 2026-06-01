package com.uit.minhho.financetracker.fragment.personal;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.adapter.personal.BudgetAdapter;
import com.uit.minhho.financetracker.data.local.entity.Category;
import com.uit.minhho.financetracker.viewmodel.BudgetViewModel;
import com.uit.minhho.financetracker.viewmodel.CategoryViewModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BudgetFragment extends Fragment {
    private BudgetAdapter adapter;
    private BudgetViewModel budgetViewModel;
    private CategoryViewModel categoryViewModel;
    private List<com.uit.minhho.financetracker.data.local.entity.Budget> currentBudgets = new ArrayList<>();
    private Map<Integer, String> categoryNamesById = new HashMap<>();

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

        adapter = new BudgetAdapter(new ArrayList<>(), this::confirmDeleteBudget);
        rv.setAdapter(adapter);

        budgetViewModel = new ViewModelProvider(requireActivity()).get(BudgetViewModel.class);
        categoryViewModel = new ViewModelProvider(requireActivity()).get(CategoryViewModel.class);
        categoryViewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            categoryNamesById = new HashMap<>();
            if (categories != null) {
                for (Category category : categories) {
                    categoryNamesById.put(category.getId(), category.getName());
                }
            }
            renderBudgets();
        });
        budgetViewModel.getAllBudgets().observe(getViewLifecycleOwner(), budgets -> {
            currentBudgets = budgets == null ? new ArrayList<>() : budgets;
            renderBudgets();
        });

        // CẬP NHẬT: Gọi Fragment thêm ngân sách
        view.findViewById(R.id.fab_add_budget).setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, 
                                       android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                    .replace(R.id.fragment_container, new AddBudgetFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (budgetViewModel != null) {
            budgetViewModel.refreshBudgets();
        }
        if (categoryViewModel != null) {
            categoryViewModel.refreshCategories();
        }
    }

    private void renderBudgets() {
        List<com.uit.minhho.financetracker.model.personal.Budget> displayBudgets = new ArrayList<>();
        for (com.uit.minhho.financetracker.data.local.entity.Budget budget : currentBudgets) {
            String categoryName = categoryNamesById.containsKey(budget.getCategoryId())
                    ? categoryNamesById.get(budget.getCategoryId())
                    : "Danh mục #" + budget.getCategoryId();
            displayBudgets.add(new com.uit.minhho.financetracker.model.personal.Budget(
                    budget.getId(),
                    String.valueOf(budget.getCategoryId()),
                    categoryName,
                    budget.getLimitAmount(),
                    budget.getSpentAmount()
            ));
        }
        adapter.setBudgets(displayBudgets);
    }

    private void confirmDeleteBudget(com.uit.minhho.financetracker.model.personal.Budget item) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa ngân sách")
                .setMessage("Bạn muốn xóa ngân sách " + item.getCategoryName() + "?")
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_delete, (dialog, which) -> deleteBudget(item))
                .show();
    }

    private void deleteBudget(com.uit.minhho.financetracker.model.personal.Budget item) {
        com.uit.minhho.financetracker.data.local.entity.Budget budget =
                new com.uit.minhho.financetracker.data.local.entity.Budget(
                        Integer.parseInt(item.getCategoryId()),
                        item.getLimitAmount(),
                        item.getSpentAmount(),
                        ""
                );
        budget.setId(item.getId());
        budgetViewModel.delete(budget, (success, message) -> {
            if (!isAdded()) {
                return;
            }
            requireActivity().runOnUiThread(() -> {
                if (!isAdded()) {
                    return;
                }
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                if (success) {
                    budgetViewModel.refreshBudgets();
                }
            });
        });
    }
}
