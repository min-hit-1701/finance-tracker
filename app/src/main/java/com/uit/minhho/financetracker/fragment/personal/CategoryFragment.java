package com.uit.minhho.financetracker.fragment.personal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.adapter.personal.CategoryAdapter;
import com.uit.minhho.financetracker.model.personal.Category;
import java.util.ArrayList;
import java.util.List;

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

        categoryList = getInitialCategories();
        
        RecyclerView recyclerView = view.findViewById(R.id.rv_categories);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        
        adapter = new CategoryAdapter(categoryList, category -> {
            // Xử lý khi người dùng nhấn vào một danh mục
        });
        recyclerView.setAdapter(adapter);

        // CẬP NHẬT: Gọi Fragment thay vì Dialog
        view.findViewById(R.id.fab_add_category).setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, 
                                       android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                    .replace(R.id.fragment_container, new AddCategoryFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }

    private List<Category> getInitialCategories() {
        List<Category> list = new ArrayList<>();
        list.add(new Category("1", getString(R.string.cat_food), 0, false));
        list.add(new Category("2", getString(R.string.cat_transport), 0, false));
        list.add(new Category("3", getString(R.string.cat_shopping), 0, false));
        list.add(new Category("4", getString(R.string.cat_utility), 0, false));
        list.add(new Category("5", getString(R.string.cat_home), 0, false));
        list.add(new Category("6", getString(R.string.cat_other), 0, false));
        list.add(new Category("7", getString(R.string.cat_salary), 0, true));
        list.add(new Category("8", getString(R.string.cat_bonus), 0, true));
        return list;
    }
}