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

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private MaterialToolbar toolbar;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;

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
                        getSupportFragmentManager().popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragmentContainer, new com.codex.adminfoodcaf.fragment.BannerManagementFragment())
                                .addToBackStack(null)
                                .commit();
                    } else if (id == R.id.drawer_home) {
                        getSupportFragmentManager().popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragmentContainer, new com.codex.adminfoodcaf.fragment.HomeFragment())
                                .commit();
                    } else if (id == R.id.Messages) {
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

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                getSupportFragmentManager().popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, new com.codex.adminfoodcaf.fragment.HomeFragment())
                        .commit();
                return true;
            } else if (itemId == R.id.nav_users) {
                getSupportFragmentManager().popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, new com.codex.adminfoodcaf.fragment.UserManagementFragment())
                        .commit();
                return true;
            } else if (itemId == R.id.nav_profile) {
                getSupportFragmentManager().popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, new com.codex.adminfoodcaf.fragment.AnalyticsFragment())
                        .commit();
                return true;
            }

            return false;
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new com.codex.adminfoodcaf.fragment.HomeFragment())
                    .commit();
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
        } else {
            androidx.fragment.app.Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
            if (currentFragment instanceof com.codex.adminfoodcaf.fragment.HomeFragment) {
                ((com.codex.adminfoodcaf.fragment.HomeFragment) currentFragment).filterProducts(query);
            }
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

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}