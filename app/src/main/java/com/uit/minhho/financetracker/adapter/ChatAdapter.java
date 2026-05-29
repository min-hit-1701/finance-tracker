package com.uit.minhho.financetracker.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.model.ChatMessage;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private final List<ChatMessage> messages;

    public ChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        if (message.isBot()) {
            holder.cardBot.setVisibility(View.VISIBLE);
            holder.cardUser.setVisibility(View.GONE);
            holder.tvBot.setText(message.getText());
        } else {
            holder.cardBot.setVisibility(View.GONE);
            holder.cardUser.setVisibility(View.VISIBLE);
            holder.tvUser.setText(message.getText());
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        View cardUser, cardBot;
        TextView tvUser, tvBot;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            cardUser = itemView.findViewById(R.id.card_user_message);
            cardBot = itemView.findViewById(R.id.card_bot_message);
            tvUser = itemView.findViewById(R.id.tv_user_message);
            tvBot = itemView.findViewById(R.id.tv_bot_message);
        }
    }
}
