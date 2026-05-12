package com.uit.minhho.financetracker.fragment.business;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.adapter.business.BusinessBudgetAdapter;
import com.uit.minhho.financetracker.viewmodel.BusinessViewModel;

import java.util.ArrayList;
import java.util.Calendar;

public class BusinessBudgetFragment extends Fragment {

    private BusinessBudgetAdapter budgetAdapter;
    private BusinessViewModel businessViewModel;

    public BusinessBudgetFragment() {
        super(R.layout.fragment_business_budget);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        businessViewModel = new ViewModelProvider(requireActivity()).get(BusinessViewModel.class);

        RecyclerView recyclerView = view.findViewById(R.id.rv_business_budgets);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        budgetAdapter = new BusinessBudgetAdapter(new ArrayList<>());
        recyclerView.setAdapter(budgetAdapter);

        businessViewModel.getBusinessBudgets().observe(getViewLifecycleOwner(), budgets -> {
            budgetAdapter.submitItems(budgets);
            View emptyView = view.findViewById(R.id.tv_business_budget_empty);
            if (emptyView != null) {
                emptyView.setVisibility(budgets == null || budgets.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });

        getParentFragmentManager().setFragmentResultListener(
                BusinessAddBudgetFragment.REQUEST_KEY,
                getViewLifecycleOwner(),
                (requestKey, result) -> {
                    String name = result.getString(BusinessAddBudgetFragment.KEY_NAME, "");
                    int limit = result.getInt(BusinessAddBudgetFragment.KEY_LIMIT, 0);
                    int used = result.getInt(BusinessAddBudgetFragment.KEY_USED, 0);
                    businessViewModel.addBusinessBudget(name, limit, used, buildCurrentPeriod());
                }
        );

        businessViewModel.getOperationMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.trim().isEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                businessViewModel.clearOperationMessage();
            }
        });

        view.findViewById(R.id.btn_create_business_budget).setOnClickListener(v ->
                openChildScreen(new BusinessAddBudgetFragment())
        );
        view.findViewById(R.id.btn_back_business_budget).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );
    }

    private String buildCurrentPeriod() {
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);
        return year + "-" + month;
    }

    private void openChildScreen(Fragment fragment) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragment_container_business, fragment)
                .addToBackStack(null)
                .commit();
    }
}
