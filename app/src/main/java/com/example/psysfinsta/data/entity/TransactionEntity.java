package com.example.psysfinsta.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.psysfinsta.data.util.Converter;

import java.io.Serializable;

@Entity(tableName = "transactions")
@TypeConverters({Converter.class})
public class TransactionEntity implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private double amount;

    private String category;

    private long date;

    private String description;

    private TransactionType type;  // INCOME, EXPENSE, DEBT_ASSET, DEBT_LIABILITY

    private RecurrenceType recurrence; // NONE, DAILY, WEEKLY, MONTHLY, YEARLY

    public TransactionEntity(double amount, String category, long date, String description,
                             TransactionType type, RecurrenceType recurrence) {
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.description = description;
        this.type = type;
        this.recurrence = recurrence;
    }

    // Getters and Setters

    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    public double getAmount() { return amount; }

    public void setAmount(double amount) { this.amount = amount; }

    public String getCategory() { return category; }

    public void setCategory(String category) { this.category = category; }

    public long getDate() { return date; }

    public void setDate(long date) { this.date = date; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public TransactionType getType() { return type; }

    public void setType(TransactionType type) { this.type = type; }

    public RecurrenceType getRecurrence() { return recurrence; }

    public void setRecurrence(RecurrenceType recurrence) { this.recurrence = recurrence; }
}
