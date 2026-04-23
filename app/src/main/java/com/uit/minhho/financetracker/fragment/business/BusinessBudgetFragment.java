package com.uit.minhho.financetracker.fragment.business;

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
import com.uit.minhho.financetracker.model.business.BusinessBudgetItem;

import java.util.ArrayList;
import java.util.List;

public class BusinessBudgetFragment extends Fragment {

    private final List<BusinessBudgetItem> budgets = new ArrayList<>();
    private BusinessBudgetAdapter budgetAdapter;

    public BusinessBudgetFragment() {
        super(R.layout.fragment_business_budget);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        getParentFragmentManager().setFragmentResultListener(
                BusinessAddBudgetFragment.REQUEST_KEY,
                getViewLifecycleOwner(),
                (requestKey, result) -> {
                    String name = result.getString(BusinessAddBudgetFragment.KEY_NAME, "");
                    int limit = result.getInt(BusinessAddBudgetFragment.KEY_LIMIT, 0);
                    int used = result.getInt(BusinessAddBudgetFragment.KEY_USED, 0);

                    budgets.add(0, new BusinessBudgetItem(name, used, limit));
                    budgetAdapter.notifyDataSetChanged();
                    Toast.makeText(requireContext(), R.string.business_budget_save_success, Toast.LENGTH_SHORT).show();
                }
        );

        RecyclerView recyclerView = view.findViewById(R.id.rv_business_budgets);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        if (budgets.isEmpty()) {
            budgets.add(new BusinessBudgetItem(
                    getString(R.string.business_budget_operations),
                    14800000,
                    20000000
            ));
            budgets.add(new BusinessBudgetItem(
                    getString(R.string.business_budget_marketing),
                    5600000,
                    10000000
            ));
            budgets.add(new BusinessBudgetItem(
                    getString(R.string.business_budget_payroll),
                    13600000,
                    20000000
            ));
        }

        budgetAdapter = new BusinessBudgetAdapter(budgets);
        recyclerView.setAdapter(budgetAdapter);

        view.findViewById(R.id.btn_create_business_budget).setOnClickListener(v ->
                openChildScreen(new BusinessAddBudgetFragment())
        );
        view.findViewById(R.id.btn_back_business_budget).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );
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
