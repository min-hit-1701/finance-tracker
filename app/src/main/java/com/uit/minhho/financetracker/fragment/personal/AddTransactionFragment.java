package com.uit.minhho.financetracker.fragment.personal;

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

public class AddTransactionFragment extends Fragment {

    private EditText etDate, etAmount, etNote;
    private AutoCompleteTextView spinnerCategory, spinnerWallet;
    private TransactionViewModel viewModel;
    
    private List<Category> categoryList = new ArrayList<>();
    private List<Wallet> walletList = new ArrayList<>();
    private boolean isIncome = false;

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    if (imageBitmap != null) {
                        runOcr(imageBitmap);
                    }
                }
            }
    );

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openCamera();
                } else {
                    Toast.makeText(getContext(), "Cần quyền camera để quét hóa đơn", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_personal_add_transaction, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

        initViews(view);
        setupToolbar(view);
        loadDataFromDb();
        setupDatePicker();

        view.findViewById(R.id.btn_save).setOnClickListener(v -> saveTransaction());
        
        view.findViewById(R.id.btn_income).setOnClickListener(v -> { 
            isIncome = true; 
            updateCategorySpinner(); 
        });
        view.findViewById(R.id.btn_expense).setOnClickListener(v -> { 
            isIncome = false; 
            updateCategorySpinner(); 
        });
    }

    private void initViews(View view) {
        etDate = view.findViewById(R.id.et_date);
        etAmount = view.findViewById(R.id.et_amount);
        etNote = view.findViewById(R.id.et_note);
        spinnerCategory = view.findViewById(R.id.spinner_category);
        spinnerWallet = view.findViewById(R.id.spinner_wallet);
    }

    private void loadDataFromDb() {
        viewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            this.categoryList = categories;
            updateCategorySpinner();
        });

        viewModel.getWallets(false).observe(getViewLifecycleOwner(), wallets -> {
            this.walletList = wallets;
            List<String> names = new ArrayList<>();
            for (Wallet w : wallets) names.add(w.getName());
            spinnerWallet.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, names));
            if (!names.isEmpty() && spinnerWallet.getText().toString().isEmpty()) {
                spinnerWallet.setText(names.get(0), false);
            }
        });
    }

    private void updateCategorySpinner() {
        List<String> names = new ArrayList<>();
        for (Category c : categoryList) {
            if (c.isIncome() == isIncome) {
                names.add(c.getName());
            }
        }
        spinnerCategory.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, names));
    }

    private void saveTransaction() {
        String amountStr = etAmount.getText().toString().trim();
        if (amountStr.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            String note = etNote.getText().toString();
            
            int categoryId = 0;
            String selectedCat = spinnerCategory.getText().toString();
            for (Category c : categoryList) {
                if (c.getName().equals(selectedCat)) {
                    categoryId = c.getId();
                    break;
                }
            }

            int walletId = 0;
            String selectedWallet = spinnerWallet.getText().toString();
            for (Wallet w : walletList) {
                if (w.getName().equals(selectedWallet)) {
                    walletId = w.getId();
                    break;
                }
            }

            if (categoryId == 0 || walletId == 0) {
                Toast.makeText(getContext(), "Vui lòng chọn đầy đủ danh mục và ví", Toast.LENGTH_SHORT).show();
                return;
            }

            Transaction tx = new Transaction(amount, System.currentTimeMillis(), note, categoryId, walletId, isIncome, false);
            viewModel.insert(tx);
            
            Toast.makeText(getContext(), "Đã lưu giao dịch!", Toast.LENGTH_SHORT).show();
            getParentFragmentManager().popBackStack();
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
        }
    }

    private void runOcr(Bitmap bitmap) {
        Toast.makeText(getContext(), "Đang phân tích hóa đơn...", Toast.LENGTH_SHORT).show();
        OcrHelper.scanReceipt(bitmap, (amount, categorySuggestion, dateSuggestion, fullText) -> {
            if (amount != null && !amount.equals("0")) {
                etAmount.setText(amount);
            }
            
            if (dateSuggestion != null) {
                etDate.setText(dateSuggestion);
            }
            
            // Tự động nhận diện danh mục và cập nhật UI
            isIncome = false; // Hóa đơn thường là chi tiêu
            updateCategorySpinner();
            spinnerCategory.setText(categorySuggestion, false);
            
            String currentNote = etNote.getText().toString();
            String ocrNote = "Quét hóa đơn: " + categorySuggestion;
            etNote.setText(currentNote.isEmpty() ? ocrNote : currentNote + "\n" + ocrNote);
            Toast.makeText(getContext(), "Nhận diện: " + categorySuggestion + " - " + (amount != null ? amount : "0") + "đ", Toast.LENGTH_LONG).show();
        });
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
            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                    (view1, year, month, dayOfMonth) -> etDate.setText(dayOfMonth + "/" + (month + 1) + "/" + year),
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });
    }
}
