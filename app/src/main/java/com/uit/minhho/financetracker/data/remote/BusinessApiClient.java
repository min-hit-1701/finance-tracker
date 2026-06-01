package com.uit.minhho.financetracker.data.remote;

import android.content.Context;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.uit.minhho.financetracker.model.business.BusinessEntity;
import com.uit.minhho.financetracker.model.business.BusinessTransaction;
import com.uit.minhho.financetracker.model.business.BusinessWallet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BusinessApiClient {
    private static final String WALLETS = "business_wallets";
    private static final String TRANSACTIONS = "business_transactions";
    private static final String ENTITIES = "business_entities";
    private static final String PAYMENTS = "business_payments";

    private final Context context;

    public BusinessApiClient(Context context) {
        this.context = context.getApplicationContext();
    }

    public ApiResult createWallet(String name, double balance, String type) {
        try {
            FirebaseSession.Session session = requireSession();
            if (!session.valid) {
                return ApiResult.error(session.errorMessage);
            }

            int id = FirebaseSession.nextId();
            Map<String, Object> data = new HashMap<>();
            data.put("id", id);
            data.put("name", name);
            data.put("balance", balance);
            data.put("type", type);
            data.put("updatedAt", System.currentTimeMillis());

            Tasks.await(collection(session.uid, WALLETS).document(String.valueOf(id)).set(data));
            return ApiResult.success("Đã thêm ví doanh nghiệp vào Firebase");
        } catch (Exception e) {
            return ApiResult.error(firebaseMessage(e, "Không thể thêm ví doanh nghiệp"));
        }
    }

    public List<BusinessWallet> getWallets() {
        List<BusinessWallet> result = new ArrayList<>();
        try {
            FirebaseSession.Session session = requireSession();
            if (!session.valid) {
                return result;
            }

            QuerySnapshot snapshot = Tasks.await(collection(session.uid, WALLETS)
                    .orderBy("id", Query.Direction.DESCENDING)
                    .get());

            for (DocumentSnapshot row : snapshot.getDocuments()) {
                String type = row.getString("type");
                int id = intValue(row.get("id"), FirebaseSession.positiveHash(row.getId()));
                result.add(new BusinessWallet(
                        id,
                        text(row, "name"),
                        formatMoney(doubleValue(row.get("balance"))),
                        type == null || type.trim().isEmpty() ? "Doanh nghiệp" : type
                ));
            }
        } catch (Exception ignored) {
        }
        return result;
    }

    public List<BusinessTransaction> getTransactions() {
        List<BusinessTransaction> result = new ArrayList<>();
        try {
            FirebaseSession.Session session = requireSession();
            if (!session.valid) {
                return result;
            }

            QuerySnapshot snapshot = Tasks.await(collection(session.uid, TRANSACTIONS)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get());

            for (DocumentSnapshot row : snapshot.getDocuments()) {
                boolean income = boolValue(row.get("isIncome"));
                double amount = doubleValue(row.get("amount"));
                String note = text(row, "note");
                String displayTime = text(row, "displayTime");
                result.add(new BusinessTransaction(
                        intValue(row.get("id"), intValue(row.getId(), FirebaseSession.positiveHash(row.getId()))),
                        note.isEmpty() ? "Giao dịch doanh nghiệp" : note,
                        displayTime,
                        (income ? "+" : "-") + formatMoney(amount),
                        income,
                        amount,
                        longValue(row.get("timestamp"), 0L)
                ));
            }
        } catch (Exception ignored) {
        }
        return result;
    }

    public List<ReportTransaction> getReportTransactions() {
        List<ReportTransaction> result = new ArrayList<>();
        try {
            FirebaseSession.Session session = requireSession();
            if (!session.valid) {
                return result;
            }

            QuerySnapshot snapshot = Tasks.await(collection(session.uid, TRANSACTIONS).get());
            for (DocumentSnapshot row : snapshot.getDocuments()) {
                String label = text(row, "categoryName");
                if (label.isEmpty()) {
                    label = text(row, "note");
                }
                result.add(new ReportTransaction(
                        doubleValue(row.get("amount")),
                        boolValue(row.get("isIncome")),
                        label,
                        longValue(row.get("timestamp"), 0L)
                ));
            }
        } catch (Exception ignored) {
        }
        return result;
    }

    public ApiResult<Summary> getSummary() {
        try {
            FirebaseSession.Session session = requireSession();
            if (!session.valid) {
                return ApiResult.error(session.errorMessage);
            }

            double totalIncome = 0.0;
            double totalExpense = 0.0;
            QuerySnapshot transactions = Tasks.await(collection(session.uid, TRANSACTIONS).get());
            for (DocumentSnapshot row : transactions.getDocuments()) {
                if (boolValue(row.get("isIncome"))) {
                    totalIncome += doubleValue(row.get("amount"));
                } else {
                    totalExpense += doubleValue(row.get("amount"));
                }
            }

            double totalBalance = totalIncome - totalExpense;
            return ApiResult.success("Summary loaded", new Summary(totalBalance, totalIncome, totalExpense));
        } catch (Exception e) {
            return ApiResult.error(firebaseMessage(e, "Không thể tải tổng quan doanh nghiệp"));
        }
    }

    public ApiResult createBusinessEntity(String name, String type, String note) {
        try {
            FirebaseSession.Session session = requireSession();
            if (!session.valid) {
                return ApiResult.error(session.errorMessage);
            }

            int id = FirebaseSession.nextId();
            Map<String, Object> data = new HashMap<>();
            data.put("id", id);
            data.put("name", name);
            data.put("type", type);
            data.put("note", note);
            data.put("updatedAt", System.currentTimeMillis());

            Tasks.await(collection(session.uid, ENTITIES).document(String.valueOf(id)).set(data));
            return ApiResult.success("Đã lưu doanh nghiệp vào Firebase");
        } catch (Exception e) {
            return ApiResult.error(firebaseMessage(e, "Không thể lưu doanh nghiệp"));
        }
    }

    public List<BusinessEntity> getBusinessEntities() {
        List<BusinessEntity> result = new ArrayList<>();
        try {
            FirebaseSession.Session session = requireSession();
            if (!session.valid) {
                return result;
            }

            QuerySnapshot snapshot = Tasks.await(collection(session.uid, ENTITIES)
                    .orderBy("id", Query.Direction.DESCENDING)
                    .get());
            for (DocumentSnapshot row : snapshot.getDocuments()) {
                int id = intValue(row.get("id"), FirebaseSession.positiveHash(row.getId()));
                result.add(new BusinessEntity(
                        id,
                        text(row, "name"),
                        text(row, "type"),
                        text(row, "note")
                ));
            }
        } catch (Exception ignored) {
        }
        return result;
    }

    public ApiResult createPayment(String receiver, String account, double amount, String note) {
        try {
            FirebaseSession.Session session = requireSession();
            if (!session.valid) {
                return ApiResult.error(session.errorMessage);
            }

            SimpleWallet wallet = firstWallet(session.uid);
            if (wallet == null) {
                return ApiResult.error("Vui lòng tạo ví doanh nghiệp trước khi gửi thanh toán");
            }

            if (wallet.balance < amount) {
                return ApiResult.error("Số dư ví không đủ để thực hiện thanh toán này (Hiện có: " + formatMoney(wallet.balance) + ")");
            }

            long now = System.currentTimeMillis();
            int paymentId = FirebaseSession.nextId();
            Map<String, Object> payment = new HashMap<>();
            payment.put("id", paymentId);
            payment.put("receiver", receiver);
            payment.put("account", account);
            payment.put("amount", amount);
            payment.put("note", note);
            payment.put("timestamp", now);
            payment.put("displayTime", formatTimestamp(now));
            Tasks.await(collection(session.uid, PAYMENTS).document(String.valueOf(paymentId)).set(payment));

            int transactionId = FirebaseSession.nextId();
            Map<String, Object> transaction = transactionData(
                    transactionId,
                    amount,
                    "Thanh toán doanh nghiệp",
                    buildNote(receiver, note),
                    now,
                    false,
                    wallet.id
            );
            Tasks.await(collection(session.uid, TRANSACTIONS).document(String.valueOf(transactionId)).set(transaction));
            updateWalletBalance(session.uid, wallet.id, -amount);

            return ApiResult.success("Đã tạo lệnh chi thành công");
        } catch (Exception e) {
            return ApiResult.error(firebaseMessage(e, "Không thể tạo lệnh chi"));
        }
    }

    public ApiResult createTransaction(
            double amount,
            String partner,
            String categoryName,
            String timestamp,
            String note,
            boolean isIncome,
            int walletId
    ) {
        try {
            FirebaseSession.Session session = requireSession();
            if (!session.valid) {
                return ApiResult.error(session.errorMessage);
            }

            if (walletId <= 0) {
                return ApiResult.error("Vui lòng chọn ví doanh nghiệp");
            }

            if (!isIncome) {
                DocumentSnapshot walletSnap = Tasks.await(collection(session.uid, WALLETS).document(String.valueOf(walletId)).get());
                if (walletSnap.exists()) {
                    double balance = doubleValue(walletSnap.get("balance"));
                    if (balance < amount) {
                        return ApiResult.error("Số dư ví không đủ để thực hiện giao dịch này (Hiện có: " + formatMoney(balance) + ")");
                    }
                }
            }

            long time = parseTimestamp(timestamp);
            int id = FirebaseSession.nextId();
            Map<String, Object> data = transactionData(
                    id,
                    amount,
                    categoryName,
                    buildNote(partner, note),
                    time,
                    isIncome,
                    walletId
            );
            Tasks.await(collection(session.uid, TRANSACTIONS).document(String.valueOf(id)).set(data));
            updateWalletBalance(session.uid, walletId, isIncome ? amount : -amount);
            return ApiResult.success("Thêm giao dịch thành công");
        } catch (Exception e) {
            return ApiResult.error(firebaseMessage(e, "Không thể thêm giao dịch"));
        }
    }

    public ApiResult<Void> deleteTransaction(BusinessTransaction transaction) {
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

            boolean income = boolValue(snapshot.get("isIncome"));
            double amount = doubleValue(snapshot.get("amount"));
            int walletId = intValue(snapshot.get("walletId"), 0);

            Tasks.await(transactions.document(String.valueOf(transaction.getId())).delete());
            updateWalletBalance(session.uid, walletId, income ? -amount : amount);
            return ApiResult.success("Đã xóa giao dịch");
        } catch (Exception e) {
            return ApiResult.error(firebaseMessage(e, "Không thể xóa giao dịch"));
        }
    }

    private Map<String, Object> transactionData(
            int id,
            double amount,
            String categoryName,
            String note,
            long timestamp,
            boolean isIncome,
            int walletId
    ) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("amount", amount);
        data.put("categoryName", categoryName);
        data.put("note", note);
        data.put("timestamp", timestamp);
        data.put("displayTime", formatTimestamp(timestamp));
        data.put("isIncome", isIncome);
        data.put("walletId", walletId);
        data.put("updatedAt", System.currentTimeMillis());
        return data;
    }

    private FirebaseSession.Session requireSession() {
        return FirebaseSession.require(context);
    }

    private CollectionReference collection(String uid, String name) {
        return FirebaseSession.collection(uid, name);
    }

    private SimpleWallet firstWallet(String uid) throws Exception {
        QuerySnapshot snapshot = Tasks.await(collection(uid, WALLETS).limit(1).get());
        if (snapshot.isEmpty()) {
            return null;
        }
        DocumentSnapshot first = snapshot.getDocuments().get(0);
        return new SimpleWallet(
                intValue(first.get("id"), FirebaseSession.positiveHash(first.getId())),
                text(first, "name"),
                doubleValue(first.get("balance"))
        );
    }

    private void updateWalletBalance(String uid, int walletId, double delta) throws Exception {
        DocumentSnapshot snapshot = Tasks.await(collection(uid, WALLETS).document(String.valueOf(walletId)).get());
        if (!snapshot.exists()) {
            return;
        }
        double current = doubleValue(snapshot.get("balance"));
        Tasks.await(collection(uid, WALLETS).document(String.valueOf(walletId)).update("balance", current + delta));
    }

    private String buildNote(String partner, String note) {
        String cleanPartner = partner == null ? "" : partner.trim();
        String cleanNote = note == null ? "" : note.trim();
        if (cleanNote.isEmpty()) {
            return cleanPartner;
        }
        if (cleanPartner.isEmpty()) {
            return cleanNote;
        }
        return cleanPartner + " - " + cleanNote;
    }

    private long parseTimestamp(String value) {
        if (value == null || value.trim().isEmpty()) {
            return System.currentTimeMillis();
        }
        try {
            Date parsed = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).parse(value);
            return parsed == null ? System.currentTimeMillis() : parsed.getTime();
        } catch (Exception ignored) {
            return System.currentTimeMillis();
        }
    }

    private String formatTimestamp(long timestamp) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date(timestamp));
    }

    private String formatMoney(double amount) {
        return String.format(Locale.US, "%,.0f đ", amount);
    }

    private String text(DocumentSnapshot row, String key) {
        String value = row.getString(key);
        return value == null ? "" : value;
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

    private boolean boolValue(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue() == 1;
        }
        return value != null && Boolean.parseBoolean(String.valueOf(value));
    }

    private static class SimpleWallet {
        final int id;
        final String name;
        final double balance;

        SimpleWallet(int id, String name, double balance) {
            this.id = id;
            this.name = name;
            this.balance = balance;
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

        public static <T> ApiResult<T> success(String message) {
            return new ApiResult<>(true, message, null);
        }

        public static <T> ApiResult<T> success(String message, T data) {
            return new ApiResult<>(true, message, data);
        }

        public static <T> ApiResult<T> error(String message) {
            return new ApiResult<>(false, message, null);
        }
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

    public static class ReportTransaction {
        public final double amount;
        public final boolean income;
        public final String note;
        public final long timestamp;

        public ReportTransaction(double amount, boolean income, String note, long timestamp) {
            this.amount = amount;
            this.income = income;
            this.note = note;
            this.timestamp = timestamp;
        }
    }
}
