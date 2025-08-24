package com.example.psysfinsta.data.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.psysfinsta.data.util.Converter;

import java.util.List;

@Entity(tableName = "transactions")
@TypeConverters({Converter.class})
public class TransactionEntity implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private double amount;
    private String category;
    private long date;
    private String description;
    private TransactionType type;
    private RecurrenceType recurrence;
    // Add these fields
    private int recurringGroupId;
    private String frequency;
    private boolean isRecurring;

    // Default constructor (Room requires this if you use "new TransactionEntity()")
    public TransactionEntity() {}

    // Getters & Setters
    public int getRecurringGroupId() {
        return recurringGroupId;
    }

    public void setRecurringGroupId(int recurringGroupId) {
        this.recurringGroupId = recurringGroupId;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public boolean isRecurring() {
        return isRecurring;
    }

    public void setIsRecurring(boolean isRecurring) {
        this.isRecurring = isRecurring;
    }


    // Transient tagNames field (ignored by Room)
    @Ignore
    private List<String> tagNames;

    public TransactionEntity(double amount, String category, long date, String description,
                             TransactionType type, RecurrenceType recurrence) {
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.description = description;
        this.type = type;
        this.recurrence = recurrence;
    }

    // New constructor including tagNames (optional)
    @Ignore
    public TransactionEntity(double amount, String category, long date, String description,
                             TransactionType type, RecurrenceType recurrence, List<String> tagNames) {
        this(amount, category, date, description, type, recurrence);
        this.tagNames = tagNames;
    }

    protected TransactionEntity(Parcel in) {
        id = in.readInt();
        amount = in.readDouble();
        category = in.readString();
        date = in.readLong();
        description = in.readString();
        type = TransactionType.valueOf(in.readString());
        recurrence = RecurrenceType.valueOf(in.readString());
        recurringGroupId = in.readInt();
        frequency = in.readString();
        isRecurring = in.readByte() != 0;
        tagNames = in.createStringArrayList();
    }


    public static final Creator<TransactionEntity> CREATOR = new Creator<TransactionEntity>() {
        @Override
        public TransactionEntity createFromParcel(Parcel in) {
            return new TransactionEntity(in);
        }

        @Override
        public TransactionEntity[] newArray(int size) {
            return new TransactionEntity[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeDouble(amount);
        dest.writeString(category);
        dest.writeLong(date);
        dest.writeString(description);
        dest.writeString(type.name());
        dest.writeString(recurrence.name());
        dest.writeInt(recurringGroupId);
        dest.writeString(frequency);
        dest.writeByte((byte) (isRecurring ? 1 : 0)); // boolean as byte
        dest.writeStringList(tagNames);
    }


    @Override
    public int describeContents() {
        return 0;
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

    // New getter/setter for tagNames

    @Ignore
    public List<String> getTagNames() {
        return tagNames;
    }

    @Ignore
    public void setTagNames(List<String> tagNames) {
        this.tagNames = tagNames;
    }
}
