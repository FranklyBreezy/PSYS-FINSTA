package com.example.psysfinsta.ui.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.psysfinsta.R;
import com.example.psysfinsta.data.entity.RecurrenceType;
import com.example.psysfinsta.data.entity.TransactionEntity;
import com.example.psysfinsta.data.entity.TransactionType;
import com.example.psysfinsta.ui.viewmodel.TransactionViewModel;

import java.util.Collections;

public class TransactionFormFragment extends Fragment {

    private TransactionViewModel transactionViewModel;
    private EditText editAmount, editCategory, editDescription;
    private Spinner spinnerTransactionType, spinnerRecurrenceType;
    private Button btnSave;

    // Holds transaction if editing
    private TransactionEntity currentTransaction;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transaction_form, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

        editAmount = view.findViewById(R.id.edit_amount);
        editCategory = view.findViewById(R.id.edit_category);
        editDescription = view.findViewById(R.id.edit_description);
        spinnerTransactionType = view.findViewById(R.id.spinner_transaction_type);
        spinnerRecurrenceType = view.findViewById(R.id.spinner_recurrence_type);
        btnSave = view.findViewById(R.id.btn_save);

        // Setup adapters for enum spinners
        spinnerTransactionType.setAdapter(new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                TransactionType.values()));

        spinnerRecurrenceType.setAdapter(new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                RecurrenceType.values()));

        // Check if we have args to edit an existing transaction
        if (getArguments() != null && getArguments().containsKey("transaction")) {
            currentTransaction = (TransactionEntity) getArguments().getSerializable("transaction");
            if (currentTransaction != null) {
                prefillData(currentTransaction);
            }
        }

        btnSave.setOnClickListener(v -> saveTransaction());
    }

    private void prefillData(TransactionEntity transaction) {
        editAmount.setText(String.valueOf(transaction.getAmount()));
        editCategory.setText(transaction.getCategory());
        editDescription.setText(transaction.getDescription());
        spinnerTransactionType.setSelection(transaction.getType().ordinal());
        spinnerRecurrenceType.setSelection(transaction.getRecurrence().ordinal());
    }

    private void saveTransaction() {
        String amountStr = editAmount.getText().toString().trim();
        String category = editCategory.getText().toString().trim();
        String description = editDescription.getText().toString().trim();

        // Input validation with error messages
        if (TextUtils.isEmpty(amountStr)) {
            editAmount.setError("Amount is required");
            editAmount.requestFocus();
            return;
        }
        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                editAmount.setError("Amount must be positive");
                editAmount.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            editAmount.setError("Invalid amount");
            editAmount.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(category)) {
            editCategory.setError("Category is required");
            editCategory.requestFocus();
            return;
        }

        long date = currentTransaction != null ? currentTransaction.getDate() : System.currentTimeMillis();
        TransactionType type = (TransactionType) spinnerTransactionType.getSelectedItem();
        RecurrenceType recurrence = (RecurrenceType) spinnerRecurrenceType.getSelectedItem();

        TransactionEntity transaction;
        if (currentTransaction != null) {
            // Updating existing transaction, preserve ID
            transaction = new TransactionEntity(amount, category, date, description, type, recurrence);
            transaction.setId(currentTransaction.getId());
            transactionViewModel.updateTransaction(transaction);
            Toast.makeText(getContext(), "Transaction updated", Toast.LENGTH_SHORT).show();
        } else {
            // Creating new transaction
            transaction = new TransactionEntity(amount, category, date, description, type, recurrence);
            transactionViewModel.insertTransaction(transaction, Collections.emptyList());
            Toast.makeText(getContext(), "Transaction saved", Toast.LENGTH_SHORT).show();
        }

        NavHostFragment.findNavController(this).popBackStack();
    }
}
