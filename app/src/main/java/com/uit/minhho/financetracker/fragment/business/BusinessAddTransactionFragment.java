package com.uit.minhho.financetracker.fragment.business;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.appbar.MaterialToolbar;
import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.data.local.entity.Category;
import com.uit.minhho.financetracker.data.local.entity.Transaction;
import com.uit.minhho.financetracker.data.local.entity.Wallet;
import com.uit.minhho.financetracker.util.OcrHelper;
import com.uit.minhho.financetracker.viewmodel.TransactionViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class BusinessAddTransactionFragment extends Fragment {

    private EditText etAmount, etPartner, etDate, etNote;
    private AutoCompleteTextView spinnerCategory, spinnerWallet;
    private TransactionViewModel viewModel;

    private List<Category> categoryList = new ArrayList<>();
    private List<Wallet> walletList = new ArrayList<>();

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    if (imageBitmap != null) runOcr(imageBitmap);
                }
            }
    );

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) openCamera();
                else Toast.makeText(getContext(), "Cần quyền camera để quét hóa đơn", Toast.LENGTH_SHORT).show();
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_business_add_transaction, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

        initViews(view);
        setupToolbar(view);
        loadDataFromDb();
        setupDatePicker();

        view.findViewById(R.id.btn_save).setOnClickListener(v -> saveBusinessTransaction());
    }

    private void initViews(View view) {
        etAmount = view.findViewById(R.id.et_amount);
        etPartner = view.findViewById(R.id.et_partner);
        etDate = view.findViewById(R.id.et_date);
        etNote = view.findViewById(R.id.et_note);
        spinnerCategory = view.findViewById(R.id.spinner_category);
        spinnerWallet = view.findViewById(R.id.spinner_wallet); // Lưu ý: Layout Business cần có ID này
    }

    private void setupToolbar(View view) {
        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_add_transaction);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_scan_receipt) {
                checkPermissionAndOpenCamera();
                return true;
            }
            return false;
        });
        toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());
    }

    private void loadDataFromDb() {
        viewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            this.categoryList = categories;
            List<String> names = new ArrayList<>();
            for (Category c : categories) names.add(c.getName());
            spinnerCategory.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, names));
        });

        viewModel.getWallets(true).observe(getViewLifecycleOwner(), wallets -> {
            this.walletList = wallets;
            if (spinnerWallet != null) {
                List<String> names = new ArrayList<>();
                for (Wallet w : wallets) names.add(w.getName());
                spinnerWallet.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, names));
            }
        });
    }

    private void saveBusinessTransaction() {
        String amountStr = etAmount.getText().toString();
        if (amountStr.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            String note = etNote.getText().toString() + " (Đối tác: " + etPartner.getText().toString() + ")";
            
            int categoryId = 0;
            for (Category c : categoryList) {
                if (c.getName().equals(spinnerCategory.getText().toString())) {
                    categoryId = c.getId();
                    break;
                }
            }

            // Giao dịch doanh nghiệp mặc định dùng ví doanh nghiệp đầu tiên nếu không chọn
            int walletId = walletList.isEmpty() ? 0 : walletList.get(0).getId();

            Transaction tx = new Transaction(amount, System.currentTimeMillis(), note, categoryId, walletId, false, true);
            viewModel.insert(tx);
            
            Toast.makeText(getContext(), "Đã ghi nhận chi phí doanh nghiệp!", Toast.LENGTH_SHORT).show();
            getParentFragmentManager().popBackStack();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Lỗi khi lưu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void runOcr(Bitmap bitmap) {
        Toast.makeText(getContext(), "Đang quét hóa đơn doanh nghiệp...", Toast.LENGTH_SHORT).show();
        OcrHelper.scanReceipt(bitmap, (amount, categorySuggestion, dateSuggestion, fullText) -> {
            if (amount != null && !amount.equals("0")) etAmount.setText(amount);
            spinnerCategory.setText(categorySuggestion, false);
            if (dateSuggestion != null) etDate.setText(dateSuggestion);
            etNote.setText("OCR DN: " + categorySuggestion);
        });
    }

    private void checkPermissionAndOpenCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(takePictureIntent);
    }

    private void setupDatePicker() {
        etDate.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            new DatePickerDialog(requireContext(), (view1, year, month, day) -> 
                etDate.setText(day + "/" + (month + 1) + "/" + year),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });
    }
}
