package com.uit.minhho.financetracker.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.uit.minhho.financetracker.data.repository.AppRepository;
import com.uit.minhho.financetracker.data.local.entity.Budget;
import java.util.List;

public class BudgetViewModel extends AndroidViewModel {

    private final AppRepository repository;
    private final LiveData<List<Budget>> allBudgets;

    public BudgetViewModel(@NonNull Application application) {
        super(application);
        repository = new AppRepository(application);
        allBudgets = repository.getAllBudgets();
    }

    // Hàm lấy toàn bộ danh sách hạn mức ngân sách đã lập
    public LiveData<List<Budget>> getAllBudgets() {
        return allBudgets;
    }

    // Hàm thêm một kế hoạch ngân sách mới (ví dụ: Tháng này chỉ tiêu tối đa 5 triệu cho ăn uống)
    public void insert(Budget budget) {
        repository.insertBudget(budget);
    }
}