package com.uit.minhho.financetracker.fragment.personal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.adapter.personal.PersonalTransactionAdapter;
import com.uit.minhho.financetracker.data.local.entity.Transaction;
import com.uit.minhho.financetracker.viewmodel.TransactionViewModel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    // BACKEND: Khai báo biến ViewModel để quản lý và lắng nghe dữ liệu giao dịch từ Database
    private TransactionViewModel transactionViewModel;
    private RecyclerView rvTransactions;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_personal_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo RecyclerView hiển thị danh sách
        rvTransactions = view.findViewById(R.id.rv_recent_transactions);
        rvTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        rvTransactions.setNestedScrollingEnabled(false);

        // BACKEND: Khởi tạo ViewModel đúng kiến trúc MVVM [cite: 62]
        transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

        // Định dạng ngày tháng mặc định đề phòng hàm tx.getDate() bị lỗi
        String currentDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

        // THẦN CHÚ LẮNG NGHE DATA:
        // Gọi hàm getTransactions(false) - truyền false vì đây là luồng cá nhân (Personal), không phải Business
        transactionViewModel.getTransactions(false).observe(getViewLifecycleOwner(), transactions -> {
            if (transactions != null && !transactions.isEmpty()) {
                // TRƯỜNG HỢP 1: Nếu Database đã có dữ liệu thật, chuyển đổi sang dữ liệu hiển thị giao diện [cite: 564, 588]
                List<com.uit.minhho.financetracker.model.personal.PersonalTransaction> displayList = new ArrayList<>();

                for (Transaction tx : transactions) {
                    // Chuyển đổi thực thể Room Database thành Model hiển thị UI của Adapter [cite: 561, 577]
                    String amountText = (tx.isIncome() ? "+" : "-") + String.format("%,.0f", tx.getAmount()) + " đ";

                    displayList.add(new com.uit.minhho.financetracker.model.personal.PersonalTransaction(
                            tx.getNote() != null && !tx.getNote().isEmpty() ? tx.getNote() : "Giao dịch",
                            currentDate + " - " + (tx.isIncome() ? "Thu nhập" : "Chi phí"),
                            amountText,
                            tx.isIncome(),
                            "food" // Icon mặc định, Adapter hoặc UI sẽ tự xử lý ánh xạ sau [cite: 856]
                    ));
                }
                rvTransactions.setAdapter(new PersonalTransactionAdapter(displayList));
            } else {
                // TRƯỜNG HỢP 2: Nếu Database trống (mới cài app), nạp tạm mớ Fake Data để các bạn UI test giao diện không bị lỗi [cite: 564, 588]
                loadFakeDataFallback();
            }
        });

        // KẾT NỐI NAVIGATION: Mở màn hình Thêm giao dịch khi nhấn vào FAB [cite: 875]
        view.findViewById(R.id.fab_add_transaction).setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right,
                            android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                    .replace(R.id.fragment_container, new AddTransactionFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }

    // Hàm dự phòng để chạy Fake data khi database chưa có gì [cite: 564, 588]
    private void loadFakeDataFallback() {
        List<com.uit.minhho.financetracker.model.personal.PersonalTransaction> fakeList = new ArrayList<>();
        fakeList.add(new com.uit.minhho.financetracker.model.personal.PersonalTransaction("Lương", "01/04 - Thu nhập", "+12.000.000 đ", true, "salary"));
        fakeList.add(new com.uit.minhho.financetracker.model.personal.PersonalTransaction("Mua sắm", "Shopee - Quần áo", "-650.000 đ", false, "shopping"));
        fakeList.add(new com.uit.minhho.financetracker.model.personal.PersonalTransaction("Giải trí", "Xem phim CGV", "-200.000 đ", false, "entertainment"));
        fakeList.add(new com.uit.minhho.financetracker.model.personal.PersonalTransaction("Ăn uống", "Cơm tấm Cali", "-150.000 đ", false, "food"));
        fakeList.add(new com.uit.minhho.financetracker.model.personal.PersonalTransaction("Di chuyển", "GrabBike", "-50.000 đ", false, "transport"));
        fakeList.add(new com.uit.minhho.financetracker.model.personal.PersonalTransaction("Cafe", "The Coffee House", "-45.000 đ", false, "food"));

        rvTransactions.setAdapter(new PersonalTransactionAdapter(fakeList));
    }
}