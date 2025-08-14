package com.example.psysfinsta.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;

@Entity(
        tableName = "transaction_tag_cross_ref",
        primaryKeys = {"transactionId", "tagId"},
        foreignKeys = {
                @ForeignKey(entity = TransactionEntity.class,
                        parentColumns = "id",
                        childColumns = "transactionId",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Tag.class,
                        parentColumns = "tagId",
                        childColumns = "tagId",
                        onDelete = ForeignKey.CASCADE)
        }
)
public class TransactionTagCrossRef {
    public int transactionId;
    public int tagId;

    public TransactionTagCrossRef(int transactionId, int tagId) {
        this.transactionId = transactionId;
        this.tagId = tagId;
    }
}
