package com.uit.minhho.financetracker.fragment.business;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.adapter.business.BusinessBudgetAdapter;
import com.uit.minhho.financetracker.data.remote.BusinessApiClient;
import com.uit.minhho.financetracker.model.business.BusinessBudgetItem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BusinessBudgetFragment extends Fragment {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final List<BusinessBudgetItem> budgets = new ArrayList<>();
    private BusinessBudgetAdapter budgetAdapter;
    private BusinessApiClient apiClient;

    public BusinessBudgetFragment() {
        super(R.layout.fragment_business_budget);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiClient = new BusinessApiClient(requireContext());

        RecyclerView recyclerView = view.findViewById(R.id.rv_business_budgets);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        budgetAdapter = new BusinessBudgetAdapter(budgets, this::confirmDeleteBudget);
        recyclerView.setAdapter(budgetAdapter);
        loadBudgets();

        view.findViewById(R.id.btn_create_business_budget).setOnClickListener(v ->
                openChildScreen(new BusinessAddBudgetFragment())
        );
        view.findViewById(R.id.btn_back_business_budget).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );
    }

    @Override
    public void onResume() {
        super.onResume();
        if (apiClient != null && budgetAdapter != null) {
            loadBudgets();
        }
    }

    private void loadBudgets() {
        executorService.execute(() -> {
            List<BusinessBudgetItem> loadedBudgets = apiClient.getBusinessBudgets();
            Activity activity = getActivity();
            if (!isAdded() || activity == null) {
                return;
            }

            activity.runOnUiThread(() -> {
                if (!isAdded()) {
                    return;
                }
                budgets.clear();
                budgets.addAll(loadedBudgets);
                budgetAdapter.notifyDataSetChanged();
            });
        });
    }

    private void confirmDeleteBudget(BusinessBudgetItem budget) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_business_budget_title)
                .setMessage(R.string.delete_business_budget_message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_delete, (dialog, which) -> deleteBudget(budget))
                .show();
    }

    private void deleteBudget(BusinessBudgetItem budget) {
        executorService.execute(() -> {
            BusinessApiClient.ApiResult<Void> result = apiClient.deleteBusinessBudget(budget);
            Activity activity = getActivity();
            if (!isAdded() || activity == null) {
                return;
            }

            activity.runOnUiThread(() -> {
                if (!isAdded()) {
                    return;
                }
                Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show();
                if (result.success) {
                    loadBudgets();
                }
            });
        });
    }

    private void openChildScreen(Fragment fragment) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragment_container_business, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
