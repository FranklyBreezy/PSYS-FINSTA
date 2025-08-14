package com.example.psysfinsta.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.psysfinsta.R;
import com.example.psysfinsta.data.entity.TransactionEntity;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<TransactionEntity> transactions;
    private OnItemClickListener onItemClickListener;

    public void setTransactions(List<TransactionEntity> transactions) {
        this.transactions = transactions;
        notifyDataSetChanged();
    }

    public TransactionEntity getTransactionAtPosition(int position) {
        if (transactions != null && position >= 0 && position < transactions.size()) {
            return transactions.get(position);
        }
        return null;
    }

    // Interface for click events
    public interface OnItemClickListener {
        void onItemClick(TransactionEntity transaction);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        TransactionEntity tx = transactions.get(position);

        holder.amount.setText("₹" + tx.getAmount());
        holder.category.setText(tx.getCategory());
        holder.description.setText(tx.getDescription());

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        holder.date.setText(sdf.format(tx.getDate()));

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(tx);
            }
        });
    }

    @Override
    public int getItemCount() {
        return transactions != null ? transactions.size() : 0;
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView amount, category, description, date;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            amount = itemView.findViewById(R.id.text_amount);
            category = itemView.findViewById(R.id.text_category);
            description = itemView.findViewById(R.id.text_description);
            date = itemView.findViewById(R.id.text_date);
        }
    }
}
