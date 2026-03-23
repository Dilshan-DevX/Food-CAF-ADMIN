package com.codex.adminfoodcaf.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.codex.adminfoodcaf.R;
import com.codex.adminfoodcaf.adapter.AdminProductAdapter;
import com.codex.adminfoodcaf.databinding.FragmentHomeBinding;
import com.codex.adminfoodcaf.model.Order;
import com.codex.adminfoodcaf.model.Product;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private FirebaseFirestore db;

    public HomeFragment() {
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

        loadStats();
        loadProducts();
    }

    private void loadStats() {

        db.collection("orders").addSnapshotListener((orderSnap, e) -> {
            if (e != null || binding == null || !isAdded() || orderSnap == null)
                return;

            int totalOrders = orderSnap.size();
            binding.orderCount.setText(String.valueOf(totalOrders));

            int salesCount = 0;
            int pendingCount = 0;
            double revenue = 0.0;

            List<Order> orderList = orderSnap.toObjects(Order.class);

            for (Order order : orderList) {

                boolean isPaid = "true".equalsIgnoreCase(order.getStatus())
                        || "Paid".equalsIgnoreCase(order.getStatus())
                        || "Delivered".equalsIgnoreCase(order.getStatus());

                if (isPaid) {
                    salesCount++;

                    if (order.getOrderItems() != null) {
                        for (Order.OrderItem item : order.getOrderItems()) {
                            revenue += item.getTotalPrice();
                        }
                    }
                } else {
                    pendingCount++;
                }
            }

            binding.salesCount.setText(String.valueOf(salesCount));
            binding.pendingPayment.setText(String.valueOf(pendingCount));
            binding.totalRevenue.setText(String.format("LKR %.0f", revenue));
        });


        db.collection("products").addSnapshotListener((productSnap, e) -> {
            if (e != null || binding == null || !isAdded() || productSnap == null)
                return;
            binding.productCount.setText(String.valueOf(productSnap.size()));
        });

        db.collection("users").addSnapshotListener((userSnap, e) -> {
            if (e != null || binding == null || !isAdded() || userSnap == null)
                return;
            binding.userCount.setText(String.valueOf(userSnap.size()));
        });
    }


    private void loadProducts() {
        binding.rvAdminList.setLayoutManager(new LinearLayoutManager(getContext()));
        db.collection("products").addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (e != null || binding == null || !isAdded() || queryDocumentSnapshots == null)
                return;

            List<Product> productList = new java.util.ArrayList<>();
            for (com.google.firebase.firestore.QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                Product p = doc.toObject(Product.class);
                p.setProductId(doc.getId());
                productList.add(p);
            }

            AdminProductAdapter adapter = new AdminProductAdapter(productList, product -> {
                if (binding == null || !isAdded())
                    return;

                Bundle bundle = new Bundle();
                bundle.putString("productId", product.getProductId());

                SingleProductFragment singleProductFragment = new SingleProductFragment();
                singleProductFragment.setArguments(bundle);

                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, singleProductFragment)
                        .addToBackStack(null)
                        .commit();
            });

            binding.rvAdminList.setAdapter(adapter);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}