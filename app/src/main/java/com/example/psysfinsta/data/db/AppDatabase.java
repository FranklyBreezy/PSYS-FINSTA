package com.example.psysfinsta.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.psysfinsta.data.dao.TagDao;
import com.example.psysfinsta.data.dao.TransactionDao;
import com.example.psysfinsta.data.entity.Tag;
import com.example.psysfinsta.data.entity.TransactionEntity;
import com.example.psysfinsta.data.entity.TransactionTagCrossRef;
import com.example.psysfinsta.data.util.Converter;

@Database(
        entities = {
                TransactionEntity.class,
                Tag.class,
                TransactionTagCrossRef.class
        },
        version = 1,
        exportSchema = false
)
@TypeConverters({Converter.class})
public abstract class AppDatabase extends RoomDatabase {

    public abstract TransactionDao transactionDao();
    public abstract TagDao tagDao();  // You forgot this. Mandatory.

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "psysfinsta_database"
                            )
                            .fallbackToDestructiveMigration()  // use with caution
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
