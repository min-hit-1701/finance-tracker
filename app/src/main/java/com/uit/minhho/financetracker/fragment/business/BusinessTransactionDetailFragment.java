package com.uit.minhho.financetracker.fragment.business;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.model.business.BusinessTransaction;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BusinessTransactionDetailFragment extends Fragment {
    private static final String ARG_ID = "id";
    private static final String ARG_TITLE = "title";
    private static final String ARG_SUBTITLE = "subtitle";
    private static final String ARG_AMOUNT = "amount";
    private static final String ARG_INCOME = "income";
    private static final String ARG_RAW_AMOUNT = "raw_amount";
    private static final String ARG_TIMESTAMP = "timestamp";
    private static final String ARG_CATEGORY = "category";

    public static BusinessTransactionDetailFragment newInstance(BusinessTransaction transaction) {
        BusinessTransactionDetailFragment fragment = new BusinessTransactionDetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ID, transaction.getId());
        args.putString(ARG_TITLE, transaction.getTitle());
        args.putString(ARG_SUBTITLE, transaction.getSubtitle());
        args.putString(ARG_AMOUNT, transaction.getAmount());
        args.putBoolean(ARG_INCOME, transaction.isIncome());
        args.putDouble(ARG_RAW_AMOUNT, transaction.getRawAmount());
        args.putLong(ARG_TIMESTAMP, transaction.getTimestamp());
        args.putString(ARG_CATEGORY, transaction.getCategoryName());
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_business_transaction_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = requireArguments();
        boolean isIncome = args.getBoolean(ARG_INCOME);

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());

        TextView amountText = view.findViewById(R.id.tv_detail_amount);
        amountText.setText(args.getString(ARG_AMOUNT, ""));
        amountText.setTextColor(ContextCompat.getColor(requireContext(), isIncome ? R.color.income_green : R.color.expense_red));

        ((TextView) view.findViewById(R.id.tv_detail_title)).setText(args.getString(ARG_TITLE, "Giao dịch"));
        ((TextView) view.findViewById(R.id.tv_detail_type)).setText(isIncome ? "Doanh thu" : "Chi phí");
        ((TextView) view.findViewById(R.id.tv_detail_category)).setText(args.getString(ARG_CATEGORY, ""));
        ((TextView) view.findViewById(R.id.tv_detail_time)).setText(displayTime(args));
        ((TextView) view.findViewById(R.id.tv_detail_id)).setText(String.valueOf(args.getInt(ARG_ID)));
    }

    private String displayTime(Bundle args) {
        long timestamp = args.getLong(ARG_TIMESTAMP, 0L);
        if (timestamp > 0L) {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date(timestamp));
        }
        return args.getString(ARG_SUBTITLE, "");
    }
}
