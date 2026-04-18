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

        RecyclerView recyclerView = view.findViewById(R.id.rv_recent_transactions);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(new PersonalTransactionAdapter(getFakeTransactions()));

        view.findViewById(R.id.fab_add_transaction).setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
                    .replace(R.id.fragment_container, new AddTransactionFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }

    private List<PersonalTransaction> getFakeTransactions() {
        List<PersonalTransaction> list = new ArrayList<>();
        list.add(new PersonalTransaction(
                "1", "Morning Coffee", "Starbucks", 55000,
                "Today, 08:30", "cat_1", "wallet_1", false
        ));
        list.add(new PersonalTransaction(
                "2", "Salary", "Monthly salary payment", 15000000,
                "Yesterday, 17:00", "cat_2", "wallet_2", true
        ));
        list.add(new PersonalTransaction(
                "3", "Grocery", "WinMart", 230000,
                "Apr 12, 18:20", "cat_3", "wallet_1", false
                ));
        list.add(new PersonalTransaction(
                "4", "Bonus", "Quarterly bonus", 2000000,
                "Apr 10, 09:00", "cat_4", "wallet_2", true
        ));
        return list;
    }
}
