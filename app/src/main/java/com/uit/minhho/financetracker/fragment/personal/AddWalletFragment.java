package com.uit.minhho.financetracker.fragment.personal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.data.local.entity.Wallet;
import com.uit.minhho.financetracker.viewmodel.WalletViewModel;

public class AddWalletFragment extends Fragment {

    private WalletViewModel walletViewModel;
    private AutoCompleteTextView spinnerBank;
    private TextInputEditText etHolderName;
    private TextInputEditText etCardNumber;
    private TextInputEditText etCardLabel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_personal_add_wallet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khớp chuẩn 100% với các ID trong file XML của nhóm bạn
        etHolderName = view.findViewById(R.id.et_holder_name);
        etCardNumber = view.findViewById(R.id.et_card_number);
        etCardLabel = view.findViewById(R.id.et_card_label);
        spinnerBank = view.findViewById(R.id.spinner_bank_type);

        // Khởi tạo WalletViewModel để kết nối dữ liệu xuống Database của nhóm
        walletViewModel = new ViewModelProvider(this).get(WalletViewModel.class);

        // Nối sự kiện nút đóng Toolbar quay lại trang trước
        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());
        }

        setupBankSpinner();

        // Xử lý sự kiện bấm nút Lưu dựa trên ID "@id/btn_save_card" từ file XML
        view.findViewById(R.id.btn_save_card).setOnClickListener(v -> {
            saveWalletLogic();
        });
    }

    private void setupBankSpinner() {
        if (spinnerBank != null) {
            String[] banks = {"Vietcombank", "Techcombank", "MB Bank", "MoMo", "ZaloPay", "Tiền mặt"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, banks);
            spinnerBank.setAdapter(adapter);
        }
    }

    private void saveWalletLogic() {
        // Kiểm tra an toàn hệ thống
        if (etHolderName == null || etCardLabel == null || spinnerBank == null) {
            Toast.makeText(getContext(), "Lỗi: Giao diện chưa được khởi tạo đúng!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy thông tin người dùng nhập từ giao diện
        String holderName = etHolderName.getText().toString().trim();
        String cardLabel = etCardLabel.getText().toString().trim();
        String bankName = spinnerBank.getText().toString().trim();

        // Kiểm tra điều kiện bắt buộc: Không được để trống tên chủ thẻ
        if (holderName.isEmpty()) {
            etHolderName.setError("Vui lòng nhập tên chủ tài khoản!");
            return;
        }

        // Đặt tên cho Ví: Ưu tiên lấy Nhãn gợi nhớ, nếu trống thì tự ghép (Tên ngân hàng - Tên chủ thẻ)
        String finalWalletName = cardLabel.isEmpty() ? (bankName + " - " + holderName) : cardLabel;

        // Mặc định số tiền ban đầu khi kết nối nguồn tiền mới là 0đ (Giao diện ATM không có ô nhập số dư)
        double initialBalance = 0.0;

        // Lấy giá trị loại ví từ spinner để truyền vào tham số Type (Ví dụ: "Ngân hàng", "Tiền mặt")
        String walletType = bankName.isEmpty() ? "Ngân hàng" : bankName;

        // SỬA LỖI TẠI ĐÂY: Khởi tạo Wallet truyền đủ 4 tham số khớp hoàn toàn cấu trúc nhóm thiết kế
        // Tham số: (Tên ví, Số dư, Loại ví, Có phải doanh nghiệp không)
        Wallet newWallet = new Wallet(finalWalletName, initialBalance, walletType, false);

        // THẦN CHÚ BACKEND: Đẩy dữ liệu qua ViewModel để Room tự động chạy ngầm chèn vào bảng
        walletViewModel.insert(newWallet);

        // Thông báo thành công và quay lại màn hình trước
        Toast.makeText(getContext(), "Đã kết nối nguồn tiền và lưu vào Database!", Toast.LENGTH_SHORT).show();
        getParentFragmentManager().popBackStack();
    }
}