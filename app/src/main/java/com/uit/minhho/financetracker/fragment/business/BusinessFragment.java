package com.uit.minhho.financetracker.fragment.business;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.data.local.entity.BusinessContact;
import com.uit.minhho.financetracker.viewmodel.BusinessViewModel;

import java.util.List;

public class BusinessFragment extends Fragment {

    private LinearLayout itemsContainer;
    private BusinessViewModel businessViewModel;

    public BusinessFragment() {
        super(R.layout.fragment_business);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        businessViewModel = new ViewModelProvider(requireActivity()).get(BusinessViewModel.class);

        itemsContainer = view.findViewById(R.id.business_items_container);

        setupQuickAccess(view);
        setupFormActions(view);

        businessViewModel.getBusinessContacts().observe(getViewLifecycleOwner(), this::renderContacts);
    }

    private void renderContacts(List<BusinessContact> contacts) {
        itemsContainer.removeAllViews();
        if (contacts == null || contacts.isEmpty()) {
            return;
        }
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (BusinessContact c : contacts) {
            View itemView = inflater.inflate(R.layout.item_business_entity, itemsContainer, false);
            TextView nameText = itemView.findViewById(R.id.tv_business_name);
            TextView detailText = itemView.findViewById(R.id.tv_business_detail);
            nameText.setText(c.getName());
            detailText.setText(getString(R.string.business_item_detail_format, c.getType(), c.getNote()));

            itemView.findViewById(R.id.btn_delete_business).setOnClickListener(v ->
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle(R.string.action_delete)
                            .setMessage(getString(R.string.business_delete_confirm, c.getName()))
                            .setPositiveButton(R.string.action_delete, (dialog, which) ->
                                    businessViewModel.deleteBusinessContact(c))
                            .setNegativeButton(R.string.action_cancel, null)
                            .show()
            );

            itemsContainer.addView(itemView);
        }
    }

    private void setupQuickAccess(View view) {
        view.findViewById(R.id.btn_open_business_wallet)
                .setOnClickListener(v -> openChildScreen(new BusinessWalletFragment()));
        view.findViewById(R.id.btn_open_business_budget)
                .setOnClickListener(v -> openChildScreen(new BusinessBudgetFragment()));
    }

    private void setupFormActions(View view) {
        EditText nameInput = view.findViewById(R.id.et_business_name);
        EditText typeInput = view.findViewById(R.id.et_business_type);
        EditText noteInput = view.findViewById(R.id.et_business_note);
        MaterialButton saveButton = view.findViewById(R.id.btn_save_business);

        saveButton.setOnClickListener(v -> {
            String name = nameInput.getText() == null ? "" : nameInput.getText().toString().trim();
            String type = typeInput.getText() == null ? "" : typeInput.getText().toString().trim();
            String note = noteInput.getText() == null ? "" : noteInput.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                Toast.makeText(requireContext(), R.string.business_validation_empty_name, Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(type)) {
                Toast.makeText(requireContext(), R.string.business_validation_empty_type, Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(note)) {
                note = getString(R.string.business_note_default);
            }

            businessViewModel.addBusinessContact(name, type, note);
            Toast.makeText(requireContext(), R.string.business_saved_success, Toast.LENGTH_SHORT).show();

            nameInput.setText("");
            typeInput.setText("");
            noteInput.setText("");
        });
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
