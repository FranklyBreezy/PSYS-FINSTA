package com.example.psysfinsta.ui.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.psysfinsta.R;
import com.example.psysfinsta.data.entity.RecurrenceType;
import com.example.psysfinsta.data.entity.TransactionEntity;
import com.example.psysfinsta.data.entity.TransactionWithTags;
import com.example.psysfinsta.ui.adapter.TransactionAdapter;
import com.example.psysfinsta.ui.viewmodel.TransactionViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class TransactionFragment extends Fragment {

    private TransactionViewModel transactionViewModel;
    private TransactionAdapter transactionAdapter;
    private TextView weeklyNetBalanceText, monthlyNetBalanceText, yearlyNetBalanceText;

    public TransactionFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transaction, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        weeklyNetBalanceText = view.findViewById(R.id.weeklyNetBalanceText);
        monthlyNetBalanceText = view.findViewById(R.id.monthlyNetBalanceText);
        yearlyNetBalanceText = view.findViewById(R.id.yearlyNetBalanceText);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_transactions);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        transactionAdapter = new TransactionAdapter();
        recyclerView.setAdapter(transactionAdapter);

        // Navigation on item click (passes TransactionWithTags)
        transactionAdapter.setOnItemClickListener(transactionWithTags -> {
            Bundle bundle = new Bundle();
            bundle.putParcelable("transactionWithTags", transactionWithTags);
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_transactionFragment_to_transactionFormFragment, bundle);
        });

        FloatingActionButton fab = view.findViewById(R.id.fab_add_transaction);
        fab.setOnClickListener(v -> {
            Navigation.findNavController(v)
                    .navigate(R.id.action_transactionFragment_to_transactionFormFragment);
        });

        transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
        transactionViewModel.getWeeklyNetBalance().observe(getViewLifecycleOwner(), balance -> {
            weeklyNetBalanceText.setText(formatBalanceText("Weekly Net Balance: ", balance));
        });

        transactionViewModel.getMonthlyNetBalance().observe(getViewLifecycleOwner(), balance -> {
            monthlyNetBalanceText.setText(formatBalanceText("Current Month Net Balance: ", balance));
        });

        transactionViewModel.getYearlyNetBalance().observe(getViewLifecycleOwner(), balance -> {
            yearlyNetBalanceText.setText(formatBalanceText("Current Year Net Balance: ", balance));
        });

        // Pagination UI wiring
        View btnPrev = view.findViewById(R.id.btn_prev);
        View btnNext = view.findViewById(R.id.btn_next);
        TextView pageIndicator = view.findViewById(R.id.page_indicator);

        btnPrev.setOnClickListener(v -> {
            transactionViewModel.prevPage();
            updatePagedList();
        });

        btnNext.setOnClickListener(v -> {
            transactionViewModel.nextPage();
            updatePagedList();
        });

        transactionViewModel.getCurrentPage().observe(getViewLifecycleOwner(), page -> {
            int totalPages = 1;
            List<TransactionWithTags> all = transactionViewModel.getRepositoryLiveData().getValue();
            if (all != null && transactionViewModel.getPageSize().getValue() > 0) {
                totalPages = (int) Math.ceil((double) all.size() / transactionViewModel.getPageSize().getValue());
            }
            pageIndicator.setText(String.format("Page %d of %d", page + 1, totalPages));
        });

        // Observe repository and update paged list
        transactionViewModel.getRepositoryLiveData().observe(getViewLifecycleOwner(), transactionWithTags -> {
            updatePagedList();
        });

        // Swipe-to-delete logic
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView,
                                          @NonNull RecyclerView.ViewHolder viewHolder,
                                          @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                        int position = viewHolder.getAdapterPosition();
                        TransactionEntity transactionToDelete = transactionAdapter.getTransactionAtPosition(position);

                        long currentTime = System.currentTimeMillis();
                        long timeDifference = currentTime - transactionToDelete.getDate();
                        long oneHourMillis = 60 * 60 * 1000L;

                        if (transactionToDelete.getRecurrence() != null &&
                                transactionToDelete.getRecurrence() != RecurrenceType.NONE) {
                            showRecurringTransactionDeleteDialog(transactionToDelete, position);
                        } else if (timeDifference > oneHourMillis) {
                            showHistoricalTransactionDeleteDialog(transactionToDelete, position);
                        } else {
                            transactionViewModel.deleteTransaction(transactionToDelete);
                            showUndoSnackbar(requireView(), transactionToDelete);
                        }
                    }
                };

        new ItemTouchHelper(simpleItemTouchCallback).attachToRecyclerView(recyclerView);
    }

    private void updatePagedList() {
        transactionAdapter.setTransactionWithTagsList(transactionViewModel.getPagedTransactions());
    }

    private void showRecurringTransactionDeleteDialog(TransactionEntity transactionToDelete, int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Recurring Transaction")
                .setMessage("This transaction is recurring. What would you like to delete?")
                .setPositiveButton("Only This", (dialog, which) -> {
                    transactionViewModel.deleteSingle(transactionToDelete);
                    showUndoSnackbar(requireView(), transactionToDelete);
                })
                .setNeutralButton("All Future", (dialog, which) -> {
                    transactionViewModel.deleteFuture(transactionToDelete);
                    showUndoSnackbar(requireView(), transactionToDelete);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    transactionAdapter.notifyItemChanged(position);
                })
                .setOnCancelListener(dialog -> transactionAdapter.notifyItemChanged(position))
                .show();
    }

    private String formatBalanceText(String label, double balance) {
        String sign = balance > 0 ? "+" : (balance < 0 ? "-" : "");
        return label + sign + String.format("%.2f", Math.abs(balance));
    }

    private void showHistoricalTransactionDeleteDialog(TransactionEntity transactionToDelete, int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Historical Transaction")
                .setMessage("This transaction was added over an hour ago. Delete it?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    transactionViewModel.deleteTransaction(transactionToDelete);
                    showUndoSnackbar(requireView(), transactionToDelete);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    transactionAdapter.notifyItemChanged(position);
                })
                .setOnCancelListener(dialog -> transactionAdapter.notifyItemChanged(position))
                .show();
    }

    private void showUndoSnackbar(View view, TransactionEntity deletedTransaction) {
        Snackbar.make(view, "Transaction deleted", Snackbar.LENGTH_LONG)
                .setAction("Undo", v -> transactionViewModel.undoDelete())
                .show();
    }
}