package com.example.psysfinsta.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.psysfinsta.data.entity.Tag;
import com.example.psysfinsta.data.repository.TagRepository;

import java.util.List;

public class TagViewModel extends AndroidViewModel {

    private final TagRepository tagRepository;
    private final LiveData<List<Tag>> allTags;

    public TagViewModel(@NonNull Application application) {
        super(application);
        tagRepository = new TagRepository(application);
        allTags = tagRepository.getAllTagsLive();
    }

    public LiveData<List<Tag>> getAllTags() {
        return allTags;
    }

    public void insertTag(Tag tag) {
        tagRepository.insert(tag);
    }

    public void updateTag(Tag tag) {
        tagRepository.update(tag);
    }

    public void deleteTag(Tag tag) {
        tagRepository.delete(tag);
    }

    public List<Tag> getAllTagsImmediate() {
        return tagRepository.getAllTags();
    }
}
