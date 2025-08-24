package com.example.psysfinsta.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "budgets")
public class Budget {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String tagName;
    public double limit;
    public long monthTimestamp;

    public Budget(String tagName, double limit, long monthTimestamp) {
        this.tagName = tagName;
        this.limit = limit;
        this.monthTimestamp = monthTimestamp;
    }
}