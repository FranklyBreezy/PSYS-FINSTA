package com.example.psysfinsta.ui.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.psysfinsta.data.entity.Tag;
import com.example.psysfinsta.data.entity.TransactionEntity;
import com.example.psysfinsta.data.entity.TransactionType;
import com.example.psysfinsta.data.entity.TransactionWithTags;
import com.example.psysfinsta.data.repository.TagRepository;
import com.example.psysfinsta.data.repository.TransactionRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TransactionViewModel extends AndroidViewModel {

    private final TransactionRepository transactionRepository;
    private final TagRepository tagRepository;

    private final MutableLiveData<List<TransactionEntity>> filteredTransactions = new MutableLiveData<>();
    private final MutableLiveData<List<Tag>> allTags = new MutableLiveData<>();

    private LiveData<List<TransactionWithTags>> repositoryLiveData;
    private final Observer<List<TransactionWithTags>> repositoryObserver;

    private final Executor executor = Executors.newSingleThreadExecutor();

    // Filters
    private Set<String> selectedTagNames = new HashSet<>();
    private long filterStartDate = 0L;
    private long filterEndDate = Long.MAX_VALUE;
    private TransactionType filterType = null;

    public TransactionViewModel(@NonNull Application application) {
        super(application);
        transactionRepository = new TransactionRepository(application);
        tagRepository = new TagRepository(application);

        repositoryObserver = this::onRepositoryDataChanged;

        loadTags();
        loadTransactions(); // initial load
    }

    public LiveData<List<TransactionEntity>> getFilteredTransactions() {
        return filteredTransactions;
    }

    public LiveData<List<Tag>> getAllTags() {
        return allTags;
    }

    public void loadTags() {
        executor.execute(() -> {
            try {
                List<Tag> tags = tagRepository.getAllTags();
                allTags.postValue(tags);
            } catch (Exception e) {
                e.printStackTrace(); // You can replace this with Log.e
            }
        });
    }

    public void loadTransactions() {
        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                if (repositoryLiveData != null) {
                    repositoryLiveData.removeObserver(repositoryObserver);
                }

                repositoryLiveData = transactionRepository.getFilteredTransactions(filterType, filterStartDate, filterEndDate);
                repositoryLiveData.observeForever(repositoryObserver);
            } catch (Exception e) {
                e.printStackTrace(); // You can replace this with Log.e
            }
        });
    }

    private void onRepositoryDataChanged(List<TransactionWithTags> transactionWithTagsList) {
        if (transactionWithTagsList == null) {
            filteredTransactions.setValue(new ArrayList<>());
            return;
        }

        List<TransactionEntity> result = new ArrayList<>();

        for (TransactionWithTags twt : transactionWithTagsList) {
            if (selectedTagNames == null || selectedTagNames.isEmpty()) {
                result.add(twt.transactionEntity);
            } else {
                for (Tag tag : twt.tags) {
                    if (selectedTagNames.contains(tag.getName())) {
                        result.add(twt.transactionEntity);
                        break;
                    }
                }

            }
        }

        filteredTransactions.setValue(result);
    }

    public void setFilterTags(Set<String> tagNames) {
        this.selectedTagNames = tagNames != null ? tagNames : new HashSet<>();
        loadTransactions();
    }

    public void setFilterDateRange(long start, long end) {
        this.filterStartDate = start;
        this.filterEndDate = end;
        loadTransactions();
    }

    public void setFilterType(TransactionType type) {
        this.filterType = type;
        loadTransactions();
    }

    public void insertTransaction(TransactionEntity transactionEntity, List<String> tagNames) {
        executor.execute(() -> {
            try {
                transactionRepository.insert(transactionEntity, tagNames);
                loadTransactions();
                loadTags();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void updateTransaction(TransactionEntity transactionEntity) {
        executor.execute(() -> {
            try {
                transactionRepository.update(transactionEntity);
                loadTransactions();
                loadTags();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void deleteTransaction(TransactionEntity transactionEntity) {
        executor.execute(() -> {
            try {
                transactionRepository.delete(transactionEntity);
                loadTransactions();
                loadTags();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void addTag(String tagName) {
        if (tagName == null || tagName.trim().isEmpty()) return;
        executor.execute(() -> {
            try {
                tagRepository.insert(new Tag(tagName.trim()));
                loadTags();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (repositoryLiveData != null) {
            repositoryLiveData.removeObserver(repositoryObserver);
        }
    }
}
