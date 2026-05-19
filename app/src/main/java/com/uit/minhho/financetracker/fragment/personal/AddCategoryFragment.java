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
import com.uit.minhho.financetracker.data.local.entity.Category;
import com.uit.minhho.financetracker.viewmodel.CategoryViewModel;

public class AddCategoryFragment extends Fragment {

    private CategoryViewModel categoryViewModel;
    private TextInputEditText etCategoryName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_personal_add_category, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etCategoryName = view.findViewById(R.id.et_category_name);

        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());
        }

        View btnSave = view.findViewById(R.id.btn_save_category);
        if (btnSave != null) {
            btnSave.setOnClickListener(v -> saveCategoryLogic());
        }
    }

    private void saveCategoryLogic() {
        if (etCategoryName == null) return;

        String categoryName = etCategoryName.getText().toString().trim();

        if (categoryName.isEmpty()) {
            etCategoryName.setError("Vui lòng nhập tên danh mục!");
            return;
        }

        int defaultIconRes = 0;

        String categoryType = "EXPENSE";

        boolean isBusiness = false;

        Category newCategory = new Category(categoryName, defaultIconRes, categoryType, isBusiness);

        categoryViewModel.insert(newCategory);

        Toast.makeText(getContext(), "Đã lưu danh mục mới vào Database!", Toast.LENGTH_SHORT).show();
        getParentFragmentManager().popBackStack();
    }
}