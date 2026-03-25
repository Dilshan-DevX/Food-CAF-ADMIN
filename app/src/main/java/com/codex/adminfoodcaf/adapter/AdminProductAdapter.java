package com.codex.adminfoodcaf.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codex.adminfoodcaf.R;
import com.codex.adminfoodcaf.model.Product;

import java.util.ArrayList;
import java.util.List;

public class AdminProductAdapter extends RecyclerView.Adapter<AdminProductAdapter.ViewHolder> {

    private List<Product> productList;
    private List<Product> originalList;
    private OnProductClickListener listener;

    public AdminProductAdapter(List<Product> productList, OnProductClickListener listener) {
        this.productList = new ArrayList<>(productList);
        this.originalList = new ArrayList<>(productList);
        this.listener = listener;
    }

    /** Update the entire dataset (called after fresh Firestore snapshot) */
    public void updateList(List<Product> newList) {
        this.originalList = new ArrayList<>(newList);
        this.productList = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    /** Filter by product name — pass null or empty to reset. Exact/Prefix matches appear top. */
    public void filter(String query) {
        productList.clear();
        if (query == null || query.trim().isEmpty()) {
            productList.addAll(originalList);
        } else {
            String lower = query.toLowerCase().trim();
            List<Product> topMatches = new ArrayList<>();
            List<Product> otherMatches = new ArrayList<>();
            
            for (Product p : originalList) {
                if (p.getFoodTitle() != null) {
                    String titleLower = p.getFoodTitle().toLowerCase();
                    if (titleLower.equals(lower) || titleLower.startsWith(lower)) {
                        topMatches.add(p);
                    } else if (titleLower.contains(lower)) {
                        otherMatches.add(p);
                    }
                }
            }
            productList.addAll(topMatches);
            productList.addAll(otherMatches);
        }
        notifyDataSetChanged();
    }

    public List<Product> getOriginalList() {
        return originalList;
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
        holder.itemName.setText(product.getFoodTitle() != null ? product.getFoodTitle() : "N/A");
        if (product.isAvailability()) {
            holder.tvAvailability.setText("Available");
            holder.tvAvailability.setTextColor(Color.parseColor("#2EBA63"));
        } else {
            holder.tvAvailability.setText("Not Available");
            holder.tvAvailability.setTextColor(Color.parseColor("#F44336"));
        }

        holder.Rating.setText(product.getFoodRating() != null ? product.getFoodRating() : "-");
        holder.tvTime.setText(product.getFoodTime() != null ? product.getFoodTime() : "-");
        holder.tvTotalPrice.setText(String.format("LKR %.2f", product.getProductPrice()));

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

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onProductClick(product);
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