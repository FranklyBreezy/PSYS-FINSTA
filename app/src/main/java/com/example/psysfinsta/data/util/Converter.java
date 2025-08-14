package com.example.psysfinsta.data.util;

import androidx.room.TypeConverter;

import com.example.psysfinsta.data.entity.RecurrenceType;
import com.example.psysfinsta.data.entity.TransactionType;

public class Converter {

    @TypeConverter
    public static String fromTransactionType(TransactionType type) {
        return type == null ? null : type.name();
    }

    @TypeConverter
    public static TransactionType toTransactionType(String type) {
        return type == null ? null : TransactionType.valueOf(type);
    }

    @TypeConverter
    public static String fromRecurrenceType(RecurrenceType recurrence) {
        return recurrence == null ? null : recurrence.name();
    }

    @TypeConverter
    public static RecurrenceType toRecurrenceType(String recurrence) {
        return recurrence == null ? null : RecurrenceType.valueOf(recurrence);
    }
}
