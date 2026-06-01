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

public class SeedPersonalIncomeCategories {
    private static final String SERVICE_ACCOUNT_PATH = "serviceAccountKey.json";
    private static final String PERSONAL_INCOME = "PERSONAL_INCOME";

    private static final List<CategoryRow> CATEGORIES = List.of(
            new CategoryRow(2026060301L, "Lương", "#10B981"),
            new CategoryRow(2026060302L, "Thưởng", "#22C55E"),
            new CategoryRow(2026060303L, "Làm thêm", "#14B8A6"),
            new CategoryRow(2026060304L, "Freelance", "#06B6D4"),
            new CategoryRow(2026060305L, "Hoa hồng cá nhân", "#8B5CF6"),
            new CategoryRow(2026060306L, "Lãi tiết kiệm", "#65A30D"),
            new CategoryRow(2026060307L, "Cổ tức", "#059669"),
            new CategoryRow(2026060308L, "Lợi nhuận đầu tư", "#4F46E5")
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
                        .collection("personal_categories")
                        .document(String.valueOf(category.id));
                if (doc.get().get().exists()) {
                    continue;
                }
                doc.set(category.toMap()).get();
                writeCount++;
            }
        }

        System.out.println("Seeded PERSONAL_INCOME categories for users: " + userCount);
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
            data.put("isBusiness", false);
            data.put("type", PERSONAL_INCOME);
            data.put("seededFrom", "SeedPersonalIncomeCategories");
            data.put("seededAt", Timestamp.now());
            data.put("updatedAt", System.currentTimeMillis());
            return data;
        }
    }
}
