package com.example.psysfinsta.data.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import java.util.List;

public class TransactionWithTags implements Parcelable {
    @Embedded
    public TransactionEntity transactionEntity;

    @Relation(
            parentColumn = "id",
            entityColumn = "tagId",
            associateBy = @Junction(
                    value = TransactionTagCrossRef.class,
                    parentColumn = "transactionId",
                    entityColumn = "tagId"
            )
    )
    public List<Tag> tags;

    public TransactionWithTags() {}

    protected TransactionWithTags(Parcel in) {
        transactionEntity = in.readParcelable(TransactionEntity.class.getClassLoader());
        tags = in.createTypedArrayList(Tag.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(transactionEntity, flags);
        dest.writeTypedList(tags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TransactionWithTags> CREATOR = new Creator<TransactionWithTags>() {
        @Override
        public TransactionWithTags createFromParcel(Parcel in) {
            return new TransactionWithTags(in);
        }

        @Override
        public TransactionWithTags[] newArray(int size) {
            return new TransactionWithTags[size];
        }
    };

    // Getters
    public List<Tag> getTags() {
        return tags;
    }

    public TransactionEntity getTransactionEntity() {
        return transactionEntity;
    }
}
