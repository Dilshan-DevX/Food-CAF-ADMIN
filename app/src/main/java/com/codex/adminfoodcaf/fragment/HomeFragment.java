package com.codex.adminfoodcaf.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.codex.adminfoodcaf.R;
import com.codex.adminfoodcaf.adapter.HomeCategoryAdapter;
import com.codex.adminfoodcaf.adapter.HomeProductAdapter;
import com.codex.adminfoodcaf.databinding.FragmentHomeBinding;
import com.codex.adminfoodcaf.model.Category;
import com.codex.adminfoodcaf.model.Product;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private FirebaseFirestore db;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Category data load cheyadaniki function call
        loadCategories();

        // Product data load cheyadaniki function call
        loadProducts();
    }

    private void loadCategories() {
        db.collection("categories").get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                List<Category> categoryList = queryDocumentSnapshots.toObjects(Category.class);

                // Horizontal list kosam LinearLayoutManager vaadam
                binding.rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
                HomeCategoryAdapter categoryAdapter = new HomeCategoryAdapter(categoryList, getContext());
                binding.rvCategories.setAdapter(categoryAdapter);
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to load categories", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadProducts() {
        db.collection("products").get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                List<Product> productList = queryDocumentSnapshots.toObjects(Product.class);

                binding.rvProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));

                // 🔴 මෙතනදී තමයි Click Listener එක පාස් කරන්නේ
                HomeProductAdapter productAdapter = new HomeProductAdapter(productList, product -> {
                    
                    if(product.getProductId() != null){
                        // කෑමක් ක්ලික් කළාම SingleProductFragment එකට යනවා
                        Bundle bundle = new Bundle();
                        bundle.putString("productId", product.getProductId());

                        SingleProductFragment fragment = new SingleProductFragment();
                        fragment.setArguments(bundle);

                        requireActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragmentContainer, fragment)
                                .addToBackStack(null)
                                .commit();
                    }
                });

                binding.rvProducts.setAdapter(productAdapter);
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to load products", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}