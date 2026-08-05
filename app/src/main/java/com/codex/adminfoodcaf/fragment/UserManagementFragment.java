package com.codex.adminfoodcaf.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.codex.adminfoodcaf.R;
import com.codex.adminfoodcaf.adapter.UserAdapter;
import com.codex.adminfoodcaf.model.User;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class UserManagementFragment extends Fragment {

    private RecyclerView  rvUsers;
    private TextInputEditText  etSearch;
    private TextView  tvUserCount;
    private UserAdapter  adapter;

    public UserManagementFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_uesr_management, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvUsers     = view.findViewById(R.id.rvUsers);
        etSearch    = view.findViewById(R.id.etSearch);
        tvUserCount = view.findViewById(R.id.tvUserCount);

        rvUsers.setLayoutManager(new LinearLayoutManager(getContext()));

        loadUsers();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                if (adapter != null) {
                    adapter.filter(s.toString());
                    rvUsers.scrollToPosition(0);
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void loadUsers() {
        FirebaseFirestore.getInstance()
                .collection("users")
                .addSnapshotListener((snap, e) -> {
                    if (e != null || !isAdded() || snap == null) {
                        if (isAdded() && e != null) {
                            Toast.makeText(getContext(),
                                    "Failed to load users: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                        return;
                    }

                    List<User> userList = snap.toObjects(User.class);


                    tvUserCount.setText(userList.size() + " users");

                    adapter = new UserAdapter(userList);
                    rvUsers.setAdapter(adapter);

                    if (etSearch != null && etSearch.getText() != null && !etSearch.getText().toString().isEmpty()) {
                        adapter.filter(etSearch.getText().toString());
                    }
                });
    }
}