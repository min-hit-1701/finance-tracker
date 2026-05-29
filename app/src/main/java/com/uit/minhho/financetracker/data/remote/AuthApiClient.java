package com.uit.minhho.financetracker.data.remote;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AuthApiClient {
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public AuthResult login(String email, String password) {
        try {
            FirebaseUser user = Tasks.await(auth.signInWithEmailAndPassword(email, password)).getUser();
            if (user == null) {
                return AuthResult.error("Không lấy được thông tin tài khoản. Vui lòng đăng nhập lại");
            }
            return AuthResult.success("Đăng nhập thành công", user.getUid());
        } catch (Exception e) {
            return AuthResult.error(firebaseMessage(e, "Đăng nhập thất bại"));
        }
    }

    public AuthResult register(String fullName, String email, String password) {
        try {
            FirebaseUser user = Tasks.await(auth.createUserWithEmailAndPassword(email, password)).getUser();
            if (user == null) {
                return AuthResult.error("Không tạo được tài khoản. Vui lòng thử lại");
            }

            Map<String, Object> profile = new HashMap<>();
            profile.put("fullName", fullName);
            profile.put("email", email);
            profile.put("createdAt", System.currentTimeMillis());
            Tasks.await(firestore.collection("users").document(user.getUid()).set(profile));

            return AuthResult.success("Đăng ký thành công", user.getUid());
        } catch (Exception e) {
            return AuthResult.error(firebaseMessage(e, "Đăng ký thất bại"));
        }
    }

    private String firebaseMessage(Exception e, String fallback) {
        Throwable cause = e.getCause() != null ? e.getCause() : e;
        String message = cause.getMessage();
        if (message == null || message.trim().isEmpty()) {
            return fallback;
        }
        String normalized = message.toLowerCase();
        if (normalized.contains("configuration_not_found")) {
            return "Firebase Authentication chưa được cấu hình. Vào Firebase Console > Authentication > Sign-in method và bật Email/Password cho project này";
        }
        if (normalized.contains("invalid_login_credentials")
                || normalized.contains("auth credential is incorrect")
                || normalized.contains("malformed or has expired")
                || normalized.contains("wrong-password")
                || normalized.contains("user-not-found")) {
            return "Email hoặc mật khẩu không đúng. Nếu đây là tài khoản cũ từ XAMPP/MySQL, bạn cần đăng ký lại trên Firebase";
        }
        if (normalized.contains("badly formatted")
                || normalized.contains("invalid-email")) {
            return "Email không hợp lệ. Vui lòng nhập đúng định dạng email";
        }
        if (normalized.contains("email-already-in-use")) {
            return "Email này đã được đăng ký. Vui lòng đăng nhập hoặc dùng email khác";
        }
        if (normalized.contains("weak-password")) {
            return "Mật khẩu quá yếu. Vui lòng dùng mật khẩu ít nhất 6 ký tự";
        }
        if (normalized.contains("permission_denied") || normalized.contains("permission denied")) {
            return "Firestore đang chặn quyền truy cập. Hãy kiểm tra Rules của Firebase";
        }
        return message;
    }

    public static class AuthResult {
        public final boolean success;
        public final String message;
        public final int userId;
        public final String token;
        public final String uid;

        private AuthResult(boolean success, String message) {
            this(success, message, 0, "", "");
        }

        private AuthResult(boolean success, String message, int userId, String token, String uid) {
            this.success = success;
            this.message = message;
            this.userId = userId;
            this.token = token;
            this.uid = uid;
        }

        public static AuthResult success(String message, String uid) {
            int userId = Math.abs(uid.hashCode());
            if (userId == 0) {
                userId = 1;
            }
            return new AuthResult(true, message, userId, uid, uid);
        }

        public static AuthResult error(String message) {
            return new AuthResult(false, message);
        }
    }
}
