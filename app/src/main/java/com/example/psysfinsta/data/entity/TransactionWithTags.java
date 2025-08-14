package com.example.psysfinsta.data.entity;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import java.util.List;

public class TransactionWithTags {
    @Embedded
    public TransactionEntity transactionEntity;

    @Relation(
            parentColumn = "id",              // TransactionEntity.id
            entityColumn = "tagId",           // Tag.tagId
            associateBy = @Junction(
                    value = TransactionTagCrossRef.class,
                    parentColumn = "transactionId",  // field in TransactionTagCrossRef
                    entityColumn = "tagId"           // field in TransactionTagCrossRef
            )
    )
    public List<Tag> tags;
}
