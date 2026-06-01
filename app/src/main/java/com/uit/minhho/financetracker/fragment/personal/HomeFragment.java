package com.uit.minhho.financetracker.fragment.personal;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.adapter.personal.PersonalTransactionAdapter;
import com.uit.minhho.financetracker.data.local.entity.Category;
import com.uit.minhho.financetracker.data.local.entity.Transaction;
import com.uit.minhho.financetracker.viewmodel.CategoryViewModel;
import com.uit.minhho.financetracker.viewmodel.TransactionViewModel;
import com.uit.minhho.financetracker.viewmodel.WalletViewModel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment {

    // BACKEND: Khai báo biến ViewModel để quản lý và lắng nghe dữ liệu giao dịch từ Database
    private TransactionViewModel transactionViewModel;
    private WalletViewModel walletViewModel;
    private CategoryViewModel categoryViewModel;
    private RecyclerView rvTransactions;
    private PersonalTransactionAdapter transactionAdapter;
    private TextView totalBalanceText;
    private TextView totalIncomeText;
    private TextView totalExpenseText;
    private List<Transaction> currentTransactions = new ArrayList<>();
    private Map<Integer, Integer> categoryIconsById = new HashMap<>();
    private Map<Integer, String> categoryNamesById = new HashMap<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_personal_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo RecyclerView hiển thị danh sách
        rvTransactions = view.findViewById(R.id.rv_recent_transactions);
        rvTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        rvTransactions.setNestedScrollingEnabled(false);
        transactionAdapter = new PersonalTransactionAdapter(new ArrayList<>(), new PersonalTransactionAdapter.OnTransactionActionListener() {
            @Override
            public void onTransactionClick(com.uit.minhho.financetracker.model.personal.PersonalTransaction transaction) {
                openTransactionDetail(transaction);
            }

            @Override
            public void onTransactionLongClick(com.uit.minhho.financetracker.model.personal.PersonalTransaction transaction) {
                confirmDeleteTransaction(transaction);
            }
        });
        rvTransactions.setAdapter(transactionAdapter);
        totalBalanceText = view.findViewById(R.id.tv_total_balance);
        totalIncomeText = view.findViewById(R.id.tv_total_income);
        totalExpenseText = view.findViewById(R.id.tv_total_expense);

        // BACKEND: Khởi tạo ViewModel đúng kiến trúc MVVM [cite: 62]
        transactionViewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);
        walletViewModel = new ViewModelProvider(requireActivity()).get(WalletViewModel.class);
        categoryViewModel = new ViewModelProvider(requireActivity()).get(CategoryViewModel.class);

        walletViewModel.getTotalBalance(false).observe(getViewLifecycleOwner(), total ->
                totalBalanceText.setText(formatMoney(total == null ? 0.0 : total))
        );
        transactionViewModel.getTotalIncome(false).observe(getViewLifecycleOwner(), total ->
                totalIncomeText.setText("+" + formatMoney(total == null ? 0.0 : total))
        );
        transactionViewModel.getTotalExpense(false).observe(getViewLifecycleOwner(), total ->
                totalExpenseText.setText("-" + formatMoney(total == null ? 0.0 : total))
        );
        categoryViewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            categoryIconsById = new HashMap<>();
            categoryNamesById = new HashMap<>();
            if (categories != null) {
                for (Category category : categories) {
                    categoryIconsById.put(category.getId(), category.getIconRes());
                    categoryNamesById.put(category.getId(), category.getName());
                }
            }
            renderTransactions();
        });

        // THẦN CHÚ LẮNG NGHE DATA:
        // Gọi hàm getTransactions(false) - truyền false vì đây là luồng cá nhân (Personal), không phải Business
        transactionViewModel.getTransactions(false).observe(getViewLifecycleOwner(), transactions -> {
            currentTransactions = transactions == null ? new ArrayList<>() : transactions;
            renderTransactions();
        });

        // KẾT NỐI NAVIGATION: Mở màn hình Thêm giao dịch khi nhấn vào FAB [cite: 875]
        view.findViewById(R.id.fab_add_transaction).setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right,
                            android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                    .replace(R.id.fragment_container, new AddTransactionFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (transactionViewModel != null) {
            transactionViewModel.refreshTransactions(false);
        }
        if (walletViewModel != null) {
            walletViewModel.refreshPersonalWallets();
        }
        if (categoryViewModel != null) {
            categoryViewModel.refreshCategories();
        }
    }

    private void renderTransactions() {
        List<com.uit.minhho.financetracker.model.personal.PersonalTransaction> displayList = new ArrayList<>();
        for (Transaction tx : currentTransactions) {
            String amountText = (tx.isIncome() ? "+" : "-") + formatMoney(tx.getAmount());
            int iconRes = tx.getIconRes() > 0
                    ? tx.getIconRes()
                    : (categoryIconsById.containsKey(tx.getCategoryId()) ? categoryIconsById.get(tx.getCategoryId()) : R.drawable.ic_other);
            String timeText = dateFormat.format(new Date(tx.getTimestamp()));
            String categoryName = categoryNamesById.containsKey(tx.getCategoryId())
                    ? categoryNamesById.get(tx.getCategoryId())
                    : "";
            displayList.add(new com.uit.minhho.financetracker.model.personal.PersonalTransaction(
                    tx.getId(),
                    tx.getNote() != null && !tx.getNote().isEmpty() ? tx.getNote() : "Giao dịch",
                    timeText,
                    amountText,
                    tx.isIncome(),
                    String.valueOf(iconRes),
                    categoryName,
                    timeText
            ));
        }
        transactionAdapter = new PersonalTransactionAdapter(displayList, new PersonalTransactionAdapter.OnTransactionActionListener() {
            @Override
            public void onTransactionClick(com.uit.minhho.financetracker.model.personal.PersonalTransaction transaction) {
                openTransactionDetail(transaction);
            }

            @Override
            public void onTransactionLongClick(com.uit.minhho.financetracker.model.personal.PersonalTransaction transaction) {
                confirmDeleteTransaction(transaction);
            }
        });
        rvTransactions.setAdapter(transactionAdapter);
    }

    private void openTransactionDetail(com.uit.minhho.financetracker.model.personal.PersonalTransaction item) {
        getParentFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right,
                        android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .replace(R.id.fragment_container, PersonalTransactionDetailFragment.newInstance(item))
                .addToBackStack(null)
                .commit();
    }

    private void confirmDeleteTransaction(com.uit.minhho.financetracker.model.personal.PersonalTransaction item) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_transaction_title)
                .setMessage(R.string.delete_transaction_message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_delete, (dialog, which) -> deleteTransaction(item))
                .show();
    }

    private void deleteTransaction(com.uit.minhho.financetracker.model.personal.PersonalTransaction item) {
        Transaction transaction = new Transaction(0, 0, "", 0, 0, item.isIncome(), false);
        transaction.setId(item.getId());
        transactionViewModel.delete(transaction, (success, message) -> {
            if (!isAdded()) {
                return;
            }
            requireActivity().runOnUiThread(() -> {
                if (!isAdded()) {
                    return;
                }
                Toast.makeText(
                        requireContext(),
                        success ? getString(R.string.delete_transaction_success) : message,
                        Toast.LENGTH_SHORT
                ).show();
                if (success) {
                    transactionViewModel.refreshTransactions(false);
                    walletViewModel.refreshPersonalWallets();
                    categoryViewModel.refreshCategories();
                }
            });
        });
    }

    private String formatMoney(double amount) {
        return String.format(Locale.US, "%,.0f đ", amount);
    }
}
