package com.example.psysfinsta.data.db;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class Migrations {

    // Version 1 → 2
    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Adding columns to transactions
            database.execSQL("ALTER TABLE transactions ADD COLUMN recurringGroupId INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE transactions ADD COLUMN frequency TEXT NOT NULL DEFAULT ''");
            database.execSQL("ALTER TABLE transactions ADD COLUMN recurrence TEXT NOT NULL DEFAULT 'undefined'");
            database.execSQL("ALTER TABLE transactions ADD COLUMN type TEXT NOT NULL DEFAULT 'undefined'");
            database.execSQL("ALTER TABLE transactions ADD COLUMN isRecurring INTEGER NOT NULL DEFAULT 0");

            // Create cross-ref table
            database.execSQL("CREATE TABLE IF NOT EXISTS transaction_tag_cross_ref (" +
                    "transactionId INTEGER NOT NULL, " +
                    "tagId INTEGER NOT NULL, " +
                    "PRIMARY KEY(transactionId, tagId), " +
                    "FOREIGN KEY(transactionId) REFERENCES transactions(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(tagId) REFERENCES tags(tagId) ON DELETE CASCADE" +
                    ")");
        }
    };

    // Version 2 → 3
    public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Fix inconsistent values
            database.execSQL("UPDATE transactions SET recurrence = 'NONE' WHERE recurrence IS NULL OR recurrence = 'undefined'");
            database.execSQL("UPDATE transactions SET type = 'EXPENSE' WHERE type IS NULL OR type = 'undefined'");

            // Ensure index exists
            database.execSQL("CREATE INDEX IF NOT EXISTS index_transaction_tag_cross_ref_tagId ON transaction_tag_cross_ref(tagId)");

            // ✅ Create the 'budgets' table that was missing
            database.execSQL("CREATE TABLE IF NOT EXISTS budgets (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "tagName TEXT, " +
                    "`limit` REAL NOT NULL, " +
                    "monthTimestamp INTEGER NOT NULL" +
                    ")");
        }
    };

    // Version 3 → 4 (idempotent)
    public static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Recreate index just to ensure consistency
            database.execSQL("CREATE INDEX IF NOT EXISTS index_transaction_tag_cross_ref_tagId ON transaction_tag_cross_ref(tagId)");
        }
    };
}
