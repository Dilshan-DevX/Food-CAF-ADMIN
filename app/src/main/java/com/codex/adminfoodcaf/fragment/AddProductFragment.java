package com.codex.adminfoodcaf.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.codex.adminfoodcaf.databinding.FragmentAddProductBinding;
import com.codex.adminfoodcaf.model.Category;
import com.codex.adminfoodcaf.model.Product;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AddProductFragment extends Fragment {

    private FragmentAddProductBinding binding;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    private Uri imageUri1;
    private Uri imageUri2;

    private List<Category> categoryList = new ArrayList<>();
    private String selectedCategoryId = "";

    private ActivityResultLauncher<Intent> picker1;
    private ActivityResultLauncher<Intent> picker2;

    public AddProductFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        picker1 = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        imageUri1 = result.getData().getData();
                        binding.imgProduct.setPadding(0, 0, 0, 0);
                        binding.imgProduct.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
                        binding.imgProduct.setImageURI(imageUri1);
                    }
                }
        );

        picker2 = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        imageUri2 = result.getData().getData();
                        binding.imgProduct2.setPadding(0, 0, 0, 0);
                        binding.imgProduct2.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
                        binding.imgProduct2.setImageURI(imageUri2);
                    }
                }
        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAddProductBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        String generatedProductId = "P" + System.currentTimeMillis();
        binding.ProductId.setText(generatedProductId);
        
        loadCategories();

        binding.imgProduct.setOnClickListener(v -> openGallery(picker1));
        binding.imgProduct2.setOnClickListener(v -> openGallery(picker2));

        binding.btnUpdate.setText("ADD PRODUCT");
        binding.btnUpdate.setOnClickListener(v -> saveProduct());
    }

    private void openGallery(ActivityResultLauncher<Intent> picker) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        picker.launch(intent);
    }

    private void loadCategories() {
        db.collection("categories").get().addOnSuccessListener(queryDocumentSnapshots -> {
            categoryList.clear();
            List<String> categoryNames = new ArrayList<>();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                Category category = doc.toObject(Category.class);
                if (category.getCategoryId() == null || category.getCategoryId().isEmpty()) {
                    category.setCategoryId(doc.getId());
                }
                categoryList.add(category);
                categoryNames.add(category.getCategoryName() != null ? category.getCategoryName() : "Unknown");
            }
            
            if (getContext() != null) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, categoryNames);
                binding.dropdownCategory.setAdapter(adapter);
                binding.dropdownCategory.setOnItemClickListener((parent, view, position, id) -> {
                    selectedCategoryId = categoryList.get(position).getCategoryId();
                });
            }
        });
    }

    private void saveProduct() {
        String name = binding.etFoodTitle.getText().toString().trim();
        String priceStr = binding.etPrice.getText().toString().trim();
        String productId = binding.ProductId.getText().toString().trim();
        String rating = binding.etRating.getText().toString().trim();
        String prepTime = binding.etFoodTime.getText().toString().trim();
        String description = binding.etFoodDetail.getText().toString().trim();
        boolean availability = binding.switchAvailability.isChecked();

        if (name.isEmpty() || priceStr.isEmpty() || productId.isEmpty()) {
            Toast.makeText(getContext(), "Name, Price, and Product ID cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedCategoryId.isEmpty()) {
            Toast.makeText(getContext(), "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid price", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnUpdate.setEnabled(false);
        binding.btnUpdate.setText("Uploading...");

        List<String> imageUrls = new ArrayList<>();
        uploadImage(imageUri1, productId, imageUrls, () -> uploadImage(imageUri2, productId, imageUrls, () -> {
            Product product = new Product();
            product.setProductId(productId);
            product.setCategoryId(selectedCategoryId);
            product.setFoodTitle(name);
            product.setProductPrice(price);
            product.setFoodRating(rating);
            product.setFoodTime(prepTime);
            product.setFoodDetail(description);
            product.setAvailability(availability);
            product.setProductImage(imageUrls);

            db.collection("products").document(productId).set(product)
                    .addOnSuccessListener(aVoid -> {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Product added successfully", Toast.LENGTH_SHORT).show();
                        }
                        binding.btnUpdate.setEnabled(true);
                        binding.btnUpdate.setText("ADD PRODUCT");
                        clearFields();
                    })
                    .addOnFailureListener(e -> {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        binding.btnUpdate.setEnabled(true);
                        binding.btnUpdate.setText("ADD PRODUCT");
                    });
        }));
    }

    private void clearFields() {
        binding.etFoodTitle.setText("");
        binding.etPrice.setText("");
        binding.etRating.setText("");
        binding.etFoodTime.setText("");
        binding.etFoodDetail.setText("");
        binding.dropdownCategory.setText("", false);
        selectedCategoryId = "";
        
        int paddingPx = (int) (48 * getResources().getDisplayMetrics().density);
        binding.imgProduct.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
        binding.imgProduct.setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER);
        binding.imgProduct.setImageResource(com.codex.adminfoodcaf.R.drawable.addproduct);
        
        binding.imgProduct2.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
        binding.imgProduct2.setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER);
        binding.imgProduct2.setImageResource(com.codex.adminfoodcaf.R.drawable.addproduct);
        
        imageUri1 = null;
        imageUri2 = null;
        String generatedProductId = "P" + System.currentTimeMillis();
        binding.ProductId.setText(generatedProductId);
    }

    private void uploadImage(Uri imageUri, String productId, List<String> imageUrls, Runnable onSuccess) {
        if (imageUri == null) {
            onSuccess.run();
            return;
        }
        StorageReference ref = storage.getReference().child("product-images/" + productId + "/" + UUID.randomUUID().toString());
        ref.putFile(imageUri).addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
            imageUrls.add(uri.toString());
            onSuccess.run();
        })).addOnFailureListener(e -> {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            onSuccess.run();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}