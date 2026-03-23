package com.codex.adminfoodcaf.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.codex.adminfoodcaf.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BannerManagementFragment extends Fragment {

    private ImageView imgBannerPreview, imgSelectedPreview;
    private TextView tvCurrentTitle, tvCurrentDate;
    private TextView tvImageStatus, tvUploadPercent;
    private TextInputEditText etBannerTitle, etBannerBody, etBannerDate;
    private LinearLayout uploadProgressLayout;
    private ProgressBar uploadProgress;
    private MaterialButton btnPickImage, btnSaveBanner;

    private Uri selectedImageUri  = null;
    private String currentBannerId  = null;
    private String currentImageUrl  = null;

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK
                                && result.getData() != null) {
                            selectedImageUri = result.getData().getData();
                            if (selectedImageUri != null) {
                                tvImageStatus.setText("Image selected ✓");
                                tvImageStatus.setTextColor(
                                        android.graphics.Color.parseColor("#6DBE82"));
                                Glide.with(requireContext())
                                        .load(selectedImageUri)
                                        .centerCrop()
                                        .into(imgSelectedPreview);
                            }
                        }
                    });

    public BannerManagementFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_banner_management, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imgBannerPreview   = view.findViewById(R.id.imgBannerPreview);
        imgSelectedPreview = view.findViewById(R.id.imgSelectedPreview);
        tvCurrentTitle = view.findViewById(R.id.tvCurrentTitle);
        tvCurrentDate = view.findViewById(R.id.tvCurrentDate);
        tvImageStatus = view.findViewById(R.id.tvImageStatus);
        tvUploadPercent = view.findViewById(R.id.tvUploadPercent);
        etBannerTitle = view.findViewById(R.id.etBannerTitle);
        etBannerBody = view.findViewById(R.id.etBannerBody);
        etBannerDate = view.findViewById(R.id.etBannerDate);
        uploadProgressLayout = view.findViewById(R.id.uploadProgressLayout);
        uploadProgress = view.findViewById(R.id.uploadProgress);
        btnPickImage = view.findViewById(R.id.btnPickImage);
        btnSaveBanner = view.findViewById(R.id.btnSaveBanner);

        loadCurrentBanner();

        btnPickImage.setOnClickListener(v -> openImagePicker());

        btnSaveBanner.setOnClickListener(v -> saveBanner());
    }

    private void loadCurrentBanner() {
        FirebaseFirestore.getInstance()
                .collection("banner")
                .limit(1)
                .get()
                .addOnSuccessListener(snap -> {
                    if (!isAdded()) return;

                    if (!snap.isEmpty()) {
                        var doc = snap.getDocuments().get(0);
                        currentBannerId = doc.getId();

                        String title = doc.getString("banner_title");
                        String body  = doc.getString("banner_body");
                        String date  = doc.getString("banner_date");
                        String url   = doc.getString("banner_url");
                        currentImageUrl = url;

                        tvCurrentTitle.setText(title != null ? title : "-");
                        tvCurrentDate.setText(date  != null ? date  : "-");

                        if (title != null) etBannerTitle.setText(title);
                        if (body  != null) etBannerBody.setText(body);
                        if (date  != null) etBannerDate.setText(date);

                        if (url != null && !url.isEmpty()) {
                            loadBannerImage(url, imgBannerPreview);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    Toast.makeText(getContext(),
                            "Failed to load banner: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void saveBanner() {
        String title = etBannerTitle.getText() != null
                ? etBannerTitle.getText().toString().trim() : "";
        String body  = etBannerBody.getText()  != null
                ? etBannerBody.getText().toString().trim()  : "";
        String date  = etBannerDate.getText()  != null
                ? etBannerDate.getText().toString().trim()  : "";

        if (title.isEmpty()) {
            etBannerTitle.setError("Title required");
            return;
        }

        setBusy(true);

        if (selectedImageUri != null) {

            uploadImageThenSave(title, body, date);
        } else {

            saveToFirestore(title, body, date, currentImageUrl);
        }
    }

    private void uploadImageThenSave(String title, String body, String date) {
        String fileName = "banner_images/banner_" + UUID.randomUUID() + ".jpg";
        StorageReference storageRef = FirebaseStorage
                .getInstance("gs://foodcaf-82dbc.firebasestorage.app")
                .getReference()
                .child(fileName);

        uploadProgressLayout.setVisibility(View.VISIBLE);
        uploadProgress.setProgress(0);

        deleteOldImageFromStorage();

        storageRef.putFile(selectedImageUri)
                .addOnProgressListener(snapshot -> {
                    if (!isAdded()) return;
                    double progress = (100.0 * snapshot.getBytesTransferred())
                            / snapshot.getTotalByteCount();
                    int pct = (int) progress;
                    uploadProgress.setProgress(pct);
                    tvUploadPercent.setText(pct + "%");
                })
                .addOnSuccessListener(taskSnapshot -> {
                    storageRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                if (!isAdded()) return;
                                saveToFirestore(title, body, date, uri.toString());
                            })
                            .addOnFailureListener(e -> {
                                if (!isAdded()) return;
                                setBusy(false);
                                uploadProgressLayout.setVisibility(View.GONE);
                                Toast.makeText(getContext(),
                                        "Failed to get download URL: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    setBusy(false);
                    uploadProgressLayout.setVisibility(View.GONE);
                    Toast.makeText(getContext(),
                            "Image upload failed: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void deleteOldImageFromStorage() {
        if (currentImageUrl == null || currentImageUrl.isEmpty()) return;
        try {
            StorageReference oldRef;
            if (currentImageUrl.startsWith("gs://")) {
                oldRef = FirebaseStorage.getInstance("gs://foodcaf-82dbc.firebasestorage.app")
                        .getReferenceFromUrl(currentImageUrl);
            } else {

                oldRef = FirebaseStorage.getInstance("gs://foodcaf-82dbc.firebasestorage.app")
                        .getReferenceFromUrl(currentImageUrl);
            }
            oldRef.delete();
        } catch (Exception ignored) {

        }
    }

    private void saveToFirestore(String title, String body, String date, String imageUrl) {
        Map<String, Object> data = new HashMap<>();
        data.put("banner_title", title);
        data.put("banner_body",  body);
        data.put("banner_date",  date);
        if (imageUrl != null) data.put("banner_url", imageUrl);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (currentBannerId != null) {
            db.collection("banner").document(currentBannerId)
                    .update(data)
                    .addOnSuccessListener(v -> onSaveSuccess())
                    .addOnFailureListener(this::onSaveFailure);
        } else {
            db.collection("banner")
                    .add(data)
                    .addOnSuccessListener(ref -> {
                        currentBannerId = ref.getId();
                        onSaveSuccess();
                    })
                    .addOnFailureListener(this::onSaveFailure);
        }
    }

    private void onSaveSuccess() {
        if (!isAdded()) return;
        setBusy(false);
        uploadProgressLayout.setVisibility(View.GONE);
        selectedImageUri = null;
        tvImageStatus.setText("No image selected");
        tvImageStatus.setTextColor(android.graphics.Color.parseColor("#888888"));
        Toast.makeText(getContext(), "Banner saved successfully! ✓",
                Toast.LENGTH_SHORT).show();
        loadCurrentBanner();
    }

    private void onSaveFailure(Exception e) {
        if (!isAdded()) return;
        setBusy(false);
        uploadProgressLayout.setVisibility(View.GONE);
        Toast.makeText(getContext(),
                "Save failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
    }

    private void loadBannerImage(String url, ImageView target) {
        if (url.startsWith("gs://")) {
            FirebaseStorage.getInstance("gs://foodcaf-82dbc.firebasestorage.app")
                    .getReferenceFromUrl(url)
                    .getDownloadUrl()
                    .addOnSuccessListener(uri -> {
                        if (!isAdded()) return;
                        Glide.with(requireContext())
                                .load(uri.toString())
                                .centerCrop()
                                .placeholder(android.R.drawable.ic_menu_gallery)
                                .into(target);
                    })
                    .addOnFailureListener(e -> {
                        if (!isAdded()) return;
                        Toast.makeText(getContext(),
                                "Banner image load failed: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        } else {
            Glide.with(requireContext())
                    .load(url)
                    .centerCrop()
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(target);
        }
    }

    private void setBusy(boolean busy) {
        btnSaveBanner.setEnabled(!busy);
        btnPickImage.setEnabled(!busy);
        btnSaveBanner.setText(busy ? "Saving..." : "SAVE BANNER");
    }
}