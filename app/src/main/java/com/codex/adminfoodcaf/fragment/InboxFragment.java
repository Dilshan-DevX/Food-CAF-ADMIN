package com.codex.adminfoodcaf.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codex.adminfoodcaf.R;
import com.codex.adminfoodcaf.adapter.InboxAdapter;
import com.codex.adminfoodcaf.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class InboxFragment extends Fragment {

    private RecyclerView rvInbox;
    private FirebaseFirestore db;
    private InboxAdapter adapter;
    private List<User> userList = new ArrayList<>();

    public InboxFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_inbox, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvInbox = view.findViewById(R.id.rvInbox);
        rvInbox.setLayoutManager(new LinearLayoutManager(getContext()));

        db = FirebaseFirestore.getInstance();

        loadUsers();
    }

    private void loadUsers() {
        // Currently logged-in admin ගේ UID එක ගන්නවා
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentAdminUid = firebaseUser != null ? firebaseUser.getUid() : "";

        db.collection("users").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!isAdded()) return;

                    userList.clear();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                        User user = doc.toObject(User.class);
                        // Admin account skip — chat list ෙල show නොකෙරනවා
                        if (user != null && !doc.getId().equals(currentAdminUid)) {
                            userList.add(user);
                        }
                    }

                    setupAdapter();
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Failed to load users", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupAdapter() {
        adapter = new InboxAdapter(userList, new InboxAdapter.OnUserClickListener() {
            @Override
            public void onUserClick(User user) {
                // 🔴 User ව ක්ලික් කළාම AdminMessageFragment (Chat) එකට යනවා
                Bundle bundle = new Bundle();
                bundle.putString("customerId", user.getUId()); // User ගේ ID එක
                bundle.putString("customerName", user.getName()); // User ගේ නම

                AdminMessageFragment chatFragment = new AdminMessageFragment();
                chatFragment.setArguments(bundle);

                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, chatFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        rvInbox.setAdapter(adapter);
    }
}