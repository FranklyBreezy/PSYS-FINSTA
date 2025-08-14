package com.example.psysfinsta.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.psysfinsta.data.dao.TagDao;
import com.example.psysfinsta.data.db.AppDatabase;
import com.example.psysfinsta.data.entity.Tag;

import java.util.List;

public class TagRepository {
    private final TagDao tagDao;
    private final LiveData<List<Tag>> allTagsLive;

    public TagRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        tagDao = db.tagDao();
        allTagsLive = tagDao.getAllTagsLive();
    }

    public List<Tag> getAllTags() {
        return tagDao.getAllTags();
    }

    public LiveData<List<Tag>> getAllTagsLive() {
        return allTagsLive;
    }

    public void insert(Tag tag) {
        new Thread(() -> tagDao.insertTag(tag)).start();
    }

    public void delete(Tag tag) {
        new Thread(() -> tagDao.deleteTag(tag)).start();
    }
    public void update(Tag tag) {
        new Thread(() -> tagDao.updateTag(tag)).start();
    }

}
