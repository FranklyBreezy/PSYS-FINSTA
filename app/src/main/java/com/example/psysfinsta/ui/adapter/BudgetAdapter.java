package com.example.psysfinsta.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.psysfinsta.R;
import com.example.psysfinsta.data.entity.Budget;

import java.util.List;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {

    private List<Budget> budgetList;

    public BudgetAdapter(List<Budget> budgetList) {
        this.budgetList = budgetList;
    }

    public void updateData(List<Budget> newList) {
        this.budgetList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_budget, parent, false);
        return new BudgetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        Budget item = budgetList.get(position);
        holder.tagName.setText(item.tagName);
        holder.limit.setText("₹" + item.limit);
    }

    @Override
    public int getItemCount() {
        return budgetList.size();
    }

    static class BudgetViewHolder extends RecyclerView.ViewHolder {
        TextView tagName, limit;

        public BudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            tagName = itemView.findViewById(R.id.txtTagName);
            limit = itemView.findViewById(R.id.txtLimit);
        }
    }
}