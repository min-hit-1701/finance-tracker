import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import java.io.FileInputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SeedBusinessIncomeCategories {
    private static final String SERVICE_ACCOUNT_PATH = "serviceAccountKey.json";
    private static final String BUSINESS_INCOME = "BUSINESS_INCOME";

    private static final List<CategoryRow> CATEGORIES = List.of(
            new CategoryRow(2026060201L, "Bán hàng", "#16A34A"),
            new CategoryRow(2026060202L, "Cung cấp dịch vụ", "#0D9488"),
            new CategoryRow(2026060203L, "Hợp đồng dự án", "#2563EB"),
            new CategoryRow(2026060204L, "Hoa hồng", "#7C3AED"),
            new CategoryRow(2026060205L, "Quảng cáo", "#DB2777"),
            new CategoryRow(2026060206L, "Cho thuê tài sản", "#0891B2"),
            new CategoryRow(2026060207L, "Lãi tiền gửi", "#65A30D"),
            new CategoryRow(2026060208L, "Đầu tư tài chính", "#4F46E5"),
            new CategoryRow(2026060209L, "Cổ tức", "#059669"),
            new CategoryRow(2026060210L, "Thu hồi công nợ", "#0284C7"),
            new CategoryRow(2026060211L, "Thanh lý tài sản", "#CA8A04"),
            new CategoryRow(2026060212L, "Hoàn thuế", "#10B981"),
            new CategoryRow(2026060213L, "Góp vốn chủ sở hữu", "#1D4ED8"),
            new CategoryRow(2026060214L, "Thu nhập khác", "#64748B")
    );

    public static void main(String[] args) throws Exception {
        initFirebase();
        Firestore db = FirestoreClient.getFirestore();

        int userCount = 0;
        int writeCount = 0;
        for (QueryDocumentSnapshot user : db.collection("users").get().get().getDocuments()) {
            userCount++;
            for (CategoryRow category : CATEGORIES) {
                DocumentReference doc = user.getReference()
                        .collection("business_categories")
                        .document(String.valueOf(category.id));
                if (doc.get().get().exists()) {
                    continue;
                }
                doc.set(category.toMap()).get();
                writeCount++;
            }
        }

        System.out.println("Seeded BUSINESS_INCOME categories for users: " + userCount);
        System.out.println("New category documents written: " + writeCount);
    }

    private static void initFirebase() throws Exception {
        try (FileInputStream serviceAccount = new FileInputStream(SERVICE_ACCOUNT_PATH)) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            FirebaseApp.initializeApp(options);
        }
    }

    private static class CategoryRow {
        final long id;
        final String name;
        final String colorHex;

        CategoryRow(long id, String name, String colorHex) {
            this.id = id;
            this.name = name;
            this.colorHex = colorHex;
        }

        Map<String, Object> toMap() {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("id", id);
            data.put("name", name);
            data.put("iconRes", 0);
            data.put("colorHex", colorHex);
            data.put("isIncome", true);
            data.put("isBusiness", true);
            data.put("type", BUSINESS_INCOME);
            data.put("seededFrom", "SeedBusinessIncomeCategories");
            data.put("seededAt", Timestamp.now());
            data.put("updatedAt", System.currentTimeMillis());
            return data;
        }
    }
}
