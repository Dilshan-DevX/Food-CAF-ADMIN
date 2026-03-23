package com.codex.adminfoodcaf.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codex.adminfoodcaf.R;
import com.codex.adminfoodcaf.model.Message;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private static final int MSG_TYPE_LEFT = 0;  // User ගෙන් ආපු මැසේජ් (වම් පැත්තේ)
    private static final int MSG_TYPE_RIGHT = 1; // Admin යවන මැසේජ් (දකුණු පැත්තේ)

    private List<Message> messageList;
    private String adminId; // මෙතනට එන්නේ "admin_user_id" කියන එකයි

    public MessageAdapter(List<Message> messageList, String adminId) {
        this.messageList = messageList;
        this.adminId = adminId;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent, parent, false);
            return new MessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received, parent, false);
            return new MessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);

        holder.tvMessage.setText(message.getMessageText());

        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        String time = sdf.format(new Date(message.getTimestamp()));
        holder.tvTime.setText(time);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        // මැසේජ් එක යවපු කෙනා Admin නම් (Sender ID == adminId), ඒක දකුණු පැත්තෙන් පෙන්වනවා
        if (messageList.get(position).getSenderId().equals(adminId)) {
            return MSG_TYPE_RIGHT;
        } else {
            // නැත්නම් ඒක User එවපු එකක්, ඒ නිසා වම් පැත්තෙන් පෙන්වනවා
            return MSG_TYPE_LEFT;
        }
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;
        TextView tvTime;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}