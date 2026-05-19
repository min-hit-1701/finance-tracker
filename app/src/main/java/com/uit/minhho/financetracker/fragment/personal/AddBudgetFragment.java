package com.uit.minhho.financetracker.fragment.personal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.data.local.entity.Budget;
import com.uit.minhho.financetracker.viewmodel.BudgetViewModel;

public class AddBudgetFragment extends Fragment {

    private BudgetViewModel budgetViewModel;
    private TextInputEditText etBudgetAmount;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_personal_add_budget, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ánh xạ ô nhập số tiền hạn mức ngân sách
        etBudgetAmount = view.findViewById(R.id.et_budget_amount);

        // Khởi tạo BudgetViewModel để kết nối dữ liệu xuống Database của nhóm
        budgetViewModel = new ViewModelProvider(this).get(BudgetViewModel.class);

        // Điều hướng quay lại khi bấm nút đóng Toolbar
        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());
        }

        // Xử lý sự kiện bấm nút Lưu ngân sách mới dựa trên ID từ file XML của nhóm bạn
        View btnSave = view.findViewById(R.id.btn_save_budget);
        if (btnSave != null) {
            btnSave.setOnClickListener(v -> saveBudgetLogic());
        }
    }

    private void saveBudgetLogic() {
        if (etBudgetAmount == null) return;

        String amountStr = etBudgetAmount.getText().toString().trim();

        if (amountStr.isEmpty()) {
            etBudgetAmount.setError("Vui lòng nhập số tiền ngân sách!");
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);

            // 1. Tạm thời gán ID danh mục (CategoryId) mặc định = 1 (Ví dụ: Ăn uống)
            int defaultCategoryId = 1;

            // 2. Số tiền đã chi tiêu ban đầu khi vừa tạo Ngân sách mới tinh chắc chắn là 0đ
            double currentUsage = 0.0;

            // 3. Chu kỳ thời hạn ngân sách mặc định là "Tháng này" theo giao diện thiết kế
            String period = "Tháng này";

            // SỬA LỖI TẠI ĐÂY: Triệu hồi Constructor truyền chính xác 4 tham số (int, double, double, String)
            // Khớp hoàn toàn với cú pháp quy định trong file Entity Budget của nhóm
            Budget newBudget = new Budget(defaultCategoryId, amount, currentUsage, period);

            // Triệu hồi ViewModel xử lý ghi xuống Room Database chạy ngầm
            budgetViewModel.insert(newBudget);

            // Thông báo thành công và tắt màn hình thêm mới để quay lại danh sách ngân sách
            Toast.makeText(getContext(), "Đã thiết lập hạn mức ngân sách thành công!", Toast.LENGTH_SHORT).show();
            getParentFragmentManager().popBackStack();

        } catch (NumberFormatException e) {
            etBudgetAmount.setError("Số tiền nhập vào không hợp lệ!");
        }
    }
}