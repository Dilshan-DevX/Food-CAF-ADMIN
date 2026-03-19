package com.codex.adminfoodcaf.adapter;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codex.adminfoodcaf.R;
import com.codex.adminfoodcaf.fragment.SingleProductFragment;
import com.codex.adminfoodcaf.model.Product;

import java.util.List;

public class AdminProductAdapter extends RecyclerView.Adapter<AdminProductAdapter.ViewHolder> {

    private List<Product> productList;
    private OnProductClickListener listener;

    public AdminProductAdapter(List<Product> productList, OnProductClickListener listener) {
        this.productList = productList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);

        // ✅ itemName — product name
        holder.itemName.setText(product.getFoodTitle() != null ? product.getFoodTitle() : "N/A");

        // ✅ tvAvailability — Available / Not Available + color
        if (product.isAvailability()) {
            holder.tvAvailability.setText("Available");
            holder.tvAvailability.setTextColor(Color.parseColor("#2EBA63"));
        } else {
            holder.tvAvailability.setText("Not Available");
            holder.tvAvailability.setTextColor(Color.parseColor("#F44336"));
        }

        // ✅ Rating — foodRating
        holder.Rating.setText(product.getFoodRating() != null ? product.getFoodRating() : "-");

        // ✅ tvTime — foodTime
        holder.tvTime.setText(product.getFoodTime() != null ? product.getFoodTime() : "-");

        // ✅ tvTotalPrice — productPrice
        holder.tvTotalPrice.setText(String.format("LKR %.2f", product.getProductPrice()));

        // ✅ imgRestaurant — productImage list eke pahala wena image (index 0)
        if (product.getProductImage() != null && !product.getProductImage().isEmpty()) {
            Glide.with(holder.imgRestaurant.getContext())
                    .load(product.getProductImage().get(0))
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .centerCrop()
                    .into(holder.imgRestaurant);
        } else {
            holder.imgRestaurant.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // ✅ Click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onProductClick(product);

//            Bundle bundle = new Bundle();
//            bundle.putString("productId", product.getProductId()); // Product ID pass kartoy
//
//            SingleProductFragment singleProductFragment = new SingleProductFragment();
//            singleProductFragment.setArguments(bundle);
//
//            requireActivity().getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.fragmentContainer, singleProductFragment)
//                    .addToBackStack(null)
//                    .commit();
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemName, tvAvailability, Rating, tvTime, tvTotalPrice;
        ImageView imgRestaurant;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName       = itemView.findViewById(R.id.itemName);
            tvAvailability = itemView.findViewById(R.id.tvAvailability);
            Rating         = itemView.findViewById(R.id.Rating);
            tvTime         = itemView.findViewById(R.id.tvTime);
            tvTotalPrice   = itemView.findViewById(R.id.tvTotalPrice);
            imgRestaurant  = itemView.findViewById(R.id.imgRestaurant);
        }
    }

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }
}