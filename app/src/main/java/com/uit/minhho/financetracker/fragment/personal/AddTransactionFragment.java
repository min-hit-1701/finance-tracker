package com.uit.minhho.financetracker.fragment.personal;

import android.app.AlertDialog;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.adapter.personal.IconAdapter;
import com.uit.minhho.financetracker.data.local.entity.Category;
import com.uit.minhho.financetracker.data.local.entity.Transaction;
import com.uit.minhho.financetracker.data.local.entity.Wallet;
import com.uit.minhho.financetracker.viewmodel.CategoryViewModel;
import com.uit.minhho.financetracker.viewmodel.TransactionViewModel;
import com.uit.minhho.financetracker.viewmodel.WalletViewModel;

import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.List;
import java.util.Map;

public class AddTransactionFragment extends Fragment {

    private EditText etDate, etAmount, etNote;
    private AutoCompleteTextView spinnerCategory, spinnerWallet;
    private ImageView ivTransactionIcon;
    private TextView tvTransactionIconHint;
    private LinearLayout iconSelector;
    private MaterialButtonToggleGroup toggleType;
    private WalletViewModel walletViewModel;
    private CategoryViewModel categoryViewModel;
    private TransactionViewModel transactionViewModel;
    private final Map<String, Integer> categoryIdsByName = new HashMap<>();
    private final Map<String, Integer> categoryIconsByName = new HashMap<>();
    private final Map<String, Integer> walletIdsByName = new HashMap<>();
    private int selectedIconResId = R.drawable.ic_other;
    private boolean userSelectedIcon = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_personal_add_transaction, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        walletViewModel = new ViewModelProvider(requireActivity()).get(WalletViewModel.class);
        categoryViewModel = new ViewModelProvider(requireActivity()).get(CategoryViewModel.class);
        transactionViewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);
        setupSpinners();
        setupAutomaticTimestampField();

        view.findViewById(R.id.btn_save).setOnClickListener(v -> saveTransactionLogic());
    }

    private void initViews(View view) {
        etDate = view.findViewById(R.id.et_date);
        etAmount = view.findViewById(R.id.et_amount);
        etNote = view.findViewById(R.id.et_note);
        spinnerCategory = view.findViewById(R.id.spinner_category);
        spinnerWallet = view.findViewById(R.id.spinner_wallet);
        ivTransactionIcon = view.findViewById(R.id.iv_transaction_icon);
        tvTransactionIconHint = view.findViewById(R.id.tv_transaction_icon_hint);
        iconSelector = view.findViewById(R.id.transaction_icon_selector);
        toggleType = view.findViewById(R.id.toggle_group_type);
        updateIconUi();
        iconSelector.setOnClickListener(v -> showIconPickerDialog());

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());
    }

    private void setupSpinners() {
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        spinnerCategory.setAdapter(catAdapter);
        observeCategories(false, catAdapter);
        spinnerCategory.setOnItemClickListener((parent, view, position, id) -> {
            String categoryName = spinnerCategory.getText().toString().trim();
            Integer categoryIcon = categoryIconsByName.get(categoryName);
            if (!userSelectedIcon && categoryIcon != null && categoryIcon > 0) {
                selectedIconResId = categoryIcon;
                updateIconUi();
            }
        });
        toggleType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                observeCategories(checkedId == R.id.btn_income, catAdapter);
                spinnerCategory.setText("", false);
                userSelectedIcon = false;
                selectedIconResId = R.drawable.ic_other;
                updateIconUi();
            }
        });

        ArrayAdapter<String> walletAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        spinnerWallet.setAdapter(walletAdapter);

        walletViewModel.getPersonalWallets().observe(getViewLifecycleOwner(), wallets -> {
            List<String> walletNames = new ArrayList<>();
            walletIdsByName.clear();
            if (wallets != null) {
                for (Wallet wallet : wallets) {
                    walletNames.add(wallet.getName());
                    walletIdsByName.put(wallet.getName(), wallet.getId());
                }
            }

            walletAdapter.clear();
            walletAdapter.addAll(walletNames);
            walletAdapter.notifyDataSetChanged();

            String selectedWallet = spinnerWallet.getText().toString();
            if (!walletNames.contains(selectedWallet)) {
                spinnerWallet.setText("", false);
            }
        });
    }

    private void setupAutomaticTimestampField() {
        etDate.setText(currentTimestampText());
        etDate.setFocusable(false);
        etDate.setClickable(false);
    }

    private void observeCategories(boolean isIncome, ArrayAdapter<String> adapter) {
        categoryViewModel.getCategoriesByType(isIncome).observe(getViewLifecycleOwner(), categories -> {
            categoryIdsByName.clear();
            categoryIconsByName.clear();
            adapter.clear();
            if (categories != null) {
                for (Category category : categories) {
                    categoryIdsByName.put(category.getName(), category.getId());
                    categoryIconsByName.put(category.getName(), category.getIconRes());
                    adapter.add(category.getName());
                }
            }
            adapter.notifyDataSetChanged();
        });
    }

    private void saveTransactionLogic() {
        String amountText = etAmount.getText().toString().trim().replace(",", "");
        String categoryName = spinnerCategory.getText().toString().trim();
        String walletName = spinnerWallet.getText().toString().trim();

        if (amountText.isEmpty()) {
            etAmount.setError("Vui lòng nhập số tiền!");
            return;
        }
        if (categoryName.isEmpty()) {
            spinnerCategory.setError("Vui lòng chọn danh mục!");
            return;
        }
        if (walletName.isEmpty()) {
            spinnerWallet.setError("Vui lòng chọn ví!");
            return;
        }

        Integer categoryId = categoryIdsByName.get(categoryName);
        Integer walletId = walletIdsByName.get(walletName);
        if (categoryId == null) {
            spinnerCategory.setError("Danh mục chưa có trong database!");
            return;
        }
        if (walletId == null) {
            spinnerWallet.setError("Ví chưa có trong database!");
            return;
        }

        try {
            double amount = Double.parseDouble(amountText);
            boolean isIncome = toggleType.getCheckedButtonId() == R.id.btn_income;
            String note = etNote.getText() == null ? "" : etNote.getText().toString().trim();
            long timestamp = selectedTimestamp();
            int iconToSave = selectedIconResId > 0 ? selectedIconResId : R.drawable.ic_other;
            Transaction transaction = new Transaction(amount, timestamp, note, categoryId, walletId, isIncome, false, iconToSave);
            View saveButton = getView() == null ? null : getView().findViewById(R.id.btn_save);
            if (saveButton != null) {
                saveButton.setEnabled(false);
            }
            Context appContext = requireContext().getApplicationContext();
            transactionViewModel.insert(transaction, (success, message) -> {
                Activity activity = getActivity();
                if (!isAdded() || activity == null) {
                    return;
                }
                activity.runOnUiThread(() -> {
                    if (!isAdded()) {
                        return;
                    }
                    if (saveButton != null) {
                        saveButton.setEnabled(true);
                    }
                    Toast.makeText(appContext, message, Toast.LENGTH_SHORT).show();
                    if (success) {
                        transactionViewModel.refreshTransactions(false);
                        walletViewModel.refreshPersonalWallets();
                        categoryViewModel.refreshCategories();
                        getParentFragmentManager().popBackStack();
                    }
                });
            });
        } catch (NumberFormatException e) {
            etAmount.setError("Số tiền không hợp lệ!");
        }
    }

    private void showIconPickerDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_icon_picker, null);
        RecyclerView rvIcons = dialogView.findViewById(R.id.rv_icons);
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.category_icon_picker_title)
                .setView(dialogView)
                .setNegativeButton(R.string.action_cancel, null)
                .create();

        IconAdapter adapter = new IconAdapter(iconOptions(), iconResId -> {
            selectedIconResId = iconResId;
            userSelectedIcon = true;
            updateIconUi();
            dialog.dismiss();
        });
        rvIcons.setLayoutManager(new GridLayoutManager(requireContext(), 4));
        rvIcons.setAdapter(adapter);
        dialog.show();
    }

    private List<Integer> iconOptions() {
        List<Integer> icons = new ArrayList<>();
        icons.add(R.drawable.ic_food);
        icons.add(R.drawable.ic_transport);
        icons.add(R.drawable.ic_shopping);
        icons.add(R.drawable.ic_utility);
        icons.add(R.drawable.ic_home);
        icons.add(R.drawable.ic_entertainment);
        icons.add(R.drawable.ic_health);
        icons.add(R.drawable.ic_education);
        icons.add(R.drawable.ic_salary);
        icons.add(R.drawable.ic_bonus);
        icons.add(R.drawable.ic_investment);
        icons.add(R.drawable.ic_other);
        return icons;
    }

    private void updateIconUi() {
        if (ivTransactionIcon == null) {
            return;
        }
        ivTransactionIcon.setImageResource(selectedIconResId);
        ImageViewCompat.setImageTintList(
                ivTransactionIcon,
                ColorStateList.valueOf(requireContext().getColor(R.color.brand_primary))
        );
        if (tvTransactionIconHint != null) {
            tvTransactionIconHint.setText(userSelectedIcon ? "Icon giao dịch đã chọn" : getString(R.string.category_icon_label));
        }
    }

    private long selectedTimestamp() {
        return System.currentTimeMillis();
    }

    private String currentTimestampText() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());
    }
}
