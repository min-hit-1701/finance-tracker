package com.uit.minhho.financetracker.fragment.personal;

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
import com.uit.minhho.financetracker.adapter.personal.PersonalTransactionAdapter;
import com.uit.minhho.financetracker.model.personal.PersonalTransaction;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_personal_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rv = view.findViewById(R.id.rv_recent_transactions);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setNestedScrollingEnabled(false);
        
        List<PersonalTransaction> list = new ArrayList<>();
        list.add(new PersonalTransaction("Lương", "01/04 - Thu nhập", "+12.000.000 đ", true, "salary"));
        list.add(new PersonalTransaction("Mua sắm", "Shopee - Quần áo", "-650.000 đ", false, "shopping"));
        list.add(new PersonalTransaction("Giải trí", "Xem phim CGV", "-200.000 đ", false, "entertainment"));
        list.add(new PersonalTransaction("Ăn uống", "Cơm tấm Cali", "-150.000 đ", false, "food"));
        list.add(new PersonalTransaction("Di chuyển", "GrabBike", "-50.000 đ", false, "transport"));
        list.add(new PersonalTransaction("Cafe", "The Coffee House", "-45.000 đ", false, "food"));

        rv.setAdapter(new PersonalTransactionAdapter(list));
    }
}