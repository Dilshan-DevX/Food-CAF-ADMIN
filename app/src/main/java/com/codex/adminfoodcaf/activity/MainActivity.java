package com.codex.adminfoodcaf.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.codex.adminfoodcaf.R;
import com.codex.adminfoodcaf.model.User;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private MaterialToolbar toolbar;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;

    private long appStartTime = System.currentTimeMillis();
    private java.util.List<ListenerRegistration> messageListeners = new java.util.ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawertLayout);
        toolbar = findViewById(R.id.toolbar);
        navigationView = findViewById(R.id.sideNavView);
        bottomNavigationView = findViewById(R.id.bottomNavView);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name,
                R.string.app_name);
        drawerLayout.addDrawerListener(toggle);
        toggle.setDrawerIndicatorEnabled(false);
        toggle.syncState();

        ImageView menuIcon = findViewById(R.id.menu_Logo);
        if (menuIcon != null) {
            Glide.with(this)
                    .load(R.drawable.menu_24px)
                    .into(menuIcon);

            menuIcon.setOnClickListener(view -> {
                if (drawerLayout != null) {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            });
        }

        if (navigationView != null) {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                navigationView.getMenu().findItem(R.id.nav_Login).setVisible(false);
                navigationView.getMenu().findItem(R.id.nav_Logout).setVisible(true);
            } else {
                navigationView.getMenu().findItem(R.id.nav_Login).setVisible(true);
                navigationView.getMenu().findItem(R.id.nav_Logout).setVisible(false);
            }
            
            navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int id = item.getItemId();

                    if (id == R.id.nav_Logout) {
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(MainActivity.this, SigninActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                        return true;
                    } else if (id == R.id.nav_add) {
                        uncheckBottomNav(bottomNavigationView);
                        getSupportFragmentManager().popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragmentContainer, new com.codex.adminfoodcaf.fragment.BannerManagementFragment())
                                .addToBackStack(null)
                                .commit();
                    } else if (id == R.id.drawer_home) {
                        bottomNavigationView.getMenu().findItem(R.id.nav_home).setChecked(true);
                        getSupportFragmentManager().popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragmentContainer, new com.codex.adminfoodcaf.fragment.HomeFragment())
                                .commit();
                    } else if (id == R.id.Messages) {
                        uncheckBottomNav(bottomNavigationView);
                        getSupportFragmentManager().popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragmentContainer, new com.codex.adminfoodcaf.fragment.InboxFragment())
                                .addToBackStack(null)
                                .commit();
                    }

                    if (drawerLayout != null) {
                        drawerLayout.closeDrawer(GravityCompat.START);
                    }
                    return true;
                }
            });
        }

        ImageView navLogo = findViewById(R.id.nav_Logo);
        if (navLogo != null) {
            Glide.with(this)
                    .load(R.drawable.img)
                    .into(navLogo);
        }

        loadUserProfileInfo();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                navigationView.setCheckedItem(R.id.drawer_home);
                getSupportFragmentManager().popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, new com.codex.adminfoodcaf.fragment.HomeFragment())
                        .commit();
                return true;
            } else if (itemId == R.id.nav_users) {
                uncheckSideNav(navigationView);
                getSupportFragmentManager().popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, new com.codex.adminfoodcaf.fragment.UserManagementFragment())
                        .commit();
                return true;
            } else if (itemId == R.id.nav_profile) {
                uncheckSideNav(navigationView);
                getSupportFragmentManager().popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, new com.codex.adminfoodcaf.fragment.AnalyticsFragment())
                        .commit();
                return true;
            }

            return false;
        });

        listenForNewMessages();

        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getBooleanExtra("open_messages", false)) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, new com.codex.adminfoodcaf.fragment.InboxFragment())
                        .commit();
            } else {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, new com.codex.adminfoodcaf.fragment.HomeFragment())
                        .commit();
            }
        }
        
        setupGlobalSearch();

    }

    private void setupGlobalSearch() {
        android.widget.AutoCompleteTextView searchInput = findViewById(R.id.textInputSearch);
        if (searchInput == null) return;

        FirebaseFirestore.getInstance().collection("products").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    java.util.List<String> productNames = new java.util.ArrayList<>();
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String title = doc.getString("foodTitle");
                        if (title != null) productNames.add(title);
                    }
                    android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
                            this, android.R.layout.simple_dropdown_item_1line, productNames);
                    searchInput.setAdapter(adapter);
                });

        searchInput.setOnItemClickListener((parent, view, position, id) -> {
            String query = (String) parent.getItemAtPosition(position);
            searchInput.setText(query, false); // Set text explicitly without showing dropdown again
            navigateToHomeWithSearch(query);
            hideKeyboard(searchInput);
        });

        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                String query = searchInput.getText().toString().trim();
                if (!query.isEmpty()) {
                    navigateToHomeWithSearch(query);
                }
                hideKeyboard(searchInput);
                return true;
            }
            return false;
        });
    }

    private void navigateToHomeWithSearch(String query) {
        if (bottomNavigationView.getSelectedItemId() != R.id.nav_home) {
            getSupportFragmentManager().popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
            com.codex.adminfoodcaf.fragment.HomeFragment homeFrag = new com.codex.adminfoodcaf.fragment.HomeFragment();
            Bundle bundle = new Bundle();
            bundle.putString("searchQuery", query);
            homeFrag.setArguments(bundle);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, homeFrag)
                    .commit();

            bottomNavigationView.getMenu().findItem(R.id.nav_home).setChecked(true);
            navigationView.setCheckedItem(R.id.drawer_home);
        } else {
            androidx.fragment.app.Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
            if (currentFragment instanceof com.codex.adminfoodcaf.fragment.HomeFragment) {
                ((com.codex.adminfoodcaf.fragment.HomeFragment) currentFragment).filterProducts(query);
            }
        }
    }
    
    private void uncheckBottomNav(BottomNavigationView bottomNav) {
        android.view.Menu menu = bottomNav.getMenu();
        menu.setGroupCheckable(0, true, false);
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setChecked(false);
        }
        menu.setGroupCheckable(0, true, true);
    }

    private void uncheckSideNav(NavigationView sideNav) {
        android.view.Menu menu = sideNav.getMenu();
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setChecked(false);
        }
    }

    private void hideKeyboard(View view) {
        android.view.inputmethod.InputMethodManager imm = 
            (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        view.clearFocus();
    }

    private void loadUserProfileInfo() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && navigationView != null) {
            View headerView = navigationView.getHeaderView(0);
            ImageView profileImg = headerView.findViewById(R.id.profileImg);
            TextView profileName = headerView.findViewById(R.id.profileName);
            TextView profileEmail = headerView.findViewById(R.id.profileEmail);

            profileEmail.setText(currentUser.getEmail());

            FirebaseFirestore.getInstance().collection("users").document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            User user = documentSnapshot.toObject(User.class);
                            if (user != null) {
                                profileName.setText(user.getName());
                                if (user.getProfilePicUrl() != null && !user.getProfilePicUrl().isEmpty()) {
                                    Glide.with(MainActivity.this)
                                            .load(user.getProfilePicUrl())
                                            .circleCrop()
                                            .into(profileImg);
                                }
                            }
                        }
                    })
                    .addOnFailureListener(e -> Toast
                            .makeText(MainActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show());
        }
    }

    private void listenForNewMessages() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        removeMessageListeners();

        FirebaseFirestore.getInstance().collection("users").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    for (com.google.firebase.firestore.QueryDocumentSnapshot userDoc : queryDocumentSnapshots) {
                        String customerId = userDoc.getId();

                        // Skip listening to admin's own chats folder if it exists
                        if (customerId.equals(currentUserUid)) continue;

                        ListenerRegistration listener = FirebaseFirestore.getInstance()
                                .collection("chats").document(customerId).collection("messages")
                                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
                                .addSnapshotListener((value, error) -> {
                                    if (error != null || value == null) return;

                                    for (com.google.firebase.firestore.DocumentChange dc : value.getDocumentChanges()) {
                                        if (dc.getType() == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                                            com.codex.adminfoodcaf.model.Message msg = dc.getDocument().toObject(com.codex.adminfoodcaf.model.Message.class);

                                            if (msg.getSenderId() != null && !msg.getSenderId().equals(currentUserUid) &&
                                                    msg.getTimestamp() > appStartTime &&
                                                    !com.codex.adminfoodcaf.fragment.AdminMessageFragment.isChatOpen) {

                                                showNotification("New Message from " + (userDoc.getString("name") != null ? userDoc.getString("name") : "Customer"), msg.getMessageText(), (int) msg.getTimestamp());
                                            }
                                        }
                                    }
                                });
                        messageListeners.add(listener);
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("ChatNotification", "Failed to load users for listeners.", e);
                });
    }

    private void removeMessageListeners() {
        for (ListenerRegistration listener : messageListeners) {
            listener.remove();
        }
        messageListeners.clear();
    }

    private void showNotification(String title, String body, int notificationId) {
        android.app.NotificationManager notificationManager = (android.app.NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);
        String channelId = "admin_chat_notifications";

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            android.app.NotificationChannel channel = new android.app.NotificationChannel(
                    channelId,
                    "Customer Chat Messages",
                    android.app.NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("open_messages", true);
        
        int flags = android.app.PendingIntent.FLAG_UPDATE_CURRENT;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            flags |= android.app.PendingIntent.FLAG_IMMUTABLE;
        }
        android.app.PendingIntent pendingIntent = android.app.PendingIntent.getActivity(this, 0, intent, flags);

        androidx.core.app.NotificationCompat.Builder builder = new androidx.core.app.NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.img)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                .setDefaults(androidx.core.app.NotificationCompat.DEFAULT_ALL)
                .setContentIntent(pendingIntent);

        notificationManager.notify(notificationId, builder.build());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeMessageListeners();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}