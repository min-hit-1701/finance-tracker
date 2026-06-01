package com.uit.minhho.financetracker.data.remote;

import android.content.Context;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.uit.minhho.financetracker.data.local.entity.Budget;
import com.uit.minhho.financetracker.data.local.entity.Category;
import com.uit.minhho.financetracker.data.local.entity.Transaction;
import com.uit.minhho.financetracker.data.local.entity.Wallet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PersonalApiClient {
    private static final String CATEGORIES = "personal_categories";
    private static final String WALLETS = "personal_wallets";
    private static final String TRANSACTIONS = "personal_transactions";
    private static final String BUDGETS = "personal_budgets";

    private final Context context;

    public PersonalApiClient(Context context) {
        this.context = context.getApplicationContext();
    }

    public ApiResult<List<Category>> getCategories(Boolean isIncome) {
        try {
            FirebaseSession.Session session = requireSession();
            if (!session.valid) {
                return ApiResult.error(session.errorMessage);
            }

            Query query = collection(session.uid, CATEGORIES).orderBy("id", Query.Direction.DESCENDING);
            if (isIncome != null) {
                query = collection(session.uid, CATEGORIES).whereEqualTo("isIncome", isIncome);
            }

            QuerySnapshot snapshot = Tasks.await(query.get());
            List<Category> result = new ArrayList<>();
            for (DocumentSnapshot row : snapshot.getDocuments()) {
                Category category = new Category(
                        row.getString("name") == null ? "" : row.getString("name"),
                        intValue(row.get("iconRes")),
                        row.getString("colorHex") == null ? "#F44336" : row.getString("colorHex"),
                        boolValue(row.get("isIncome"))
                );
                category.setId(intValue(row.get("id"), FirebaseSession.positiveHash(row.getId())));
                result.add(category);
            }
            return ApiResult.success("Categories loaded", result);
        } catch (Exception e) {
            return ApiResult.error(firebaseMessage(e, "Không thể tải danh mục"));
        }
    }

    public ApiResult<Category> createCategory(Category category) {
        try {
            FirebaseSession.Session session = requireSession();
            if (!session.valid) {
                return ApiResult.error(session.errorMessage);
            }

            int id = category.getId() > 0 ? category.getId() : FirebaseSession.nextId();
            Map<String, Object> data = new HashMap<>();
            data.put("id", id);
            data.put("name", category.getName());
            data.put("iconRes", category.getIconRes());
            data.put("colorHex", category.getColorHex());
            data.put("isIncome", category.isIncome());
            data.put("updatedAt", System.currentTimeMillis());

            Tasks.await(collection(session.uid, CATEGORIES).document(String.valueOf(id)).set(data));
            category.setId(id);
            return ApiResult.success("Đã lưu danh mục vào Firebase!", category);
        } catch (Exception e) {
            return ApiResult.error(firebaseMessage(e, "Không thể lưu danh mục"));
        }
    }

    public ApiResult<Void> deleteCategory(Category category) {
        try {
            FirebaseSession.Session session = requireSession();
            if (!session.valid) {
                return ApiResult.error(session.errorMessage);
            }
            if (category == null || category.getId() <= 0) {
                return ApiResult.error("Không tìm thấy danh mục cần xóa");
            }

            Tasks.await(collection(session.uid, CATEGORIES).document(String.valueOf(category.getId())).delete());
            return ApiResult.success("Đã xóa danh mục", null);
        } catch (Exception e) {
            return ApiResult.error(firebaseMessage(e, "Không thể xóa danh mục"));
        }
    }

    public ApiResult<List<Wallet>> getWallets() {
        try {
            FirebaseSession.Session session = requireSession();
            if (!session.valid) {
                return ApiResult.error(session.errorMessage);
            }

            QuerySnapshot snapshot = Tasks.await(collection(session.uid, WALLETS)
                    .orderBy("id", Query.Direction.DESCENDING)
                    .get());

            List<Wallet> result = new ArrayList<>();
            for (DocumentSnapshot row : snapshot.getDocuments()) {
                Wallet wallet = new Wallet(
                        row.getString("name") == null ? "" : row.getString("name"),
                        doubleValue(row.get("balance")),
                        row.getString("type") == null ? "Cash" : row.getString("type"),
                        false
                );
                wallet.setId(intValue(row.get("id"), FirebaseSession.positiveHash(row.getId())));
                result.add(wallet);
            }
            return ApiResult.success("Wallets loaded", result);
        } catch (Exception e) {
            return ApiResult.error(firebaseMessage(e, "Không thể tải ví"));
        }
    }

    public ApiResult<Wallet> createWallet(Wallet wallet) {
        try {
            FirebaseSession.Session session = requireSession();
            if (!session.valid) {
                return ApiResult.error(session.errorMessage);
            }

            int id = wallet.getId() > 0 ? wallet.getId() : FirebaseSession.nextId();
            Map<String, Object> data = new HashMap<>();
            data.put("id", id);
            data.put("name", wallet.getName());
            data.put("balance", wallet.getBalance());
            data.put("type", wallet.getType());
            data.put("isBusiness", false);
            data.put("updatedAt", System.currentTimeMillis());

            Tasks.await(collection(session.uid, WALLETS).document(String.valueOf(id)).set(data));
            wallet.setId(id);
            return ApiResult.success("Đã lưu ví vào Firebase!", wallet);
        } catch (Exception e) {
            return ApiResult.error(firebaseMessage(e, "Không thể lưu ví"));
        }
    }

    public ApiResult<Void> deleteWallet(Wallet wallet) {
        try {
            FirebaseSession.Session session = requireSession();
            if (!session.valid) {
                return ApiResult.error(session.errorMessage);
            }

            Tasks.await(collection(session.uid, WALLETS).document(String.valueOf(wallet.getId())).delete());
            return ApiResult.success("Đã xóa ví", null);
        } catch (Exception e) {
            return ApiResult.error(firebaseMessage(e, "Không thể xóa ví"));
        }
    }

    public ApiResult<List<Transaction>> getTransactions() {
        try {
            FirebaseSession.Session session = requireSession();
            if (!session.valid) {
                return ApiResult.error(session.errorMessage);
            }

            QuerySnapshot snapshot = Tasks.await(collection(session.uid, TRANSACTIONS)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get());

            List<Transaction> result = new ArrayList<>();
            for (DocumentSnapshot row : snapshot.getDocuments()) {
                Transaction transaction = new Transaction(
                        doubleValue(row.get("amount")),
                        longValue(row.get("timestamp"), System.currentTimeMillis()),
                        row.getString("note") == null ? "" : row.getString("note"),
                        intValue(row.get("categoryId")),
                        intValue(row.get("walletId")),
                        boolValue(row.get("isIncome")),
                        false,
                        intValue(row.get("iconRes"))
                );
                transaction.setId(intValue(row.get("id"), intValue(row.getId(), FirebaseSession.positiveHash(row.getId()))));
                result.add(transaction);
            }
            return ApiResult.success("Transactions loaded", result);
        } catch (Exception e) {
            return ApiResult.error(firebaseMessage(e, "Không thể tải giao dịch"));
        }
    }

    public ApiResult<Transaction> createTransaction(Transaction transaction) {
        try {
            FirebaseSession.Session session = requireSession();
            if (!session.valid) {
                return ApiResult.error(session.errorMessage);
            }

            int id = transaction.getId() > 0 ? transaction.getId() : FirebaseSession.nextId();
            Map<String, Object> data = new HashMap<>();
            data.put("id", id);
            data.put("amount", transaction.getAmount());
            data.put("timestamp", transaction.getTimestamp());
            data.put("displayTime", formatTimestamp(transaction.getTimestamp()));
            data.put("note", transaction.getNote());
            data.put("categoryId", transaction.getCategoryId());
            data.put("walletId", transaction.getWalletId());
            data.put("isIncome", transaction.isIncome());
            data.put("isBusiness", false);
            data.put("iconRes", transaction.getIconRes());
            data.put("updatedAt", System.currentTimeMillis());

            CollectionReference wallets = collection(session.uid, WALLETS);
            Tasks.await(collection(session.uid, TRANSACTIONS).document(String.valueOf(id)).set(data));
            updateWalletBalance(wallets, transaction.getWalletId(), signedAmount(transaction));
            transaction.setId(id);
            return ApiResult.success("Đã lưu giao dịch vào Firebase!", transaction);
        } catch (Exception e) {
            return ApiResult.error(firebaseMessage(e, "Không thể lưu giao dịch"));
        }
    }

    public ApiResult<Void> deleteTransaction(Transaction transaction) {
        try {
            FirebaseSession.Session session = requireSession();
            if (!session.valid) {
                return ApiResult.error(session.errorMessage);
            }
            if (transaction.getId() <= 0) {
                return ApiResult.error("Không tìm thấy giao dịch cần xóa");
            }

            CollectionReference transactions = collection(session.uid, TRANSACTIONS);
            DocumentSnapshot snapshot = Tasks.await(transactions.document(String.valueOf(transaction.getId())).get());
            if (!snapshot.exists()) {
                return ApiResult.error("Giao dịch không còn tồn tại");
            }

            Transaction stored = new Transaction(
                    doubleValue(snapshot.get("amount")),
                    longValue(snapshot.get("timestamp"), System.currentTimeMillis()),
                    snapshot.getString("note") == null ? "" : snapshot.getString("note"),
                    intValue(snapshot.get("categoryId")),
                    intValue(snapshot.get("walletId")),
                    boolValue(snapshot.get("isIncome")),
                    false,
                    intValue(snapshot.get("iconRes"))
            );
            stored.setId(intValue(snapshot.get("id"), transaction.getId()));

            Tasks.await(transactions.document(String.valueOf(transaction.getId())).delete());
            updateWalletBalance(collection(session.uid, WALLETS), stored.getWalletId(), -signedAmount(stored));
            return ApiResult.success("Đã xóa giao dịch", null);
        } catch (Exception e) {
            return ApiResult.error(firebaseMessage(e, "Không thể xóa giao dịch"));
        }
    }

    public ApiResult<List<Budget>> getBudgets() {
        try {
            FirebaseSession.Session session = requireSession();
            if (!session.valid) {
                return ApiResult.error(session.errorMessage);
            }

            QuerySnapshot snapshot = Tasks.await(collection(session.uid, BUDGETS)
                    .orderBy("id", Query.Direction.DESCENDING)
                    .get());
            QuerySnapshot transactionSnapshot = Tasks.await(collection(session.uid, TRANSACTIONS).get());

            List<Budget> result = new ArrayList<>();
            for (DocumentSnapshot row : snapshot.getDocuments()) {
                int categoryId = intValue(row.get("categoryId"));
                String period = row.getString("period") == null ? "" : row.getString("period");
                Budget budget = new Budget(
                        categoryId,
                        doubleValue(row.get("limitAmount")),
                        spentForBudget(transactionSnapshot.getDocuments(), categoryId, period),
                        period
                );
                budget.setId(intValue(row.get("id"), FirebaseSession.positiveHash(row.getId())));
                result.add(budget);
            }
            return ApiResult.success("Budgets loaded", result);
        } catch (Exception e) {
            return ApiResult.error(firebaseMessage(e, "Không thể tải ngân sách"));
        }
    }

    public ApiResult<Budget> createBudget(Budget budget) {
        try {
            FirebaseSession.Session session = requireSession();
            if (!session.valid) {
                return ApiResult.error(session.errorMessage);
            }

            int id = budget.getId() > 0 ? budget.getId() : FirebaseSession.nextId();
            Map<String, Object> data = new HashMap<>();
            data.put("id", id);
            data.put("categoryId", budget.getCategoryId());
            data.put("limitAmount", budget.getLimitAmount());
            data.put("spentAmount", budget.getSpentAmount());
            data.put("period", budget.getPeriod());
            data.put("updatedAt", System.currentTimeMillis());

            Tasks.await(collection(session.uid, BUDGETS).document(String.valueOf(id)).set(data));
            budget.setId(id);
            return ApiResult.success("Đã thiết lập ngân sách trên Firebase!", budget);
        } catch (Exception e) {
            return ApiResult.error(firebaseMessage(e, "Không thể lưu ngân sách"));
        }
    }

    public ApiResult<Void> deleteBudget(Budget budget) {
        try {
            FirebaseSession.Session session = requireSession();
            if (!session.valid) {
                return ApiResult.error(session.errorMessage);
            }
            if (budget == null || budget.getId() <= 0) {
                return ApiResult.error("Không tìm thấy ngân sách trong database");
            }

            Tasks.await(collection(session.uid, BUDGETS).document(String.valueOf(budget.getId())).delete());
            return ApiResult.success("Đã xóa ngân sách", null);
        } catch (Exception e) {
            return ApiResult.error(firebaseMessage(e, "Không thể xóa ngân sách"));
        }
    }

    public ApiResult<Summary> getSummary() {
        try {
            ApiResult<List<Transaction>> transactions = getTransactions();
            if (!transactions.success) {
                return ApiResult.error(transactions.message);
            }

            double totalIncome = 0.0;
            double totalExpense = 0.0;
            for (Transaction row : transactions.data) {
                if (row.isIncome()) {
                    totalIncome += row.getAmount();
                } else {
                    totalExpense += row.getAmount();
                }
            }
            double totalBalance = totalIncome - totalExpense;
            return ApiResult.success("Summary loaded", new Summary(totalBalance, totalIncome, totalExpense));
        } catch (Exception e) {
            return ApiResult.error(firebaseMessage(e, "Không thể tải tổng quan"));
        }
    }

    private FirebaseSession.Session requireSession() {
        return FirebaseSession.require(context);
    }

    private CollectionReference collection(String uid, String name) {
        return FirebaseSession.collection(uid, name);
    }

    private void updateWalletBalance(CollectionReference wallets, int walletId, double delta) throws Exception {
        if (walletId <= 0) {
            return;
        }
        DocumentSnapshot snapshot = Tasks.await(wallets.document(String.valueOf(walletId)).get());
        if (!snapshot.exists()) {
            return;
        }
        double current = doubleValue(snapshot.get("balance"));
        Tasks.await(wallets.document(String.valueOf(walletId)).update("balance", current + delta));
    }

    private double signedAmount(Transaction transaction) {
        return transaction.isIncome() ? transaction.getAmount() : -transaction.getAmount();
    }

    private String formatTimestamp(long timestamp) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                .format(new java.util.Date(timestamp));
    }

    private double spentForBudget(List<DocumentSnapshot> transactions, int categoryId, String period) {
        double spent = 0.0;
        for (DocumentSnapshot transaction : transactions) {
            if (boolValue(transaction.get("isIncome"))) {
                continue;
            }
            if (intValue(transaction.get("categoryId")) != categoryId) {
                continue;
            }
            long timestamp = longValue(transaction.get("timestamp"), 0L);
            if (!matchesBudgetPeriod(timestamp, period)) {
                continue;
            }
            spent += doubleValue(transaction.get("amount"));
        }
        return spent;
    }

    private boolean matchesBudgetPeriod(long timestamp, String period) {
        if (timestamp <= 0L || period == null || period.trim().isEmpty()) {
            return true;
        }
        String clean = period.trim();
        String month = new SimpleDateFormat("yyyy-MM", Locale.US).format(new java.util.Date(timestamp));
        if (clean.matches("\\d{4}-\\d{2}")) {
            return month.equals(clean);
        }
        String year = new SimpleDateFormat("yyyy", Locale.US).format(new java.util.Date(timestamp));
        if (clean.matches("\\d{4}")) {
            return year.equals(clean);
        }
        return true;
    }

    private String firebaseMessage(Exception e, String fallback) {
        Throwable cause = e.getCause() != null ? e.getCause() : e;
        String message = cause.getMessage();
        if (message == null || message.trim().isEmpty()) {
            return fallback;
        }
        String normalized = message.toLowerCase();
        if (normalized.contains("configuration_not_found")) {
            return "Firebase Authentication chưa được cấu hình. Vào Firebase Console > Authentication > Sign-in method và bật Email/Password";
        }
        if (normalized.contains("permission_denied") || normalized.contains("permission denied")) {
            return "Firestore đang chặn quyền truy cập. Hãy kiểm tra Rules của Firebase";
        }
        return message;
    }

    private int intValue(Object value) {
        return intValue(value, 0);
    }

    private int intValue(Object value, int fallback) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return value == null ? fallback : Integer.parseInt(String.valueOf(value));
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private long longValue(Object value, long fallback) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return value == null ? fallback : Long.parseLong(String.valueOf(value));
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private double doubleValue(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return value == null ? 0.0 : Double.parseDouble(String.valueOf(value));
        } catch (Exception ignored) {
            return 0.0;
        }
    }

    private boolean boolValue(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue() == 1;
        }
        return value != null && Boolean.parseBoolean(String.valueOf(value));
    }

    public static class Summary {
        public final double totalBalance;
        public final double totalIncome;
        public final double totalExpense;

        public Summary(double totalBalance, double totalIncome, double totalExpense) {
            this.totalBalance = totalBalance;
            this.totalIncome = totalIncome;
            this.totalExpense = totalExpense;
        }
    }

    public static class ApiResult<T> {
        public final boolean success;
        public final String message;
        public final T data;

        private ApiResult(boolean success, String message, T data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public static <T> ApiResult<T> success(String message, T data) {
            return new ApiResult<>(true, message, data);
        }

        public static <T> ApiResult<T> error(String message) {
            return new ApiResult<>(false, message, null);
        }
    }
}
