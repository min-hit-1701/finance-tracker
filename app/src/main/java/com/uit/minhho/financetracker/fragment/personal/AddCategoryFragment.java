package com.uit.minhho.financetracker.fragment.personal;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.adapter.personal.IconAdapter;
import com.uit.minhho.financetracker.data.local.entity.Category;
import com.uit.minhho.financetracker.viewmodel.CategoryViewModel;
import java.util.ArrayList;
import java.util.List;

public class AddCategoryFragment extends Fragment {

    private CategoryViewModel categoryViewModel;
    private TextInputEditText etCategoryName;
    private ImageView ivSelectedIcon;
    private TextView tvIconHint;
    private ImageView ivIconChevron;
    private LinearLayout iconSelectorContainer;
    private MaterialButtonToggleGroup toggleType;
    private int selectedIconResId = R.drawable.ic_other;
    private boolean iconSelected = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_personal_add_category, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etCategoryName = view.findViewById(R.id.et_category_name);
        ivSelectedIcon = view.findViewById(R.id.iv_selected_icon);
        tvIconHint = view.findViewById(R.id.tv_icon_hint);
        ivIconChevron = view.findViewById(R.id.iv_icon_chevron);
        iconSelectorContainer = view.findViewById(R.id.icon_selector_container);
        toggleType = view.findViewById(R.id.toggle_category_type);

        applyIconTint(ivSelectedIcon);
        iconSelectorContainer.setOnClickListener(v -> showIconPickerDialog());

        categoryViewModel = new ViewModelProvider(requireActivity()).get(CategoryViewModel.class);

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());
        }

        View btnSave = view.findViewById(R.id.btn_save_category);
        if (btnSave != null) {
            btnSave.setOnClickListener(v -> saveCategoryLogic());
        }
    }

    private void applyIconTint(ImageView imageView) {
        int tint = requireContext().getColor(R.color.brand_primary);
        ImageViewCompat.setImageTintList(imageView, ColorStateList.valueOf(tint));
    }

    private void showIconPickerDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_icon_picker, null);
        RecyclerView rvIcons = dialogView.findViewById(R.id.rv_icons);

        List<Integer> iconList = new ArrayList<>();
        iconList.add(R.drawable.ic_food);
        iconList.add(R.drawable.ic_transport);
        iconList.add(R.drawable.ic_shopping);
        iconList.add(R.drawable.ic_utility);
        iconList.add(R.drawable.ic_home);
        iconList.add(R.drawable.ic_entertainment);
        iconList.add(R.drawable.ic_health);
        iconList.add(R.drawable.ic_education);
        iconList.add(R.drawable.ic_salary);
        iconList.add(R.drawable.ic_bonus);
        iconList.add(R.drawable.ic_investment);
        iconList.add(R.drawable.ic_other);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.category_icon_picker_title)
                .setView(dialogView)
                .setNegativeButton(R.string.action_cancel, null)
                .create();

        IconAdapter adapter = new IconAdapter(iconList, iconResId -> {
            selectedIconResId = iconResId;
            iconSelected = true;
            updateIconSelectorUi();
            dialog.dismiss();
        });

        rvIcons.setLayoutManager(new GridLayoutManager(requireContext(), 4));
        rvIcons.setAdapter(adapter);

        dialog.show();
    }

    private void updateIconSelectorUi() {
        ivSelectedIcon.setImageResource(selectedIconResId);
        applyIconTint(ivSelectedIcon);

        tvIconHint.setVisibility(View.GONE);
        ivIconChevron.setVisibility(View.GONE);
        iconSelectorContainer.setGravity(Gravity.CENTER);
    }

    private void saveCategoryLogic() {
        if (etCategoryName == null) return;

        String categoryName = etCategoryName.getText().toString().trim();

        if (categoryName.isEmpty()) {
            etCategoryName.setError("Vui lòng nhập tên danh mục!");
            return;
        }

        boolean isIncome = toggleType.getCheckedButtonId() == R.id.btn_type_income;
        String colorHex = isIncome ? "#4CAF50" : "#F44336";

        int iconToSave = iconSelected ? selectedIconResId : R.drawable.ic_other;
        Category newCategory = new Category(categoryName, iconToSave, colorHex, isIncome);

        Context appContext = requireContext().getApplicationContext();
        categoryViewModel.insert(newCategory, (success, message) -> {
            Activity activity = getActivity();
            if (!isAdded() || activity == null) {
                return;
            }
            activity.runOnUiThread(() -> {
                if (!isAdded()) {
                    return;
                }
                Toast.makeText(appContext, message, Toast.LENGTH_SHORT).show();
                if (success) {
                    getParentFragmentManager().popBackStack();
                }
            });
        });
    }
}
