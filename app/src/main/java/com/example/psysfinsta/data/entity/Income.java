package com.example.psysfinsta.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "incomes")
public class Income {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private long date;
    private String description;
    private String category;
    private double amount;

    public Income(long date, String description, String category, double amount){
        this.amount = amount;
        this.description = description;
        this.category = category;
        this.date = date;

    }
    public int getId(){return id;}
    public void setId(int id){this.id=id;}
    public double getAmount(){return amount;}
    public void setAmount(double amount){this.amount = amount;}
    public String getDescription(){return description;}
    public void setDescription(String description){this.description = description;}
    public String getCategory(){return category;}
    public void setCategory(){this.category = category;}
    public long getDate(){return date;}
    public void setDate(long date){this.date=date;}
}
