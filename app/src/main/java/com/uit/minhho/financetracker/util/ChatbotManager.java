package com.uit.minhho.financetracker.util;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.adapter.ChatAdapter;
import com.uit.minhho.financetracker.model.ChatMessage;

import java.util.ArrayList;
import java.util.List;

public class ChatbotManager {
    private final Activity activity;
    private View bubbleView;
    private View chatWindow;
    private RecyclerView rvMessages;
    private ChatAdapter adapter;
    private List<ChatMessage> messageList;
    private EditText etInput;

    private float dX, dY; // Tọa độ phục vụ kéo thả

    public ChatbotManager(Activity activity) {
        this.activity = activity;
        this.messageList = new ArrayList<>();
    }

    public void init() {
        ViewGroup root = activity.findViewById(android.R.id.content);
        if (root == null) return;
        
        LayoutInflater inflater = LayoutInflater.from(activity);

        // 1. Khởi tạo Bong bóng chat
        bubbleView = inflater.inflate(R.layout.layout_chatbot_bubble, root, false);
        root.addView(bubbleView);

        // 2. Khởi tạo Cửa sổ chat
        chatWindow = inflater.inflate(R.layout.layout_chatbot_window, root, false);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.gravity = android.view.Gravity.BOTTOM;
        params.setMargins(32, 0, 32, 220); // Đặt trên Bottom Navigation
        chatWindow.setLayoutParams(params);
        root.addView(chatWindow);

        initChatLogic();
        makeBubbleDraggable();
    }

    private void makeBubbleDraggable() {
        bubbleView.setOnTouchListener(new View.OnTouchListener() {
            private float startX, startY;
            private long startTime;

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dX = view.getX() - event.getRawX();
                        dY = view.getY() - event.getRawY();
                        startX = event.getRawX();
                        startY = event.getRawY();
                        startTime = System.currentTimeMillis();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        view.animate()
                                .x(event.getRawX() + dX)
                                .y(event.getRawY() + dY)
                                .setDuration(0)
                                .start();
                        break;

                    case MotionEvent.ACTION_UP:
                        // Nếu là click (không di chuyển nhiều)
                        if (Math.abs(event.getRawX() - startX) < 10 && 
                            Math.abs(event.getRawY() - startY) < 10 && 
                            (System.currentTimeMillis() - startTime) < 200) {
                            toggleChatWindow();
                        }
                        break;
                }
                return true;
            }
        });
    }

    private void toggleChatWindow() {
        if (chatWindow.getVisibility() == View.VISIBLE) {
            chatWindow.setVisibility(View.GONE);
            bubbleView.setAlpha(1.0f);
        } else {
            chatWindow.setVisibility(View.VISIBLE);
            bubbleView.setAlpha(0.3f); // Làm mờ bubble khi mở chat
            if (messageList.isEmpty()) {
                addBotMessage("Xin chào! Tôi là trợ lý AI Finance. Bạn muốn kiểm tra ngân sách hay cần tư vấn chi tiêu?");
            }
        }
    }

    private void initChatLogic() {
        rvMessages = chatWindow.findViewById(R.id.rv_chat_messages);
        etInput = chatWindow.findViewById(R.id.et_chat_input);
        View btnSend = chatWindow.findViewById(R.id.btn_send_chat);
        View btnClose = chatWindow.findViewById(R.id.btn_close_chat);

        adapter = new ChatAdapter(messageList);
        rvMessages.setLayoutManager(new LinearLayoutManager(activity));
        rvMessages.setAdapter(adapter);

        btnClose.setOnClickListener(v -> toggleChatWindow());

        btnSend.setOnClickListener(v -> {
            String text = etInput.getText().toString().trim();
            if (!text.isEmpty()) {
                addUserMessage(text);
                etInput.setText("");
                processAiResponse(text);
            }
        });
    }

    private void addUserMessage(String text) {
        messageList.add(new ChatMessage(text, false));
        adapter.notifyItemInserted(messageList.size() - 1);
        rvMessages.smoothScrollToPosition(messageList.size() - 1);
    }

    private void addBotMessage(String text) {
        messageList.add(new ChatMessage(text, true));
        adapter.notifyItemInserted(messageList.size() - 1);
        rvMessages.smoothScrollToPosition(messageList.size() - 1);
    }

    private void processAiResponse(String userText) {
        String query = userText.toLowerCase();
        String response;

        if (query.contains("chi tiêu") || query.contains("hết bao nhiêu")) {
            response = "📊 Bạn đã chi 1.250.000đ trong tuần này. Khoản chi lớn nhất là 'Cơm tấm Cali' (150k).";
        } else if (query.contains("tiết kiệm")) {
            response = "💡 Mẹo: Bạn có thể tiết kiệm thêm 200k mỗi tháng nếu giảm bớt các khoản 'Giải trí' không cần thiết.";
        } else if (query.contains("ngân sách")) {
            response = "⚠️ Ngân sách 'Mua sắm' của bạn chỉ còn 10%. Hãy cân nhắc trước khi mua thêm đồ mới nhé!";
        } else {
            response = "Tôi có thể giúp bạn tổng hợp chi tiêu, nhắc nhở ngân sách hoặc tư vấn tài chính. Bạn hãy đặt câu hỏi nhé!";
        }

        // Tạo hiệu ứng chờ AI phản hồi
        chatWindow.postDelayed(() -> addBotMessage(response), 800);
    }
}
