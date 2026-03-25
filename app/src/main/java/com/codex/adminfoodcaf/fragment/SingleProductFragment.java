package com.codex.adminfoodcaf.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import android.net.Uri;
import android.app.ProgressDialog;
import android.content.Intent;
import android.app.Activity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import com.bumptech.glide.Glide;
import com.codex.adminfoodcaf.R;
import com.codex.adminfoodcaf.model.Product;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SingleProductFragment extends Fragment {

    private static final String TAG = "ProductDebug";

    private ImageView imgProduct, imgProduct2;
    private EditText etFoodTitle, etPrice, etRating, etFoodTime, etFoodDetail, etIngredients;
    private AutoCompleteTextView dropdownCategory;
    private SwitchMaterial switchAvailability;
    private TextView tvAvailabilityLabel;
    private View  btnUpdate;


    private String productId;
    private String selectedCategoryId = "";
    private final List<String> categoryIds   = new ArrayList<>();
    private final List<String> categoryNames = new ArrayList<>();
    

    private Uri imageUri1 = null;
    private Uri imageUri2 = null;
    private int selectedImageIndex = 1; 
    private final List<String> currentProductImageUrls = new ArrayList<>();
    private ActivityResultLauncher<Intent> galleryLauncher;
    private String realProductId = "";

    public SingleProductFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedUri = result.getData().getData();
                        if (selectedUri != null) {
                            if (selectedImageIndex == 1) {
                                imageUri1 = selectedUri;
                                imgProduct.setImageURI(imageUri1);
                            } else if (selectedImageIndex == 2) {
                                imageUri2 = selectedUri;
                                imgProduct2.setImageURI(imageUri2);
                            }
                        }
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragmentingleproduct, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Bind Views
        imgProduct = view.findViewById(R.id.imgProduct);
        imgProduct2 = view.findViewById(R.id.imgProduct2);
        etFoodTitle = view.findViewById(R.id.etFoodTitle);
        etPrice = view.findViewById(R.id.etPrice);
        etRating  = view.findViewById(R.id.etRating);
        etFoodTime  = view.findViewById(R.id.etFoodTime);
        etFoodDetail  = view.findViewById(R.id.etFoodDetail);
        etIngredients = view.findViewById(R.id.etIngredients);
        dropdownCategory = view.findViewById(R.id.dropdownCategory);
        switchAvailability = view.findViewById(R.id.switchAvailability);
        tvAvailabilityLabel = view.findViewById(R.id.tvAvailabilityLabel);
        btnUpdate = view.findViewById(R.id.btnUpdate);


        if (getArguments() != null) {
            productId = getArguments().getString("productId");
        }

        Log.d(TAG, "▶ productId received: " + productId);

        loadCategories();

        switchAvailability.setOnCheckedChangeListener((buttonView, isChecked) -> {
            setAvailabilityLabel(isChecked);
        });

        dropdownCategory.setOnItemClickListener((parent, view1, position, id) -> {
            selectedCategoryId = categoryIds.get(position);
        });

        btnUpdate.setOnClickListener(v -> updateProduct());

        imgProduct.setOnClickListener(v -> openGallery(1));
        imgProduct2.setOnClickListener(v -> openGallery(2));
    }

    private void openGallery(int index) {
        selectedImageIndex = index;
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    private void loadCategories() {
        Log.d(TAG, "▶ loadCategories() called");
        FirebaseFirestore.getInstance().collection("categories").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "✔ categories count: " + queryDocumentSnapshots.size());
                    categoryIds.clear();
                    categoryNames.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String rCatId = doc.getString("categoryId");
                        if (rCatId == null || rCatId.isEmpty()) rCatId = doc.getId();
                        categoryIds.add(rCatId);
                        
                        String catName = doc.getString("categoryName");
                        categoryNames.add(catName != null ? catName : "Unknown");
                    }
                    if (getContext() != null) {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, categoryNames);
                        dropdownCategory.setAdapter(adapter);
                    }


                    Log.d(TAG, "▶ Calling loadProduct after categories. productId=" + productId);
                    if (productId != null) {
                        loadProduct(productId);
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Failed to load categories", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadProduct(String id) {
        Log.d(TAG, "▶ loadProduct() called with id: " + id);
        FirebaseFirestore.getInstance()
                .collection("products")
                .document(id)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!isAdded() || doc == null || !doc.exists()) {
                        Log.e(TAG, "✖ doc null or not exists. doc=" + doc + " exists=" + (doc != null && doc.exists()));
                        return;
                    }
                    Log.d(TAG, "✔ Product doc found: " + doc.getData());

                    Product p = doc.toObject(Product.class);
                    if (p == null) return;

                    realProductId = (p.getProductId() != null && !p.getProductId().isEmpty()) ? p.getProductId() : productId;


                    etFoodTitle.setText(p.getFoodTitle() != null ? p.getFoodTitle() : "");
                    etPrice.setText(String.valueOf(p.getProductPrice()));
                    etRating.setText(p.getFoodRating() != null ? p.getFoodRating() : "");
                    etFoodTime.setText(p.getFoodTime() != null ? p.getFoodTime() : "");
                    etFoodDetail.setText(p.getFoodDetail() != null ? p.getFoodDetail() : "");
                    etIngredients.setText(p.getIngrideint() != null ? p.getIngrideint() : "");


                    switchAvailability.setChecked(p.isAvailability());
                    setAvailabilityLabel(p.isAvailability());

                    String catId = p.getCategoryId();
                    if (catId != null && !catId.isEmpty()) {
                        String cleanCat = catId.trim();
                        int idx = -1;

                        for (int i = 0; i < categoryIds.size(); i++) {
                            if (categoryIds.get(i).equalsIgnoreCase(cleanCat)) {
                                idx = i;
                                break;
                            }
                        }

                        if (idx < 0) {
                            for (int i = 0; i < categoryNames.size(); i++) {
                                if (categoryNames.get(i).equalsIgnoreCase(cleanCat)) {
                                    idx = i;
                                    break;
                                }
                            }
                        }

                        if (idx >= 0) {
                            String cName = categoryNames.get(idx);
                            selectedCategoryId = categoryIds.get(idx);
                            dropdownCategory.setText(cName, false);
                        } else {

                            Toast.makeText(getContext(), "Debug: Category '" + cleanCat + "' not found", Toast.LENGTH_LONG).show();
                            dropdownCategory.setText("Select Category", false);
                        }
                    } else {
                        Toast.makeText(getContext(), "Debug: DB Category is null or empty", Toast.LENGTH_LONG).show();
                        dropdownCategory.setText("Select Category", false);
                    }

                    List<String> imgs = p.getProductImage();
                    if (imgs != null) {
                        currentProductImageUrls.clear();
                        currentProductImageUrls.addAll(imgs);
                    }
                    
                    if (imgs != null && !imgs.isEmpty()) {
                        Glide.with(requireContext())
                                .load(imgs.get(0))
                                .placeholder(android.R.drawable.ic_menu_gallery)
                                .centerCrop()
                                .into(imgProduct);

                        if (imgs.size() > 1) {
                            Glide.with(requireContext())
                                    .load(imgs.get(1))
                                    .placeholder(android.R.drawable.ic_menu_gallery)
                                    .centerCrop()
                                    .into(imgProduct2);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Product load failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateProduct() {
        if (productId == null || productId.isEmpty()) {
            Toast.makeText(getContext(), "Invalid product ID", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = etFoodTitle.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String rating = etRating.getText().toString().trim();
        String time = etFoodTime.getText().toString().trim();
        String detail = etFoodDetail.getText().toString().trim();
        String ingredients = etIngredients.getText().toString().trim();
        boolean available  = switchAvailability.isChecked();

        if (title.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(getContext(), "Title and Price cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        double price = 0;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid price format", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUri1 != null || imageUri2 != null) {
            uploadImagesAndSave(title, price, rating, time, detail, ingredients, available);
        } else {
            saveProductData(title, price, rating, time, detail, ingredients, available, currentProductImageUrls);
        }
    }
    
    private void uploadImagesAndSave(String title, double price, String rating, String time, String detail, String ingredients, boolean available) {
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Uploading Images...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        StorageReference storageRef = FirebaseStorage.getInstance()
                .getReferenceFromUrl("gs://foodcaf-82dbc.firebasestorage.app/product-images")
                .child(realProductId);

        uploadSingleImage(imageUri1, storageRef, url1 -> {
            if (url1 != null && !url1.isEmpty()) {
                if (currentProductImageUrls.isEmpty()) currentProductImageUrls.add(url1);
                else currentProductImageUrls.set(0, url1);
            }

            uploadSingleImage(imageUri2, storageRef, url2 -> {
                if (url2 != null && !url2.isEmpty()) {
                    if (currentProductImageUrls.size() < 2) {
                        if (currentProductImageUrls.isEmpty()) currentProductImageUrls.add("");
                        currentProductImageUrls.add(url2);
                    } else {
                        currentProductImageUrls.set(1, url2);
                    }
                }

                progressDialog.dismiss();
                saveProductData(title, price, rating, time, detail, ingredients, available, currentProductImageUrls);
            });
        });
    }

    private interface ImageUploadCallback {
        void onUpload(String downloadUrl);
    }

    private void uploadSingleImage(Uri uri, StorageReference storageRef, ImageUploadCallback callback) {
        if (uri == null) {
            callback.onUpload(null);
            return;
        }

        StorageReference fileRef = storageRef.child(System.currentTimeMillis() + "_" + java.util.UUID.randomUUID().toString() + ".jpg");
        fileRef.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(downloadUri -> callback.onUpload(downloadUri.toString())).addOnFailureListener(e -> callback.onUpload(null)))
                .addOnFailureListener(e -> {
                    if (isAdded()) Toast.makeText(getContext(), "Image upload failed", Toast.LENGTH_SHORT).show();
                    callback.onUpload(null);
                });
    }

    private void saveProductData(String title, double price, String rating, String time, String detail, String ingredients, boolean available, List<String> imageUrls) {
        Map<String, Object> data = new HashMap<>();
        data.put("foodTitle",    title);
        data.put("productPrice", price);
        data.put("foodRating",   rating);
        data.put("foodTime",     time);
        data.put("foodDetail",   detail);
        data.put("ingrideint",   ingredients);
        data.put("categoryId",   selectedCategoryId);
        data.put("availability", available);
        data.put("productImage", imageUrls);

        FirebaseFirestore.getInstance()
                .collection("products")
                .document(productId)
                .update(data)
                .addOnSuccessListener(v -> {
                    if (!isAdded()) return;
                    Toast.makeText(getContext(), "Product saved!", Toast.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    Toast.makeText(getContext(), "Save failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void setAvailabilityLabel(boolean available) {
        tvAvailabilityLabel.setText(available ? "Available" : "Not Available");
        tvAvailabilityLabel.setTextColor(Color.parseColor(available ? "#2EBA63" : "#F44336"));
    }
}