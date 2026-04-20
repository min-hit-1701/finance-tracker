package com.uit.minhho.financetracker.fragment.business;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.adapter.business.BusinessTransactionAdapter;
import com.uit.minhho.financetracker.model.business.BusinessTransaction;

import java.util.ArrayList;
import java.util.List;

/**
 * Màn hình hiển thị lịch sử giao dịch doanh nghiệp.
 * Dữ liệu được Việt hóa hoàn toàn và trình bày chuyên nghiệp.
 */
public class TransactionFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Sử dụng layout đã thiết kế sẵn cho doanh nghiệp
        return inflater.inflate(R.layout.fragment_business_transaction, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        RecyclerView recyclerView = view.findViewById(R.id.business_transaction_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        // Thiết lập Adapter với dữ liệu thực tế tại Việt Nam
        recyclerView.setAdapter(new BusinessTransactionAdapter(fakeTransactions()));
    }

    /**
     * Tạo dữ liệu mẫu mang tính logic cho quản lý kinh doanh.
     */
    private List<BusinessTransaction> fakeTransactions() {
        List<BusinessTransaction> list = new ArrayList<>();
        
        // Các giao dịch thu/chi thực tế
        list.add(new BusinessTransaction(
                getString(R.string.business_tx_rent),
                "12 Th4 - 09:20",
                "-4.000.000 đ",
                false
        ));
        list.add(new BusinessTransaction(
                getString(R.string.business_tx_client),
                "12 Th4 - 13:10",
                "+8.500.000 đ",
                true
        ));
        list.add(new BusinessTransaction(
                getString(R.string.business_tx_advert),
                "11 Th4 - 17:45",
                "-1.800.000 đ",
                false
        ));
        list.add(new BusinessTransaction(
                "Nhập hàng linh kiện",
                "10 Th4 - 10:30",
                "-12.500.000 đ",
                false
        ));
        list.add(new BusinessTransaction(
                "Khách thanh toán nợ",
                "09 Th4 - 15:00",
                "+3.200.000 đ",
                true
        ));

        return list;
    }
}
