package com.example.psysfinsta.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.psysfinsta.R;
import com.example.psysfinsta.data.entity.Tag;
import com.example.psysfinsta.data.entity.TransactionEntity;
import com.example.psysfinsta.data.entity.TransactionWithTags;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<TransactionWithTags> transactions;
    private OnItemClickListener onItemClickListener;

    // ✅ Set new list of TransactionWithTags
    public void setTransactionWithTagsList(List<TransactionWithTags> transactions) {
        this.transactions = transactions;
        notifyDataSetChanged();
    }

    // ✅ Retrieve TransactionEntity at position (for swipe-to-delete, etc.)
    public TransactionEntity getTransactionAtPosition(int position) {
        if (transactions != null && position >= 0 && position < transactions.size()) {
            return transactions.get(position).transactionEntity;
        }
        return null;
    }

    // ✅ Interface updated to pass full TransactionWithTags
    public interface OnItemClickListener {
        void onItemClick(TransactionWithTags transactionWithTags);
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
        TransactionWithTags twt = transactions.get(position);
        TransactionEntity tx = twt.transactionEntity;

        holder.amount.setText("₹" + tx.getAmount());
        holder.category.setText(tx.getCategory());
        holder.description.setText(tx.getDescription());

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        holder.date.setText(sdf.format(tx.getDate()));

        // Optional: Show tags if you have a tags TextView
        if (!twt.tags.isEmpty()) {
            StringBuilder tagsText = new StringBuilder();
            for (Tag tag : twt.tags) {
                tagsText.append("#").append(tag.getName()).append(" ");
            }
            // If you add a TextView for tags, you can use:
            // holder.tags.setText(tagsText.toString().trim());
        }

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(twt);
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
            // Optional: Add if you want to display tags
            // tags = itemView.findViewById(R.id.text_tags);
        }
    }
}
