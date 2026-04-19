package com.uit.minhho.financetracker.fragment.personal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.adapter.personal.CategoryAdapter;
import com.uit.minhho.financetracker.model.personal.Category;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Màn hình quản lý danh mục thu chi cá nhân.
 * Đã được nâng cấp 100% tiếng Việt, icon chuẩn và giao diện hiện đại.
 */
public class CategoryFragment extends Fragment {

    private CategoryAdapter adapter;
    private List<Category> categoryList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_personal_category, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Nạp dữ liệu danh mục ban đầu hoàn toàn bằng tiếng Việt
        categoryList = getInitialCategories();
        
        RecyclerView recyclerView = view.findViewById(R.id.rv_categories);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        
        // Khởi tạo adapter với listener để xử lý sự kiện nhấn
        adapter = new CategoryAdapter(categoryList, category -> {
            // Xử lý khi người dùng nhấn vào một danh mục (xem lịch sử thu chi của mục đó)
        });
        recyclerView.setAdapter(adapter);

        // Nút thêm danh mục mới
        view.findViewById(R.id.fab_add_category).setOnClickListener(v -> showAddCategoryDialog());
    }

    private void showAddCategoryDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_category, null);
        EditText etName = dialogView.findViewById(R.id.et_category_name);
        MaterialButtonToggleGroup toggleType = dialogView.findViewById(R.id.toggle_category_type);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.personal_add_category)
                .setView(dialogView)
                .setPositiveButton(R.string.action_save, (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    if (!name.isEmpty()) {
                        boolean isIncome = toggleType.getCheckedButtonId() == R.id.btn_type_income;
                        addCategory(name, isIncome);
                    }
                })
                .setNegativeButton("Hủy bỏ", null)
                .show();
    }

    private void addCategory(String name, boolean isIncome) {
        Category newCategory = new Category(
                UUID.randomUUID().toString(),
                name,
                isIncome ? android.R.drawable.ic_menu_upload : android.R.drawable.ic_menu_gallery,
                isIncome
        );
        
        categoryList.add(newCategory);
        adapter.notifyItemInserted(categoryList.size() - 1);
    }

    private List<Category> getInitialCategories() {
        List<Category> list = new ArrayList<>();
        
        // Các danh mục Khoản chi (Việt hóa 100% từ Resource)
        list.add(new Category("1", getString(R.string.cat_food), 0, false));
        list.add(new Category("2", getString(R.string.cat_transport), 0, false));
        list.add(new Category("3", getString(R.string.cat_shopping), 0, false));
        list.add(new Category("4", getString(R.string.cat_utility), 0, false));
        list.add(new Category("5", getString(R.string.cat_home), 0, false));
        list.add(new Category("6", getString(R.string.cat_other), 0, false));
        
        // Các danh mục Khoản thu (Việt hóa 100% từ Resource)
        list.add(new Category("7", getString(R.string.cat_salary), 0, true));
        list.add(new Category("8", getString(R.string.cat_bonus), 0, true));
        
        return list;
    }
}
