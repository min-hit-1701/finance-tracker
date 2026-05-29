import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.SetOptions;
import com.google.cloud.firestore.WriteBatch;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.cloud.FirestoreClient;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImportFirestore {
    private static final String SERVICE_ACCOUNT_PATH = "serviceAccountKey.json";
    private static final String SQL_PATH = "DATABASE.sql";
    private static final String DEFAULT_IMPORTED_PASSWORD = "123456";

    private static final Map<Long, String> userUidByMysqlId = new HashMap<>();
    private static final Map<Long, String> categoryNameById = new HashMap<>();

    public static void main(String[] args) throws Exception {
        initFirebase();

        Firestore db = FirestoreClient.getFirestore();
        Map<String, List<Map<String, Object>>> tables = parseInsertStatements(readFile(SQL_PATH));

        importUsers(db, rows(tables, "users"));
        indexCategoryNames(rows(tables, "categories"));
        importCategories(db, rows(tables, "categories"));
        importWallets(db, rows(tables, "wallets"));
        importBudgets(db, rows(tables, "budgets"));
        importTransactions(db, rows(tables, "transactions"));

        System.out.println("DONE. Imported SQL data into Firestore structure used by the Android app.");
        System.out.println("Imported users can log in with their SQL email and password 123456.");
    }

    private static void initFirebase() throws Exception {
        try (FileInputStream serviceAccount = new FileInputStream(SERVICE_ACCOUNT_PATH)) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);
        }
    }

    private static void importUsers(Firestore db, List<Map<String, Object>> users) throws Exception {
        for (Map<String, Object> row : users) {
            long oldId = longValue(row.get("id"));
            String fullName = stringValue(row.get("fullName"));
            String email = stringValue(row.get("email"));
            if (email.isEmpty()) {
                System.out.println("Skip user without email, old id: " + oldId);
                continue;
            }

            UserRecord authUser = findOrCreateAuthUser(email, fullName);
            userUidByMysqlId.put(oldId, authUser.getUid());

            Map<String, Object> profile = new LinkedHashMap<>();
            profile.put("fullName", fullName);
            profile.put("email", email);
            profile.put("oldMysqlId", oldId);
            profile.put("importedFromMysql", true);
            profile.put("importedAt", Timestamp.now());

            db.collection("users").document(authUser.getUid()).set(profile, SetOptions.merge()).get();
            System.out.println("User " + oldId + " -> " + authUser.getUid() + " (" + email + ")");
        }
    }

    private static UserRecord findOrCreateAuthUser(String email, String fullName) throws Exception {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        try {
            return auth.getUserByEmail(email);
        } catch (FirebaseAuthException e) {
            if (!"USER_NOT_FOUND".equals(e.getAuthErrorCode().name())) {
                throw e;
            }
        }

        UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                .setEmail(email)
                .setPassword(DEFAULT_IMPORTED_PASSWORD)
                .setDisplayName(fullName)
                .setEmailVerified(false)
                .setDisabled(false);

        return auth.createUser(request);
    }

    private static void indexCategoryNames(List<Map<String, Object>> categories) {
        for (Map<String, Object> row : categories) {
            categoryNameById.put(longValue(row.get("id")), stringValue(row.get("name")));
        }
    }

    private static void importCategories(Firestore db, List<Map<String, Object>> rows) throws Exception {
        List<Map<String, Object>> globalRows = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            if (row.get("userId") == null) {
                globalRows.add(row);
            } else {
                writeCategory(db, uidFor(row.get("userId")), row);
            }
        }

        for (String uid : userUidByMysqlId.values()) {
            for (Map<String, Object> row : globalRows) {
                writeCategory(db, uid, row);
            }
        }
        System.out.println("Imported categories: " + rows.size());
    }

    private static void writeCategory(Firestore db, String uid, Map<String, Object> row) throws Exception {
        if (uid == null) {
            return;
        }
        boolean isBusiness = boolValue(row.get("isBusiness"));
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", intValue(row.get("id")));
        data.put("name", stringValue(row.get("name")));
        data.put("iconRes", 0);
        data.put("legacyIconName", stringValue(row.get("iconRes")));
        data.put("colorHex", stringValue(row.get("colorHex"), "#F44336"));
        data.put("isIncome", boolValue(row.get("isIncome")));
        data.put("isBusiness", isBusiness);
        data.put("oldMysqlId", longValue(row.get("id")));
        data.put("importedFromMysql", true);
        data.put("importedAt", Timestamp.now());

        String collection = isBusiness ? "business_categories" : "personal_categories";
        db.collection("users").document(uid)
                .collection(collection)
                .document(String.valueOf(data.get("id")))
                .set(data, SetOptions.merge())
                .get();
    }

    private static void importWallets(Firestore db, List<Map<String, Object>> rows) throws Exception {
        BatchWriter writer = new BatchWriter(db);
        for (Map<String, Object> row : rows) {
            String uid = uidFor(row.get("userId"));
            if (uid == null) {
                continue;
            }
            boolean isBusiness = boolValue(row.get("isBusiness"));
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("id", intValue(row.get("id")));
            data.put("name", stringValue(row.get("name")));
            data.put("balance", doubleValue(row.get("balance")));
            data.put("type", stringValue(row.get("type"), "Cash"));
            data.put("isBusiness", isBusiness);
            data.put("oldMysqlId", longValue(row.get("id")));
            data.put("updatedAt", System.currentTimeMillis());
            data.put("importedFromMysql", true);
            data.put("importedAt", Timestamp.now());

            String collection = isBusiness ? "business_wallets" : "personal_wallets";
            writer.set(userSubcollectionDoc(db, uid, collection, data.get("id")), data);
        }
        writer.commit();
        System.out.println("Imported wallets: " + rows.size());
    }

    private static void importBudgets(Firestore db, List<Map<String, Object>> rows) throws Exception {
        BatchWriter writer = new BatchWriter(db);
        for (Map<String, Object> row : rows) {
            String uid = uidFor(row.get("userId"));
            if (uid == null) {
                continue;
            }
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("id", intValue(row.get("id")));
            data.put("categoryId", intValue(row.get("categoryId")));
            data.put("limitAmount", doubleValue(row.get("amount")));
            data.put("spentAmount", 0.0);
            data.put("period", stringValue(row.get("month")));
            data.put("oldMysqlId", longValue(row.get("id")));
            data.put("updatedAt", System.currentTimeMillis());
            data.put("importedFromMysql", true);
            data.put("importedAt", Timestamp.now());

            writer.set(userSubcollectionDoc(db, uid, "personal_budgets", data.get("id")), data);
        }
        writer.commit();
        System.out.println("Imported budgets: " + rows.size());
    }

    private static void importTransactions(Firestore db, List<Map<String, Object>> rows) throws Exception {
        BatchWriter writer = new BatchWriter(db);
        for (Map<String, Object> row : rows) {
            String uid = uidFor(row.get("userId"));
            if (uid == null) {
                continue;
            }
            boolean isBusiness = boolValue(row.get("isBusiness"));
            long time = dateMillis(row.get("timestamp"));
            int categoryId = intValue(row.get("categoryId"));

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("id", intValue(row.get("id")));
            data.put("amount", doubleValue(row.get("amount")));
            data.put("timestamp", time);
            data.put("displayTime", formatTimestamp(time));
            data.put("note", stringValue(row.get("note")));
            data.put("categoryId", categoryId);
            data.put("categoryName", categoryNameById.getOrDefault((long) categoryId, ""));
            data.put("walletId", intValue(row.get("walletId")));
            data.put("isIncome", boolValue(row.get("isIncome")));
            data.put("isBusiness", isBusiness);
            data.put("iconRes", 0);
            data.put("oldMysqlId", longValue(row.get("id")));
            data.put("updatedAt", System.currentTimeMillis());
            data.put("importedFromMysql", true);
            data.put("importedAt", Timestamp.now());

            String collection = isBusiness ? "business_transactions" : "personal_transactions";
            writer.set(userSubcollectionDoc(db, uid, collection, data.get("id")), data);
        }
        writer.commit();
        System.out.println("Imported transactions: " + rows.size());
    }

    private static DocumentReference userSubcollectionDoc(Firestore db, String uid, String collection, Object id) {
        return db.collection("users").document(uid)
                .collection(collection)
                .document(String.valueOf(id));
    }

    private static String uidFor(Object userIdValue) {
        String uid = userUidByMysqlId.get(longValue(userIdValue));
        if (uid == null) {
            System.out.println("Skip row for missing MySQL userId: " + userIdValue);
        }
        return uid;
    }

    private static List<Map<String, Object>> rows(Map<String, List<Map<String, Object>>> tables, String table) {
        return tables.getOrDefault(table, new ArrayList<>());
    }

    private static String readFile(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }

    private static Map<String, List<Map<String, Object>>> parseInsertStatements(String sql) {
        Map<String, List<Map<String, Object>>> result = new LinkedHashMap<>();
        Pattern insertPattern = Pattern.compile(
                "INSERT\\s+INTO\\s+`?(\\w+)`?\\s*\\((.*?)\\)\\s*VALUES\\s*(.*?);",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        );

        Matcher matcher = insertPattern.matcher(sql);
        while (matcher.find()) {
            String tableName = matcher.group(1);
            List<String> columns = parseColumns(matcher.group(2));
            List<String> rawRows = splitRows(matcher.group(3));
            result.putIfAbsent(tableName, new ArrayList<>());

            for (String rawRow : rawRows) {
                List<String> values = splitValues(rawRow);
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 0; i < columns.size() && i < values.size(); i++) {
                    row.put(columns.get(i), parseValue(values.get(i)));
                }
                result.get(tableName).add(row);
            }
        }
        return result;
    }

    private static List<String> parseColumns(String columnsRaw) {
        List<String> columns = new ArrayList<>();
        for (String column : columnsRaw.split(",")) {
            columns.add(column.replace("`", "").trim());
        }
        return columns;
    }

    private static List<String> splitRows(String valuesRaw) {
        List<String> rows = new ArrayList<>();
        boolean inString = false;
        boolean escaping = false;
        int start = -1;
        int depth = 0;

        for (int i = 0; i < valuesRaw.length(); i++) {
            char c = valuesRaw.charAt(i);

            if (inString) {
                if (escaping) {
                    escaping = false;
                } else if (c == '\\') {
                    escaping = true;
                } else if (c == '\'') {
                    inString = false;
                }
                continue;
            }

            if (c == '\'') {
                inString = true;
            } else if (c == '(') {
                if (depth == 0) {
                    start = i + 1;
                }
                depth++;
            } else if (c == ')') {
                depth--;
                if (depth == 0 && start >= 0) {
                    rows.add(valuesRaw.substring(start, i));
                }
            }
        }

        return rows;
    }

    private static List<String> splitValues(String rowRaw) {
        List<String> values = new ArrayList<>();
        boolean inString = false;
        boolean escaping = false;
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < rowRaw.length(); i++) {
            char c = rowRaw.charAt(i);

            if (inString) {
                current.append(c);
                if (escaping) {
                    escaping = false;
                } else if (c == '\\') {
                    escaping = true;
                } else if (c == '\'') {
                    inString = false;
                }
                continue;
            }

            if (c == '\'') {
                inString = true;
                current.append(c);
            } else if (c == ',') {
                values.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }

        values.add(current.toString().trim());
        return values;
    }

    private static Object parseValue(String value) {
        String clean = value.trim();
        if (clean.equalsIgnoreCase("NULL")) {
            return null;
        }
        if (clean.startsWith("'") && clean.endsWith("'")) {
            return clean.substring(1, clean.length() - 1)
                    .replace("\\'", "'")
                    .replace("\\n", "\n")
                    .replace("\\r", "\r")
                    .replace("\\t", "\t")
                    .replace("\\\\", "\\");
        }
        try {
            if (clean.contains(".")) {
                return Double.parseDouble(clean);
            }
            return Long.parseLong(clean);
        } catch (NumberFormatException e) {
            return clean;
        }
    }

    private static long dateMillis(Object value) {
        String text = stringValue(value);
        if (text.isEmpty()) {
            return System.currentTimeMillis();
        }
        try {
            Date parsed = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).parse(text);
            return parsed == null ? System.currentTimeMillis() : parsed.getTime();
        } catch (Exception ignored) {
            return System.currentTimeMillis();
        }
    }

    private static String formatTimestamp(long timestamp) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date(timestamp));
    }

    private static boolean boolValue(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return longValue(value) == 1L;
    }

    private static int intValue(Object value) {
        return (int) longValue(value);
    }

    private static long longValue(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value == null) {
            return 0L;
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ignored) {
            return 0L;
        }
    }

    private static double doubleValue(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value == null) {
            return 0.0;
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException ignored) {
            return 0.0;
        }
    }

    private static String stringValue(Object value) {
        return stringValue(value, "");
    }

    private static String stringValue(Object value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String text = String.valueOf(value);
        return text.trim().isEmpty() ? fallback : text;
    }

    private static class BatchWriter {
        private final Firestore db;
        private WriteBatch batch;
        private int count;

        BatchWriter(Firestore db) {
            this.db = db;
            this.batch = db.batch();
        }

        void set(DocumentReference ref, Map<String, Object> data) throws Exception {
            batch.set(ref, data, SetOptions.merge());
            count++;
            if (count >= 450) {
                commit();
            }
        }

        void commit() throws Exception {
            if (count == 0) {
                return;
            }
            ApiFuture<List<com.google.cloud.firestore.WriteResult>> future = batch.commit();
            future.get();
            batch = db.batch();
            count = 0;
        }
    }
}
