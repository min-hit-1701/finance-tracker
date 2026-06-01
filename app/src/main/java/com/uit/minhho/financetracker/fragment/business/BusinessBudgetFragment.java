package com.uit.minhho.financetracker.fragment.business;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.data.local.entity.Budget;
import com.uit.minhho.financetracker.viewmodel.BusinessViewModel;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class BusinessBudgetFragment extends Fragment {

    private BusinessViewModel businessViewModel;
    private LinearLayout itemsContainer;
    private View emptyView;
    private final DecimalFormat amountFormatter = new DecimalFormat("#,###");
    private final List<Budget> currentBudgets = new ArrayList<>();
    private double totalBalance = 0;

    public BusinessBudgetFragment() {
        super(R.layout.fragment_business_budget);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        businessViewModel = new ViewModelProvider(requireActivity()).get(BusinessViewModel.class);
        itemsContainer = view.findViewById(R.id.budget_items_container);
        emptyView = view.findViewById(R.id.tv_business_budget_empty);

        businessViewModel.getBusinessBudgets().observe(getViewLifecycleOwner(), budgets -> {
            currentBudgets.clear();
            if (budgets != null) currentBudgets.addAll(budgets);
            renderBudgets(budgets);
        });

        businessViewModel.getBusinessTotalBalance().observe(getViewLifecycleOwner(), bal -> {
            if (bal != null) totalBalance = bal;
        });

        getParentFragmentManager().setFragmentResultListener(
                BusinessAddBudgetFragment.REQUEST_KEY,
                getViewLifecycleOwner(),
                (requestKey, result) -> {
                    String name = result.getString(BusinessAddBudgetFragment.KEY_NAME, "");
                    int limit = result.getInt(BusinessAddBudgetFragment.KEY_LIMIT, 0);
                    int editId = result.getInt("edit_id", 0);

                    double existingTotal = 0;
                    for (Budget b : currentBudgets) {
                        if (editId > 0 && b.getId() == editId) continue;
                        existingTotal += b.getLimitAmount();
                    }
                    double newTotal = existingTotal + limit;

                    if (limit > totalBalance) {
                        new MaterialAlertDialogBuilder(requireContext())
                                .setTitle(R.string.business_error_title)
                                .setMessage("Hạn mức " + amountFormatter.format(limit) + " vượt quá số dư ví (" + amountFormatter.format(totalBalance) + "). Không thể tạo.")
                                .setPositiveButton("OK", null).show();
                        return;
                    }
                    if (editId <= 0 && newTotal > totalBalance) {
                        new MaterialAlertDialogBuilder(requireContext())
                                .setTitle(R.string.business_error_title)
                                .setMessage("Tổng hạn mức ngân sách (" + amountFormatter.format(newTotal) + ") vượt quá số dư ví (" + amountFormatter.format(totalBalance) + "). Không thể tạo.")
                                .setPositiveButton("OK", null).show();
                        return;
                    }
                    if (editId <= 0 && existingTotal > 0 && newTotal >= totalBalance * 0.9) {
                        new MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Cảnh báo")
                                .setMessage("Tổng hạn mức đã chiếm " + (int)((newTotal / totalBalance) * 100) + "% số dư ví. Bạn có chắc muốn tiếp tục?")
                                .setPositiveButton("Tiếp tục", (d, w) -> businessViewModel.addBusinessBudget(name, limit, 0, buildCurrentPeriod()))
                                .setNegativeButton("Hủy", null).show();
                        return;
                    }

                    int used = result.getInt(BusinessAddBudgetFragment.KEY_USED, 0);
                    if (editId > 0) {
                        businessViewModel.updateBusinessBudget(editId, name, limit, used, buildCurrentPeriod());
                    } else {
                        businessViewModel.addBusinessBudget(name, limit, used, buildCurrentPeriod());
                    }
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

    private void renderBudgets(List<Budget> budgets) {
        itemsContainer.removeAllViews();
        if (budgets == null || budgets.isEmpty()) {
            if (emptyView != null) emptyView.setVisibility(View.VISIBLE);
            return;
        }
        if (emptyView != null) emptyView.setVisibility(View.GONE);

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (Budget item : budgets) {
            View itemView = inflater.inflate(R.layout.item_business_budget, itemsContainer, false);
            TextView titleText = itemView.findViewById(R.id.tv_business_budget_name);
            TextView usageText = itemView.findViewById(R.id.tv_business_budget_usage);
            ProgressBar progressBar = itemView.findViewById(R.id.pb_business_budget);

            String budgetName = item.getName() == null || item.getName().trim().isEmpty()
                    ? getString(R.string.business_budget_default_name)
                    : item.getName();
            titleText.setText(budgetName);
            usageText.setText(getString(R.string.business_budget_usage_format,
                    amountFormatter.format(item.getSpentAmount()),
                    amountFormatter.format(item.getLimitAmount())));
            progressBar.setProgress(getProgress(item));

            itemView.findViewById(R.id.btn_edit_budget).setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putInt("edit_id", item.getId());
                args.putString("edit_name", budgetName);
                args.putInt("edit_limit", (int) item.getLimitAmount());
                openEditChildScreen(new BusinessAddBudgetFragment(), args);
            });

            itemView.findViewById(R.id.btn_delete_budget).setOnClickListener(v ->
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle(R.string.action_delete)
                            .setMessage(getString(R.string.business_delete_confirm, budgetName))
                            .setPositiveButton(R.string.action_delete, (d, w) ->
                                    businessViewModel.deleteBusinessBudget(item))
                            .setNegativeButton(R.string.action_cancel, null)
                            .show()
            );

            itemsContainer.addView(itemView);
        }
    }

    private int getProgress(Budget budget) {
        if (budget.getLimitAmount() <= 0) return 0;
        return Math.min(100, (int) ((budget.getSpentAmount() * 100f) / budget.getLimitAmount()));
    }

    private String buildCurrentPeriod() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1);
    }

    private void openChildScreen(Fragment fragment) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragment_container_business, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void openEditChildScreen(Fragment fragment, Bundle args) {
        fragment.setArguments(args);
        openChildScreen(fragment);
    }
}
