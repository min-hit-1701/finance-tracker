import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.SetOptions;
import com.google.cloud.firestore.WriteBatch;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.cloud.FirestoreClient;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ImportBusinessPatch {
    private static final String SERVICE_ACCOUNT_PATH = "serviceAccountKey.json";
    private static final long BASE_ID = 2026060100L;

    public static void main(String[] args) throws Exception {
        initFirebase();

        Firestore db = FirestoreClient.getFirestore();
        Map<Integer, String> uidByUserId = loadImportedUserUids();

        BatchWriter writer = new BatchWriter(db);
        importBusinessCategories(db, writer, uidByUserId);
        importBusinessWallets(db, writer, uidByUserId);
        importBusinessBudgets(db, writer, uidByUserId);
        importBusinessPartners(db, writer, uidByUserId);
        writer.commit();

        System.out.println("DONE. Imported business_data_patch.sql into Firestore business collections.");
    }

    private static void initFirebase() throws Exception {
        try (FileInputStream serviceAccount = new FileInputStream(SERVICE_ACCOUNT_PATH)) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            FirebaseApp.initializeApp(options);
        }
    }

    private static Map<Integer, String> loadImportedUserUids() throws Exception {
        Map<Integer, String> result = new LinkedHashMap<>();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        for (int userId = 1; userId <= 10; userId++) {
            String email = "user" + userId + "@example.com";
            UserRecord user = auth.getUserByEmail(email);
            result.put(userId, user.getUid());
            System.out.println("User " + userId + " -> " + user.getUid());
        }
        return result;
    }

    private static void importBusinessCategories(Firestore db, BatchWriter writer, Map<Integer, String> uidByUserId) throws Exception {
        List<CategoryRow> categories = List.of(
                new CategoryRow(2026060101L, "Marketing", "icon_marketing", "#FF9800"),
                new CategoryRow(2026060102L, "Quảng cáo", "icon_ads", "#E91E63"),
                new CategoryRow(2026060103L, "Sự kiện", "icon_event", "#9C27B0"),
                new CategoryRow(2026060104L, "Chăm sóc khách hàng", "icon_customer_care", "#03A9F4"),
                new CategoryRow(2026060105L, "Văn phòng phẩm", "icon_stationery", "#4CAF50"),
                new CategoryRow(2026060106L, "Lương nhân sự", "icon_staff_salary", "#607D8B")
        );

        for (String uid : uidByUserId.values()) {
            for (CategoryRow category : categories) {
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("id", category.id);
                data.put("name", category.name);
                data.put("iconRes", 0);
                data.put("legacyIconName", category.iconName);
                data.put("colorHex", category.colorHex);
                data.put("isIncome", false);
                data.put("isBusiness", true);
                data.put("importedFromBusinessPatch", true);
                data.put("importedAt", Timestamp.now());
                writer.set(userDoc(db, uid, "business_categories", category.id), data);
            }
        }
        System.out.println("Queued business categories for " + uidByUserId.size() + " users.");
    }

    private static void importBusinessWallets(Firestore db, BatchWriter writer, Map<Integer, String> uidByUserId) throws Exception {
        Object[][] rows = {
                {1, "Quỹ công ty - User 1", 25000000.0, "Cash"}, {1, "Tài khoản doanh nghiệp - User 1", 85000000.0, "Bank"},
                {2, "Quỹ công ty - User 2", 18000000.0, "Cash"}, {2, "Tài khoản doanh nghiệp - User 2", 72000000.0, "Bank"},
                {3, "Quỹ công ty - User 3", 30000000.0, "Cash"}, {3, "Tài khoản doanh nghiệp - User 3", 95000000.0, "Bank"},
                {4, "Quỹ công ty - User 4", 22000000.0, "Cash"}, {4, "Tài khoản doanh nghiệp - User 4", 68000000.0, "Bank"},
                {5, "Quỹ công ty - User 5", 27000000.0, "Cash"}, {5, "Tài khoản doanh nghiệp - User 5", 76000000.0, "Bank"},
                {6, "Quỹ công ty - User 6", 24000000.0, "Cash"}, {6, "Tài khoản doanh nghiệp - User 6", 88000000.0, "Bank"},
                {7, "Quỹ công ty - User 7", 32000000.0, "Cash"}, {7, "Tài khoản doanh nghiệp - User 7", 105000000.0, "Bank"},
                {8, "Quỹ công ty - User 8", 21000000.0, "Cash"}, {8, "Tài khoản doanh nghiệp - User 8", 70000000.0, "Bank"},
                {9, "Quỹ công ty - User 9", 26000000.0, "Cash"}, {9, "Tài khoản doanh nghiệp - User 9", 90000000.0, "Bank"},
                {10, "Quỹ công ty - User 10", 28000000.0, "Cash"}, {10, "Tài khoản doanh nghiệp - User 10", 98000000.0, "Bank"}
        };

        int sequence = 1;
        for (Object[] row : rows) {
            int userId = (Integer) row[0];
            String uid = uidByUserId.get(userId);
            if (uid == null) {
                continue;
            }
            long id = BASE_ID + 100 + sequence++;
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("id", id);
            data.put("name", row[1]);
            data.put("balance", row[2]);
            data.put("type", row[3]);
            data.put("isBusiness", true);
            data.put("updatedAt", System.currentTimeMillis());
            data.put("importedFromBusinessPatch", true);
            data.put("importedAt", Timestamp.now());
            writer.set(userDoc(db, uid, "business_wallets", id), data);
        }
        System.out.println("Queued business wallets: " + rows.length);
    }

    private static void importBusinessBudgets(Firestore db, BatchWriter writer, Map<Integer, String> uidByUserId) throws Exception {
        Object[][] rows = {
                {1, "Marketing", 15000000}, {1, "Quảng cáo", 12000000}, {1, "Sự kiện", 8000000},
                {2, "Marketing", 10000000}, {2, "Chăm sóc khách hàng", 6000000}, {2, "Văn phòng phẩm", 3000000},
                {3, "Marketing", 18000000}, {3, "Quảng cáo", 14000000}, {3, "Lương nhân sự", 25000000},
                {4, "Marketing", 9000000}, {4, "Sự kiện", 7000000}, {4, "Văn phòng phẩm", 2500000},
                {5, "Marketing", 13000000}, {5, "Quảng cáo", 9000000}, {5, "Chăm sóc khách hàng", 5000000},
                {6, "Marketing", 16000000}, {6, "Sự kiện", 10000000}, {6, "Lương nhân sự", 22000000},
                {7, "Marketing", 20000000}, {7, "Quảng cáo", 15000000}, {7, "Chăm sóc khách hàng", 8000000},
                {8, "Marketing", 11000000}, {8, "Sự kiện", 6000000}, {8, "Văn phòng phẩm", 3500000},
                {9, "Marketing", 17000000}, {9, "Quảng cáo", 13000000}, {9, "Lương nhân sự", 26000000},
                {10, "Marketing", 19000000}, {10, "Sự kiện", 9000000}, {10, "Chăm sóc khách hàng", 7000000}
        };

        int sequence = 1;
        for (Object[] row : rows) {
            int userId = (Integer) row[0];
            String uid = uidByUserId.get(userId);
            if (uid == null) {
                continue;
            }
            String categoryName = (String) row[1];
            long id = BASE_ID + 200 + sequence++;
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("id", id);
            data.put("name", "Ngân sách " + categoryName);
            data.put("categoryName", categoryName);
            data.put("limit", row[2]);
            data.put("period", "2026-06");
            data.put("updatedAt", System.currentTimeMillis());
            data.put("importedFromBusinessPatch", true);
            data.put("importedAt", Timestamp.now());
            writer.set(userDoc(db, uid, "business_budgets", id), data);
        }
        System.out.println("Queued business budgets: " + rows.length);
    }

    private static void importBusinessPartners(Firestore db, BatchWriter writer, Map<Integer, String> uidByUserId) throws Exception {
        List<PartnerRow> rows = new ArrayList<>();
        rows.add(new PartnerRow(1, "Công ty Minh Phát Media", "Đối tác marketing", "Nguyễn Minh Phát", "0901000001", "contact@minhphatmedia.vn", "Hà Nội", "Đối tác chạy chiến dịch truyền thông"));
        rows.add(new PartnerRow(1, "Công ty An Khang Supply", "Nhà cung cấp", "Trần An Khang", "0901000002", "sales@ankhangsupply.vn", "Hà Nội", "Cung cấp vật tư văn phòng"));
        rows.add(new PartnerRow(2, "Công ty Blue Ads", "Đối tác quảng cáo", "Lê Thanh Bình", "0902000001", "hello@blueads.vn", "TP.HCM", "Hỗ trợ quảng cáo Facebook/Google"));
        rows.add(new PartnerRow(2, "Công ty Sao Việt Event", "Đối tác sự kiện", "Phạm Sao Việt", "0902000002", "event@saoviet.vn", "Đà Nẵng", "Tổ chức sự kiện khách hàng"));
        rows.add(new PartnerRow(3, "Công ty Hưng Thịnh Logistics", "Đối tác vận chuyển", "Vũ Hưng", "0903000001", "ops@hungthinhlogistics.vn", "Hải Phòng", "Vận chuyển hàng hóa"));
        rows.add(new PartnerRow(3, "Công ty Green Office", "Nhà cung cấp", "Hoàng Mai", "0903000002", "contact@greenoffice.vn", "Hà Nội", "Văn phòng phẩm và thiết bị"));
        rows.add(new PartnerRow(4, "Công ty Alpha Creative", "Đối tác thiết kế", "Đỗ Alpha", "0904000001", "team@alphacreative.vn", "Hà Nội", "Thiết kế ấn phẩm marketing"));
        rows.add(new PartnerRow(4, "Công ty Đại Nam Trading", "Khách hàng doanh nghiệp", "Ngô Đại Nam", "0904000002", "info@dainamtrading.vn", "Bình Dương", "Khách hàng mua hàng định kỳ"));
        rows.add(new PartnerRow(5, "Công ty Lotus Media", "Đối tác marketing", "Bùi Thu Hà", "0905000001", "contact@lotusmedia.vn", "Hà Nội", "Triển khai nội dung truyền thông"));
        rows.add(new PartnerRow(5, "Công ty Đông Á Services", "Đối tác dịch vụ", "Cao Đông Á", "0905000002", "support@dongaservices.vn", "TP.HCM", "Dịch vụ hỗ trợ vận hành"));
        rows.add(new PartnerRow(6, "Công ty Nova Ads", "Đối tác quảng cáo", "Nguyễn Nova", "0906000001", "ads@nova.vn", "TP.HCM", "Quản lý ngân sách quảng cáo"));
        rows.add(new PartnerRow(6, "Công ty Thành Công Event", "Đối tác sự kiện", "Lý Thành Công", "0906000002", "booking@thanhcongevent.vn", "Hà Nội", "Tổ chức hội thảo bán hàng"));
        rows.add(new PartnerRow(7, "Công ty Sunflower CRM", "Đối tác CSKH", "Đặng Hướng Dương", "0907000001", "crm@sunflower.vn", "Đà Nẵng", "Chăm sóc khách hàng doanh nghiệp"));
        rows.add(new PartnerRow(7, "Công ty Mekong Supply", "Nhà cung cấp", "Phan Mekong", "0907000002", "sales@mekongsupply.vn", "Cần Thơ", "Cung cấp vật tư kinh doanh"));
        rows.add(new PartnerRow(8, "Công ty Bright Marketing", "Đối tác marketing", "Trương Minh", "0908000001", "hello@brightmarketing.vn", "Hà Nội", "Lập kế hoạch marketing tháng"));
        rows.add(new PartnerRow(8, "Công ty Nam Long Services", "Đối tác dịch vụ", "Mai Nam Long", "0908000002", "info@namlongservices.vn", "TP.HCM", "Dịch vụ hỗ trợ bán hàng"));
        rows.add(new PartnerRow(9, "Công ty Pixel Ads", "Đối tác quảng cáo", "Nguyễn Pixel", "0909000001", "contact@pixelads.vn", "Hà Nội", "Sản xuất nội dung quảng cáo"));
        rows.add(new PartnerRow(9, "Công ty Phúc Lộc Trading", "Khách hàng doanh nghiệp", "Lê Phúc Lộc", "0909000002", "business@phucloc.vn", "Đồng Nai", "Đối tác mua hàng theo hợp đồng"));
        rows.add(new PartnerRow(10, "Công ty Golden Event", "Đối tác sự kiện", "Trần Kim", "0910000001", "event@goldenevent.vn", "Hà Nội", "Tổ chức sự kiện ra mắt sản phẩm"));
        rows.add(new PartnerRow(10, "Công ty Việt Tín Supply", "Nhà cung cấp", "Võ Việt Tín", "0910000002", "sales@viettin.vn", "TP.HCM", "Cung cấp thiết bị và dịch vụ"));

        int sequence = 1;
        for (PartnerRow row : rows) {
            String uid = uidByUserId.get(row.userId);
            if (uid == null) {
                continue;
            }
            long id = BASE_ID + 300 + sequence++;
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("id", id);
            data.put("name", row.companyName);
            data.put("type", row.relationshipType);
            data.put("note", row.note);
            data.put("contactPerson", row.contactPerson);
            data.put("phone", row.phone);
            data.put("email", row.email);
            data.put("address", row.address);
            data.put("updatedAt", System.currentTimeMillis());
            data.put("importedFromBusinessPatch", true);
            data.put("importedAt", Timestamp.now());
            writer.set(userDoc(db, uid, "business_entities", id), data);
        }
        System.out.println("Queued business entities: " + rows.size());
    }

    private static DocumentReference userDoc(Firestore db, String uid, String collection, Object id) {
        return db.collection("users").document(uid).collection(collection).document(String.valueOf(id));
    }

    private static class CategoryRow {
        final long id;
        final String name;
        final String iconName;
        final String colorHex;

        CategoryRow(long id, String name, String iconName, String colorHex) {
            this.id = id;
            this.name = name;
            this.iconName = iconName;
            this.colorHex = colorHex;
        }
    }

    private static class PartnerRow {
        final int userId;
        final String companyName;
        final String relationshipType;
        final String contactPerson;
        final String phone;
        final String email;
        final String address;
        final String note;

        PartnerRow(int userId, String companyName, String relationshipType, String contactPerson, String phone, String email, String address, String note) {
            this.userId = userId;
            this.companyName = companyName;
            this.relationshipType = relationshipType;
            this.contactPerson = contactPerson;
            this.phone = phone;
            this.email = email;
            this.address = address;
            this.note = note;
        }
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
            batch.commit().get();
            batch = db.batch();
            count = 0;
        }
    }
}
