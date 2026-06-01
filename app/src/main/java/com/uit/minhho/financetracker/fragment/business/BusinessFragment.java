package com.uit.minhho.financetracker.fragment.business;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.adapter.business.BusinessEntityAdapter;
import com.uit.minhho.financetracker.data.remote.BusinessApiClient;
import com.uit.minhho.financetracker.model.business.BusinessEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BusinessFragment extends Fragment {

    private final List<BusinessEntity> businessEntities = new ArrayList<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private BusinessEntityAdapter entityAdapter;
    private RecyclerView entityRecyclerView;
    private BusinessApiClient apiClient;

    public BusinessFragment() {
        super(R.layout.fragment_business);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiClient = new BusinessApiClient(requireContext());
        setupBusinessList(view);
        setupQuickAccess(view);
        setupFormActions(view);
        loadBusinessEntities();
    }

    private void setupBusinessList(View view) {
        entityRecyclerView = view.findViewById(R.id.rv_business_entities);
        entityRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        businessEntities.clear();

        entityAdapter = new BusinessEntityAdapter(businessEntities, this::confirmDeleteBusinessEntity);
        entityRecyclerView.setAdapter(entityAdapter);
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

            saveButton.setEnabled(false);
            String finalNote = note;
            Context appContext = requireContext().getApplicationContext();
            executorService.execute(() -> {
                BusinessApiClient.ApiResult result = apiClient.createBusinessEntity(name, type, finalNote);
                Activity activity = getActivity();
                if (!isAdded() || activity == null) {
                    return;
                }

                activity.runOnUiThread(() -> {
                    if (!isAdded()) {
                        return;
                    }
                    saveButton.setEnabled(true);
                    Toast.makeText(appContext, result.message, Toast.LENGTH_SHORT).show();

                    if (!result.success) {
                        return;
                    }

                    loadBusinessEntities();
                    entityRecyclerView.scrollToPosition(0);

                    nameInput.setText("");
                    typeInput.setText("");
                    noteInput.setText("");
                });
            });
        });
    }

    private void confirmDeleteBusinessEntity(BusinessEntity entity) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_business_entity_title)
                .setMessage(R.string.delete_business_entity_message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_delete, (dialog, which) -> deleteBusinessEntity(entity))
                .show();
    }

    private void deleteBusinessEntity(BusinessEntity entity) {
        executorService.execute(() -> {
            BusinessApiClient.ApiResult<Void> result = apiClient.deleteBusinessEntity(entity);
            Activity activity = getActivity();
            if (!isAdded() || activity == null) {
                return;
            }

            activity.runOnUiThread(() -> {
                if (!isAdded()) {
                    return;
                }
                Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show();
                if (result.success) {
                    loadBusinessEntities();
                }
            });
        });
    }

    private void loadBusinessEntities() {
        executorService.execute(() -> {
            List<BusinessEntity> loadedItems = apiClient.getBusinessEntities();
            Activity activity = getActivity();
            if (!isAdded() || activity == null) {
                return;
            }

            activity.runOnUiThread(() -> {
                if (!isAdded()) {
                    return;
                }
                businessEntities.clear();
                businessEntities.addAll(loadedItems);
                entityAdapter.notifyDataSetChanged();
            });
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
