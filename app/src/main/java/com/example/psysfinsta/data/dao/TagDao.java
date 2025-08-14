package com.example.psysfinsta.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.psysfinsta.data.entity.Tag;

import java.util.List;

@Dao
public interface TagDao {

    @Insert
    long insertTag(Tag tag);  // changed to return inserted rowId

    @Delete
    void deleteTag(Tag tag);

    @Update
    void updateTag(Tag tag);

    @Query("SELECT * FROM tags ORDER BY tag_name ASC")
    List<Tag> getAllTags();

    @Query("SELECT * FROM tags ORDER BY tag_name ASC")
    LiveData<List<Tag>> getAllTagsLive();

    @Query("SELECT * FROM tags WHERE tag_name = :name LIMIT 1")
    Tag getTagByName(String name);

}
