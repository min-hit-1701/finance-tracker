package com.uit.minhho.financetracker.util;

import android.util.Patterns;
import java.util.regex.Pattern;

public class ValidationUtils {
    // Regex cho số điện thoại Việt Nam: Bắt đầu bằng 0 hoặc +84, theo sau là 9 chữ số
    private static final String PHONE_REGEX = "^(0|\\+84)(\\d{9,10})$";
    private static final Pattern PHONE_PATTERN = Pattern.compile(PHONE_REGEX);

    public static boolean isValidEmail(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Kiểm tra xem chuỗi nhập vào là Email hợp lệ HOẶC Số điện thoại hợp lệ
     */
    public static boolean isValidEmailOrPhone(String input) {
        return isValidEmail(input) || isValidPhone(input);
    }
}
