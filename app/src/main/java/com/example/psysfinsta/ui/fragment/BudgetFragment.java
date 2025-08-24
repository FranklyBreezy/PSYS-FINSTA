package com.example.psysfinsta.ui.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.psysfinsta.R;
import com.example.psysfinsta.data.entity.Budget;
import com.example.psysfinsta.ui.adapter.BudgetAdapter;
import com.example.psysfinsta.ui.viewmodel.BudgetViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class BudgetFragment extends Fragment {

    private BudgetViewModel viewModel;
    private BudgetAdapter adapter;
    private RecyclerView recyclerView;

    public BudgetFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budget, container, false);
        recyclerView = view.findViewById(R.id.recyclerBudget);
        view.findViewById(R.id.btnAddBudget).setOnClickListener(v -> showAddBudgetDialog());

        adapter = new BudgetAdapter(new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(BudgetViewModel.class);
        long currentMonthTimestamp = getMonthStartTimestamp();

        viewModel.getBudgetsForMonth(currentMonthTimestamp).observe(getViewLifecycleOwner(), budgets -> {
            adapter.updateData(budgets);
            Log.d("BudgetFragment", "Loaded " + budgets.size() + " budgets");
        });

        return view;
    }

    private void showAddBudgetDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_budget, null);
        EditText edtTag = dialogView.findViewById(R.id.edtCategory);
        EditText edtLimit = dialogView.findViewById(R.id.edtLimit);

        new AlertDialog.Builder(getContext())
                .setTitle("Add Budget")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String tag = edtTag.getText().toString().trim();
                    String limitStr = edtLimit.getText().toString().trim();

                    if (TextUtils.isEmpty(tag) || TextUtils.isEmpty(limitStr)) {
                        Toast.makeText(getContext(), "Fields cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        double limit = Double.parseDouble(limitStr);
                        long monthTimestamp = getMonthStartTimestamp();
                        Budget budget = new Budget(tag, limit, monthTimestamp);
                        viewModel.insert(budget);
                        Log.d("BudgetFragment", "Inserted budget for tag: " + tag);
                    } catch (NumberFormatException e) {
                        Log.e("BudgetFragment", "Invalid limit input", e);
                        Toast.makeText(getContext(), "Invalid limit", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private long getMonthStartTimestamp() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
}