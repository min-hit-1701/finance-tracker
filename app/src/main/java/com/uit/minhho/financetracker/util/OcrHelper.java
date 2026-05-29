package com.uit.minhho.financetracker.util;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OcrHelper {
    private static final String TAG = "OcrHelper";

    public interface OcrCallback {
        void onResult(String amount, String categorySuggestion, String dateSuggestion, String fullText);
    }

    public static void scanReceipt(Bitmap bitmap, OcrCallback callback) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        recognizer.process(image)
                .addOnSuccessListener(visionText -> {
                    String resultText = visionText.getText();
                    String amount = extractSmartAmount(resultText);
                    String category = suggestCategory(resultText);
                    String date = extractDate(resultText);
                    callback.onResult(amount, category, date, resultText);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "OCR Error", e);
                    callback.onResult("0", "Khác", null, "");
                });
    }

    private static String extractSmartAmount(String text) {
        String[] lines = text.split("\n");
        List<Double> candidates = new ArrayList<>();
        Pattern amountPattern = Pattern.compile("([\\d,.]{4,})");

        for (String line : lines) {
            String lowerLine = line.toLowerCase();
            if (lowerLine.contains("tổng") || lowerLine.contains("total") || 
                lowerLine.contains("thanh toán") || lowerLine.contains("tiền mặt") || 
                lowerLine.contains("vnpay") || lowerLine.contains("momo") || 
                lowerLine.contains("cash")) {
                Matcher m = amountPattern.matcher(line);
                while (m.find()) {
                    try {
                        String clean = m.group(1).replaceAll("[,.]", "");
                        candidates.add(Double.parseDouble(clean));
                    } catch (Exception ignored) {}
                }
            }
        }

        if (!candidates.isEmpty()) return String.valueOf(Collections.max(candidates).longValue());

        Matcher m = amountPattern.matcher(text);
        while (m.find()) {
            try {
                String clean = m.group(1).replaceAll("[,.]", "");
                candidates.add(Double.parseDouble(clean));
            } catch (Exception ignored) {}
        }

        if (!candidates.isEmpty()) {
            double max = Collections.max(candidates);
            if (max > 1000) return String.valueOf((long) max);
        }
        return "0";
    }

    private static String suggestCategory(String text) {
        String lower = text.toLowerCase();
        if (lower.contains("cafe") || lower.contains("phúc long") || lower.contains("highlands") || 
            lower.contains("food") || lower.contains("nhà hàng") || lower.contains("cơm") || lower.contains("trà sữa")) {
            return "Ăn uống";
        } else if (lower.contains("grab") || lower.contains("be") || lower.contains("xăng") || 
                   lower.contains("taxi") || lower.contains("vận tải") || lower.contains("vé xe")) {
            return "Di chuyển";
        } else if (lower.contains("siêu thị") || lower.contains("co.op") || lower.contains("winmart") || 
                   lower.contains("shopee") || lower.contains("lazada") || lower.contains("tiki") || 
                   lower.contains("thời trang") || lower.contains("quần áo")) {
            return "Mua sắm";
        }
        return "Khác";
    }

    private static String extractDate(String text) {
        // Tìm định dạng dd/mm/yyyy hoặc dd-mm-yyyy
        Pattern datePattern = Pattern.compile("(\\d{1,2}[/.-]\\d{1,2}[/.-]\\d{2,4})");
        Matcher m = datePattern.matcher(text);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }
}
