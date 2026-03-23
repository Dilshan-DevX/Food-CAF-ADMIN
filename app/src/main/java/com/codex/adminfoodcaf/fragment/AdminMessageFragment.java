package com.codex.adminfoodcaf.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codex.adminfoodcaf.R;
import com.codex.adminfoodcaf.adapter.MessageAdapter;
import com.codex.adminfoodcaf.model.Message;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class AdminMessageFragment extends Fragment {

    private RecyclerView rvMessages;
    private EditText etMessageInput;
    private FloatingActionButton btnSend;
    private TextView tvTitle;

    private FirebaseFirestore db;

    private String customerId;
    private String customerName;

    private final String ADMIN_ID = "UO6OFTZdtaRAiWUJLD5TiJIuONj2";

    public AdminMessageFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_message, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvMessages = view.findViewById(R.id.rvMessages);
        etMessageInput = view.findViewById(R.id.etMessageInput);
        btnSend = view.findViewById(R.id.btnSend);
        tvTitle = view.findViewById(R.id.tvTitle);

        db = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            customerId = getArguments().getString("customerId");
            customerName = getArguments().getString("customerName");
            tvTitle.setText("Chat with " + (customerName != null ? customerName : "User"));
        }

        setupRecyclerView();

        if (customerId != null) {
            loadMessages();
        }

        btnSend.setOnClickListener(v -> {
            String msgText = etMessageInput.getText().toString().trim();
            if (!msgText.isEmpty() && customerId != null) {
                sendMessage(msgText);
            }
        });
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(layoutManager);
    }

    private void sendMessage(String msgText) {
        Message message = new Message();
        message.setSenderId(ADMIN_ID);
        message.setReceiverId(customerId);
        message.setMessageText(msgText);
        message.setTimestamp(System.currentTimeMillis());

        db.collection("chats").document(customerId).collection("messages")
                .add(message)
                .addOnSuccessListener(doc -> etMessageInput.setText(""))
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to send", Toast.LENGTH_SHORT).show());
    }

    private void loadMessages() {
        db.collection("chats").document(customerId).collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;

                    List<Message> messageList = new ArrayList<>();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : value.getDocuments()) {
                        messageList.add(doc.toObject(Message.class));
                    }

                    MessageAdapter adapter = new MessageAdapter(messageList, ADMIN_ID);
                    rvMessages.setAdapter(adapter);
                    rvMessages.scrollToPosition(messageList.size() - 1);
                });
    }
}