package com.uit.minhho.financetracker.data.remote;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

final class FirebaseSession {
    private FirebaseSession() {
    }

    static Session require(Context context) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user == null ? "" : user.getUid();

        if (uid.isEmpty() && context != null) {
            SharedPreferences preferences = context.getApplicationContext()
                    .getSharedPreferences("auth_session", Context.MODE_PRIVATE);
            uid = preferences.getString("uid", preferences.getString("token", ""));
        }

        if (uid == null || uid.trim().isEmpty()) {
            return Session.invalid("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại");
        }
        return Session.valid(uid);
    }

    static CollectionReference collection(String uid, String name) {
        return FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection(name);
    }

    static int nextId() {
        long now = System.currentTimeMillis();
        int id = (int) (now & 0x7fffffff);
        return id == 0 ? 1 : id;
    }

    static int positiveHash(String value) {
        int hash = value == null ? 0 : Math.abs(value.hashCode());
        return hash == 0 ? nextId() : hash;
    }

    static class Session {
        final boolean valid;
        final String uid;
        final String errorMessage;

        private Session(boolean valid, String uid, String errorMessage) {
            this.valid = valid;
            this.uid = uid;
            this.errorMessage = errorMessage;
        }

        static Session valid(String uid) {
            return new Session(true, uid, "");
        }

        static Session invalid(String message) {
            return new Session(false, "", message);
        }
    }
}
