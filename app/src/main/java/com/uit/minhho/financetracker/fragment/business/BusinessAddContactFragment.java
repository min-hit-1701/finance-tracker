package com.uit.minhho.financetracker.fragment.business;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputLayout;
import com.uit.minhho.financetracker.R;

public class BusinessAddContactFragment extends Fragment {

    public static final String REQUEST_KEY = "request_add_business_contact";
    public static final String KEY_NAME = "contact_name";
    public static final String KEY_TYPE = "contact_type";
    public static final String KEY_NOTE = "contact_note";

    public BusinessAddContactFragment() {
        super(R.layout.fragment_business_add_contact);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextInputLayout nameLayout = view.findViewById(R.id.til_contact_name);
        TextInputLayout typeLayout = view.findViewById(R.id.til_contact_type);
        EditText nameInput = view.findViewById(R.id.et_contact_name);
        EditText typeInput = view.findViewById(R.id.et_contact_type);
        EditText noteInput = view.findViewById(R.id.et_contact_note);

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar_add_contact);
        toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());

        view.findViewById(R.id.btn_save_contact).setOnClickListener(v -> {
            nameLayout.setError(null);
            typeLayout.setError(null);

            String name = safeText(nameInput);
            String type = safeText(typeInput);
            String note = safeText(noteInput);

            if (TextUtils.isEmpty(name)) {
                nameLayout.setError(getString(R.string.business_validation_empty_name));
                return;
            }
            if (TextUtils.isEmpty(type)) {
                typeLayout.setError(getString(R.string.business_validation_empty_type));
                return;
            }
            if (TextUtils.isEmpty(note)) {
                note = getString(R.string.business_note_default);
            }

            Bundle result = new Bundle();
            result.putString(KEY_NAME, name);
            result.putString(KEY_TYPE, type);
            result.putString(KEY_NOTE, note);
            getParentFragmentManager().setFragmentResult(REQUEST_KEY, result);
            getParentFragmentManager().popBackStack();
        });

        view.findViewById(R.id.btn_cancel_contact).setOnClickListener(v ->
                getParentFragmentManager().popBackStack()
        );
    }

    private String safeText(EditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }
}
