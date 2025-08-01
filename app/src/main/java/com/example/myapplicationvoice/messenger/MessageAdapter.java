package com.example.myapplicationvoice.messenger;

import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplicationvoice.R;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Message> messages;

    public MessageAdapter(List<Message> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void addMessage(Message message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        private TextView messageText;
        private View messageContainer;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            messageContainer = itemView.findViewById(R.id.messageContainer);
        }

        public void bind(Message message) {
            messageText.setText(message.getText());

            // Получаем параметры макета
            FrameLayout.LayoutParams params =
                    (FrameLayout.LayoutParams) messageContainer.getLayoutParams();

            // Устанавливаем максимальную ширину в 80% от ширины экрана
            DisplayMetrics displayMetrics = itemView.getResources().getDisplayMetrics();
            int maxWidth = (int) (displayMetrics.widthPixels * 0.7);
            messageText.setMaxWidth(maxWidth); // Для длинных сообщений

            if (message.isMine()) {
                messageContainer.setBackgroundResource(R.drawable.bg_message_out);
                params.gravity = Gravity.END;
                messageText.setTextColor(Color.WHITE);
            } else {
                messageContainer.setBackgroundResource(R.drawable.bg_message_in);
                params.gravity = Gravity.START;
                messageText.setTextColor(Color.BLACK);
            }

            messageContainer.setLayoutParams(params);
        }
    }
}
