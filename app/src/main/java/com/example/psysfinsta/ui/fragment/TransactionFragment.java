package com.example.psysfinsta.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.psysfinsta.R;
import com.example.psysfinsta.data.entity.TransactionEntity;
import com.example.psysfinsta.ui.adapter.TransactionAdapter;
import com.example.psysfinsta.ui.viewmodel.TransactionViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class TransactionFragment extends Fragment {

    private TransactionViewModel transactionViewModel;
    private TransactionAdapter transactionAdapter;

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

        RecyclerView recyclerView = view.findViewById(R.id.recycler_transactions);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        transactionAdapter = new TransactionAdapter();
        recyclerView.setAdapter(transactionAdapter);

        // Click to edit transaction
        transactionAdapter.setOnItemClickListener(transaction -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("transaction", transaction);
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_transactionFragment_to_transactionFormFragment, bundle);
        });

        FloatingActionButton fab = view.findViewById(R.id.fab_add_transaction);
        fab.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_transactionFragment_to_transactionFormFragment);
        });

        transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
        transactionViewModel.getFilteredTransactions().observe(getViewLifecycleOwner(), transactions -> {
            transactionAdapter.setTransactions(transactions);
        });

        // Swipe to delete setup
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView,
                                          @NonNull RecyclerView.ViewHolder viewHolder,
                                          @NonNull RecyclerView.ViewHolder target) {
                        return false; // no move support
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                        int position = viewHolder.getAdapterPosition();
                        TransactionEntity transactionToDelete = transactionAdapter.getTransactionAtPosition(position);
                        transactionViewModel.deleteTransaction(transactionToDelete);
                    }
                };

        new ItemTouchHelper(simpleItemTouchCallback).attachToRecyclerView(recyclerView);
    }
}
