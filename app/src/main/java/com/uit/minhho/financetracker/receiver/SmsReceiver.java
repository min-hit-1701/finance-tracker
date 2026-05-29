package com.uit.minhho.financetracker.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

public class SmsReceiver extends BroadcastReceiver {
    private static SmsListener mListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle data = intent.getExtras();
        if (data == null) return;

        Object[] pdus = (Object[]) data.get("pdus");
        if (pdus == null) return;

        for (Object pdu : pdus) {
            SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
            String messageBody = smsMessage.getMessageBody();
            
            // Giả sử tin nhắn có định dạng: "Ma xac thuc cua ban la: 123456"
            if (messageBody.contains("Ma xac thuc")) {
                String otp = messageBody.replaceAll("[^0-9]", "");
                if (otp.length() >= 6 && mListener != null) {
                    mListener.onSmsReceived(otp.substring(0, 6));
                }
            }
        }
    }

    public static void bindListener(SmsListener listener) {
        mListener = listener;
    }

    public static void unbindListener() {
        mListener = null;
    }

    public interface SmsListener {
        void onSmsReceived(String otp);
    }
}
