package com.uit.minhho.financetracker.fragment.business;

import android.app.Activity;
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

    private final List<BusinessBudgetItem> budgets = new ArrayList<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private BusinessBudgetAdapter budgetAdapter;
    private BusinessApiClient apiClient;

    public BusinessBudgetFragment() {
        super(R.layout.fragment_business_budget);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiClient = new BusinessApiClient(requireContext());

        getParentFragmentManager().setFragmentResultListener(
                BusinessAddBudgetFragment.REQUEST_KEY,
                getViewLifecycleOwner(),
                (requestKey, result) -> {
                    String name = result.getString(BusinessAddBudgetFragment.KEY_NAME, "");
                    int limit = result.getInt(BusinessAddBudgetFragment.KEY_LIMIT, 0);
                    int used = result.getInt(BusinessAddBudgetFragment.KEY_USED, 0);

                    saveBudgetToFirebase(name, used, limit);
                }
        );

        RecyclerView recyclerView = view.findViewById(R.id.rv_business_budgets);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        budgetAdapter = new BusinessBudgetAdapter(budgets);
        recyclerView.setAdapter(budgetAdapter);

        loadBudgets();

        view.findViewById(R.id.btn_create_business_budget).setOnClickListener(v ->
                openChildScreen(new BusinessAddBudgetFragment())
        );
        view.findViewById(R.id.btn_back_business_budget).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );
    }

    private void loadBudgets() {
        executorService.execute(() -> {
            List<BusinessBudgetItem> items = apiClient.getBusinessBudgets();
            Activity activity = getActivity();
            if (activity == null || !isAdded()) return;

            activity.runOnUiThread(() -> {
                if (!isAdded()) return;
                budgets.clear();
                budgets.addAll(items);
                budgetAdapter.notifyDataSetChanged();
            });
        });
    }

    private void saveBudgetToFirebase(String name, int used, int limit) {
        executorService.execute(() -> {
            BusinessApiClient.ApiResult result = apiClient.createBusinessBudget(name, used, limit);
            Activity activity = getActivity();
            if (activity == null || !isAdded()) return;

            activity.runOnUiThread(() -> {
                if (!isAdded()) return;
                if (result.success) {
                    Toast.makeText(requireContext(), R.string.business_budget_save_success, Toast.LENGTH_SHORT).show();
                    loadBudgets();
                } else {
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show();
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
