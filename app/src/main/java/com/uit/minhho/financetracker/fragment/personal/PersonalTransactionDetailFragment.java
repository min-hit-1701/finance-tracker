package com.uit.minhho.financetracker.fragment.personal;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.model.personal.PersonalTransaction;

public class PersonalTransactionDetailFragment extends Fragment {
    private static final String ARG_ID = "id";
    private static final String ARG_TITLE = "title";
    private static final String ARG_SUBTITLE = "subtitle";
    private static final String ARG_AMOUNT = "amount";
    private static final String ARG_INCOME = "income";
    private static final String ARG_ICON = "icon";
    private static final String ARG_CATEGORY = "category";
    private static final String ARG_TIME = "time";

    public static PersonalTransactionDetailFragment newInstance(PersonalTransaction transaction) {
        PersonalTransactionDetailFragment fragment = new PersonalTransactionDetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ID, transaction.getId());
        args.putString(ARG_TITLE, transaction.getTitle());
        args.putString(ARG_SUBTITLE, transaction.getSubtitle());
        args.putString(ARG_AMOUNT, transaction.getAmount());
        args.putBoolean(ARG_INCOME, transaction.isIncome());
        args.putString(ARG_ICON, transaction.getIconType());
        args.putString(ARG_CATEGORY, transaction.getCategoryName());
        args.putString(ARG_TIME, transaction.getTimeText());
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_personal_transaction_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = requireArguments();
        boolean isIncome = args.getBoolean(ARG_INCOME);

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());

        ImageView iconView = view.findViewById(R.id.iv_transaction_icon);
        iconView.setImageResource(iconRes(args.getString(ARG_ICON)));
        ImageViewCompat.setImageTintList(iconView, ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white)));

        TextView amountText = view.findViewById(R.id.tv_detail_amount);
        amountText.setText(args.getString(ARG_AMOUNT, ""));
        amountText.setTextColor(ContextCompat.getColor(requireContext(), isIncome ? R.color.income_green : R.color.expense_red));

        ((TextView) view.findViewById(R.id.tv_detail_title)).setText(args.getString(ARG_TITLE, "Giao dịch"));
        ((TextView) view.findViewById(R.id.tv_detail_type)).setText(isIncome ? "Khoản thu" : "Khoản chi");
        ((TextView) view.findViewById(R.id.tv_detail_category)).setText(args.getString(ARG_CATEGORY, ""));
        ((TextView) view.findViewById(R.id.tv_detail_time)).setText(args.getString(ARG_TIME, ""));
        ((TextView) view.findViewById(R.id.tv_detail_id)).setText(String.valueOf(args.getInt(ARG_ID)));
    }

    private int iconRes(String value) {
        try {
            int parsed = value == null ? 0 : Integer.parseInt(value);
            return parsed > 0 ? parsed : R.drawable.ic_other;
        } catch (NumberFormatException ignored) {
            return R.drawable.ic_other;
        }
    }
}
