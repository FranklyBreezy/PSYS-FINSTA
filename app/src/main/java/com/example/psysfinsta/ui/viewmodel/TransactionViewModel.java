package com.example.psysfinsta.ui.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

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
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TransactionViewModel extends AndroidViewModel {

    private static final String TAG = "TransactionViewModel";

    private final TransactionRepository transactionRepository;
    private final TagRepository tagRepository;

    private final MutableLiveData<List<TransactionEntity>> filteredTransactions = new MutableLiveData<>();
    private final MutableLiveData<List<Tag>> allTags = new MutableLiveData<>();

    private LiveData<List<TransactionWithTags>> repositoryLiveData;
    private final Observer<List<TransactionWithTags>> repositoryObserver;

    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // Filters
    private Set<String> selectedTagNames = new HashSet<>();
    private long filterStartDate = 0L;
    private long filterEndDate = Long.MAX_VALUE;
    private TransactionType filterType = null;

    // Grouped data and balances
    private final MutableLiveData<Map<String, List<TransactionEntity>>> weeklyGroupedCurrentMonth = new MutableLiveData<>();
    private final MutableLiveData<Map<String, Double>> monthlySummariesPastMonths = new MutableLiveData<>();
    private final MutableLiveData<Map<String, Double>> yearlySummaries = new MutableLiveData<>();

    private final MutableLiveData<Double> currentMonthCashFlow = new MutableLiveData<>();
    private final MutableLiveData<Double> currentMonthNetLiquidity = new MutableLiveData<>();

    private final MutableLiveData<Double> weeklyNetBalance = new MutableLiveData<>();
    private final MutableLiveData<Double> monthlyNetBalance = new MutableLiveData<>();
    private final MutableLiveData<Double> yearlyNetBalance = new MutableLiveData<>();

    // Undo
    private TransactionEntity lastDeletedTransaction;

    public TransactionViewModel(@NonNull Application application) {
        super(application);
        transactionRepository = new TransactionRepository(application);
        tagRepository = new TagRepository(application);
        repositoryObserver = this::onRepositoryDataChanged;

        loadTags();
        loadTransactions();
    }

    public LiveData<List<TransactionWithTags>> getRepositoryLiveData() {
        return repositoryLiveData;
    }

    public LiveData<List<TransactionEntity>> getFilteredTransactions() {
        return filteredTransactions;
    }

    public LiveData<List<Tag>> getAllTags() {
        return allTags;
    }

    public LiveData<Map<String, List<TransactionEntity>>> getWeeklyGroupedCurrentMonth() {
        return weeklyGroupedCurrentMonth;
    }

    public LiveData<Map<String, Double>> getMonthlySummariesPastMonths() {
        return monthlySummariesPastMonths;
    }

    public LiveData<Map<String, Double>> getYearlySummaries() {
        return yearlySummaries;
    }

    public LiveData<Double> getCurrentMonthCashFlow() {
        return currentMonthCashFlow;
    }

    public LiveData<Double> getCurrentMonthNetLiquidity() {
        return currentMonthNetLiquidity;
    }

    public LiveData<Double> getWeeklyNetBalance() {
        return weeklyNetBalance;
    }

    public LiveData<Double> getMonthlyNetBalance() {
        return monthlyNetBalance;
    }

    public LiveData<Double> getYearlyNetBalance() {
        return yearlyNetBalance;
    }
    // --- Paging state ---
    private final MutableLiveData<Integer> currentPage = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> pageSize = new MutableLiveData<>(10); // default 10 per page

    public LiveData<Integer> getCurrentPage() { return currentPage; }
    public LiveData<Integer> getPageSize() { return pageSize; }

    public void setPageSize(int size) {
        pageSize.setValue(size);
        currentPage.setValue(0); // reset to first page
    }

    public void nextPage() {
        List<TransactionWithTags> all = repositoryLiveData.getValue();
        if (all == null) return;
        int totalPages = (int) Math.ceil((double) all.size() / pageSize.getValue());
        if (currentPage.getValue() < totalPages - 1) {
            currentPage.setValue(currentPage.getValue() + 1);
        }
    }

    public void prevPage() {
        if (currentPage.getValue() > 0) {
            currentPage.setValue(currentPage.getValue() - 1);
        }
    }

    public List<TransactionWithTags> getPagedTransactions() {
        List<TransactionWithTags> all = repositoryLiveData.getValue();
        if (all == null) return Collections.emptyList();
        int start = currentPage.getValue() * pageSize.getValue();
        int end = Math.min(start + pageSize.getValue(), all.size());
        return all.subList(start, end);
    }
    public void loadTags() {
        executor.execute(() -> {
            try {
                List<Tag> tags = tagRepository.getAllTags();
                allTags.postValue(tags);
            } catch (Exception e) {
                Log.e(TAG, "Error loading tags", e);
            }
        });
    }

    public void loadTransactions() {
        try {
            if (repositoryLiveData != null) {
                repositoryLiveData.removeObserver(repositoryObserver);
            }
            repositoryLiveData = transactionRepository.getFilteredTransactions(filterType, filterStartDate, filterEndDate);
            repositoryLiveData.observeForever(repositoryObserver);
        } catch (Exception e) {
            Log.e(TAG, "Error loading transactions", e);
        }
    }

    public void updateTransaction(TransactionEntity tx, List<String> tags) {
        executor.execute(() -> {
            try {
                transactionRepository.update(tx, tags);
                mainHandler.post(this::loadTransactions);
            } catch (Exception e) {
                Log.e(TAG, "Error updating transaction", e);
            }
        });
    }

    public void updateTransaction(TransactionEntity transactionEntity) {
        updateTransaction(transactionEntity, transactionEntity.getTagNames());
    }

    private void onRepositoryDataChanged(List<TransactionWithTags> transactionWithTagsList) {
        if (transactionWithTagsList == null) {
            filteredTransactions.setValue(new ArrayList<>());
            clearGroupedAndBalances();
            return;
        }

        List<TransactionEntity> result = new ArrayList<>();
        for (TransactionWithTags twt : transactionWithTagsList) {
            if (selectedTagNames.isEmpty()) {
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
        executor.execute(() -> groupTransactionsAndCalculateBalances(result));
    }

    private void clearGroupedAndBalances() {
        weeklyGroupedCurrentMonth.postValue(new HashMap<>());
        monthlySummariesPastMonths.postValue(new HashMap<>());
        yearlySummaries.postValue(new HashMap<>());

        currentMonthCashFlow.postValue(0.0);
        currentMonthNetLiquidity.postValue(0.0);

        weeklyNetBalance.postValue(0.0);
        monthlyNetBalance.postValue(0.0);
        yearlyNetBalance.postValue(0.0);
    }

    private void groupTransactionsAndCalculateBalances(List<TransactionEntity> transactions) {
        try {
            Map<String, List<TransactionEntity>> groupedByWeek = new HashMap<>();
            Map<String, Double> monthlySummary = new HashMap<>();
            Map<String, Double> yearlySummary = new HashMap<>();

            double totalIncomeCurrentMonth = 0;
            double totalExpenseCurrentMonth = 0;
            double totalDebtAssets = 0;
            double totalDebtLiabilities = 0;

            // For weekly net balance calculation
            Map<String, Double> weeklyNetMap = new HashMap<>();

            long now = System.currentTimeMillis();
            Calendar calendar = Calendar.getInstance();

            for (TransactionEntity tx : transactions) {
                calendar.setTimeInMillis(tx.getDate());
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int weekOfMonth = calendar.get(Calendar.WEEK_OF_MONTH);

                calendar.setTimeInMillis(now);
                int currentYear = calendar.get(Calendar.YEAR);
                int currentMonth = calendar.get(Calendar.MONTH);

                if (year == currentYear && month == currentMonth) {
                    // Group by week for current month
                    String weekKey = "Week " + weekOfMonth;
                    groupedByWeek.computeIfAbsent(weekKey, k -> new ArrayList<>()).add(tx);

                    // Track weekly net
                    double netForTxWeek = tx.getType() == TransactionType.INCOME ? tx.getAmount() : -tx.getAmount();
                    weeklyNetMap.put(weekKey, weeklyNetMap.getOrDefault(weekKey, 0.0) + netForTxWeek);

                    // Monthly totals
                    if (tx.getType() == TransactionType.INCOME) totalIncomeCurrentMonth += tx.getAmount();
                    else if (tx.getType() == TransactionType.EXPENSE) totalExpenseCurrentMonth += tx.getAmount();

                    // Debt tracking
                    if (isDebtAsset(tx)) totalDebtAssets += tx.getAmount();
                    else if (isDebtLiability(tx)) totalDebtLiabilities += tx.getAmount();
                }

                // Monthly summary for current year
                if (year == currentYear) {
                    String monthKey = year + "-" + (month + 1);
                    double netForTx = tx.getType() == TransactionType.INCOME ? tx.getAmount() : -tx.getAmount();
                    monthlySummary.put(monthKey, monthlySummary.getOrDefault(monthKey, 0.0) + netForTx);
                }

                // Yearly summary for all years
                double netForTxYear = tx.getType() == TransactionType.INCOME ? tx.getAmount() : -tx.getAmount();
                yearlySummary.put(String.valueOf(year), yearlySummary.getOrDefault(String.valueOf(year), 0.0) + netForTxYear);
            }

            // Post grouped data
            weeklyGroupedCurrentMonth.postValue(groupedByWeek);
            monthlySummariesPastMonths.postValue(monthlySummary);
            yearlySummaries.postValue(yearlySummary);

            // Net balances
            double cashFlow = totalIncomeCurrentMonth - totalExpenseCurrentMonth;
            double netLiquidity = cashFlow + (totalDebtAssets - totalDebtLiabilities);

            monthlyNetBalance.postValue(cashFlow);
            currentMonthNetLiquidity.postValue(netLiquidity);

            // Yearly net balance for current year
            yearlyNetBalance.postValue(yearlySummary.getOrDefault(String.valueOf(calendar.get(Calendar.YEAR)), 0.0));

            // Weekly net balance default → latest week in current month
            double latestWeekNet = 0.0;
            if (!weeklyNetMap.isEmpty()) {
                String latestWeekKey = weeklyNetMap.keySet().stream()
                        .max(Comparator.comparingInt(w -> Integer.parseInt(w.replace("Week ", ""))))
                        .orElse(null);
                if (latestWeekKey != null) {
                    latestWeekNet = weeklyNetMap.get(latestWeekKey);
                }
            }
            weeklyNetBalance.postValue(latestWeekNet);

        } catch (Exception e) {
            Log.e(TAG, "Error grouping transactions and calculating balances", e);
        }
    }

    private boolean isDebtAsset(TransactionEntity tx) {
        try {
            String category = tx.getCategory();
            return category.equalsIgnoreCase("Loan Given") || category.equalsIgnoreCase("Receivable");
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isDebtLiability(TransactionEntity tx) {
        try {
            String category = tx.getCategory();
            return category.equalsIgnoreCase("Loan Taken") || category.equalsIgnoreCase("Credit Card");
        } catch (Exception e) {
            return false;
        }
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
                mainHandler.post(() -> {
                    loadTransactions();
                    loadTags();
                });
            } catch (Exception e) {
                Log.e(TAG, "Error inserting transaction", e);
            }
        });
    }

    public void deleteTransaction(TransactionEntity transactionEntity) {
        executor.execute(() -> {
            try {
                lastDeletedTransaction = transactionEntity;
                transactionRepository.delete(transactionEntity);
                mainHandler.post(() -> {
                    loadTransactions();
                    loadTags();
                });
            } catch (Exception e) {
                Log.e(TAG, "Error deleting transaction", e);
            }
        });
    }

    public void undoDelete() {
        if (lastDeletedTransaction != null) {
            insertTransaction(lastDeletedTransaction, lastDeletedTransaction.getTagNames());
            lastDeletedTransaction = null;
        }
    }

    public void deleteSingle(TransactionEntity transactionEntity) {
        executor.execute(() -> {
            try {
                transactionRepository.delete(transactionEntity);
                mainHandler.post(this::loadTransactions);
            } catch (Exception e) {
                Log.e(TAG, "Error deleting single instance", e);
            }
        });
    }

    public void deleteFuture(TransactionEntity transactionEntity) {
        executor.execute(() -> {
            try {
                transactionRepository.deleteFuture(transactionEntity.getDate());
                mainHandler.post(this::loadTransactions);
            } catch (Exception e) {
                Log.e(TAG, "Error deleting future instances", e);
            }
        });
    }

    public void addTag(String tagName) {
        if (tagName == null || tagName.trim().isEmpty()) return;
        executor.execute(() -> {
            try {
                tagRepository.insert(new Tag(tagName.trim()));
                mainHandler.post(this::loadTags);
            } catch (Exception e) {
                Log.e(TAG, "Error adding tag", e);
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
