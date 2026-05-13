package com.uit.minhho.financetracker.fragment.business;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.adapter.business.BusinessEntityAdapter;
import com.uit.minhho.financetracker.data.local.entity.BusinessEntity;
import com.uit.minhho.financetracker.viewmodel.BusinessViewModel;

import java.util.ArrayList;
import java.util.List;

public class BusinessFragment extends Fragment {

    private final List<BusinessEntity> businessEntities = new ArrayList<>();
    private BusinessEntityAdapter entityAdapter;
    private RecyclerView entityRecyclerView;
    private BusinessViewModel businessViewModel;

    public BusinessFragment() {
        super(R.layout.fragment_business);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        businessViewModel = new ViewModelProvider(requireActivity()).get(BusinessViewModel.class);
        setupBusinessList(view);
        setupQuickAccess(view);
        setupFormActions(view);
        observeMessages();
    }

    private void setupBusinessList(View view) {
        entityRecyclerView = view.findViewById(R.id.rv_business_entities);
        entityRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        businessEntities.clear();

        entityAdapter = new BusinessEntityAdapter(businessEntities);
        entityRecyclerView.setAdapter(entityAdapter);

        businessViewModel.getBusinesses().observe(getViewLifecycleOwner(), businesses -> {
            businessEntities.clear();
            if (businesses != null) {
                businessEntities.addAll(businesses);
            }
            entityAdapter.notifyDataSetChanged();
        });
    }

    private void setupQuickAccess(View view) {
        View walletButton = view.findViewById(R.id.btn_open_business_wallet);
        View budgetButton = view.findViewById(R.id.btn_open_business_budget);

        walletButton.setOnClickListener(v -> openChildScreen(new BusinessWalletFragment()));
        budgetButton.setOnClickListener(v -> openChildScreen(new BusinessBudgetFragment()));
    }

    private void setupFormActions(View view) {
        EditText nameInput = view.findViewById(R.id.et_business_name);
        EditText typeInput = view.findViewById(R.id.et_business_type);
        EditText taxCodeInput = view.findViewById(R.id.et_business_tax_code);
        EditText noteInput = view.findViewById(R.id.et_business_note);
        MaterialButton saveButton = view.findViewById(R.id.btn_save_business);

        saveButton.setOnClickListener(v -> {
            String name = nameInput.getText() == null ? "" : nameInput.getText().toString().trim();
            String type = typeInput.getText() == null ? "" : typeInput.getText().toString().trim();
            String taxCode = taxCodeInput.getText() == null ? "" : taxCodeInput.getText().toString().trim();
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

            businessViewModel.addBusiness(name, type, taxCode, note);

            nameInput.setText("");
            typeInput.setText("");
            taxCodeInput.setText("");
            noteInput.setText("");
        });
    }

    private void observeMessages() {
        businessViewModel.getOperationMessage().observe(getViewLifecycleOwner(), message -> {
            if (message == null || message.trim().isEmpty()) {
                return;
            }
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            businessViewModel.clearOperationMessage();
            if (entityRecyclerView != null && entityAdapter != null && entityAdapter.getItemCount() > 0) {
                entityRecyclerView.scrollToPosition(0);
            }
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
