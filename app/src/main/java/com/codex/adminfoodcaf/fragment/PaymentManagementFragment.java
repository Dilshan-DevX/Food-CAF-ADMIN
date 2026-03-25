package com.codex.adminfoodcaf.fragment;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codex.adminfoodcaf.R;
import com.codex.adminfoodcaf.model.Order;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class PaymentManagementFragment extends Fragment {

    private TextView tvPendingCount, tvCompletedCount, tvPendingLabel, tvRefresh;
    private RecyclerView rvPendingPayments;
    private PendingPaymentAdapter adapter;
    private ListenerRegistration  ordersListener;

    public PaymentManagementFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_payment_management, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvPendingCount    = view.findViewById(R.id.tvPendingCount);
        tvCompletedCount  = view.findViewById(R.id.tvCompletedCount);
        tvPendingLabel    = view.findViewById(R.id.tvPendingLabel);
        tvRefresh         = view.findViewById(R.id.tvRefresh);
        rvPendingPayments = view.findViewById(R.id.rvPendingPayments);

        rvPendingPayments.setLayoutManager(new LinearLayoutManager(getContext()));

        tvRefresh.setOnClickListener(v -> {
            if (ordersListener != null) ordersListener.remove();
            listenPayments();
        });

        listenPayments();
    }

    private void listenPayments() {
        ordersListener = FirebaseFirestore.getInstance()
                .collection("orders")
                .addSnapshotListener((snap, e) -> {
                    if (e != null || snap == null || !isAdded()) return;

                    List<Order> allOrders = new ArrayList<>();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : snap.getDocuments()) {
                        Order order = doc.toObject(Order.class);
                        if (order != null) {
                            order.setOrderId(doc.getId());
                            allOrders.add(order);
                        }
                    }

                    List<Order> pendingList = new ArrayList<>();
                    int completedCount = 0;

                    for (Order order : allOrders) {
                        boolean isPaid = "Paid".equalsIgnoreCase(order.getStatus())
                                || "Delivered".equalsIgnoreCase(order.getStatus())
                                || "true".equalsIgnoreCase(order.getStatus());
                        if (isPaid) completedCount++;
                        else pendingList.add(order);
                    }

                    tvPendingCount.setText(String.valueOf(pendingList.size()));
                    tvCompletedCount.setText(String.valueOf(completedCount));
                    tvPendingLabel.setText(pendingList.size() + " orders");

                    adapter = new PendingPaymentAdapter(pendingList, (order, position) ->
                            confirmMarkPaid(order, position));
                    rvPendingPayments.setAdapter(adapter);
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (ordersListener != null) ordersListener.remove();
    }


    private void confirmMarkPaid(Order order, int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Mark as Paid")
                .setMessage("Mark Order #" + order.getOrderId() + " as Paid?")
                .setPositiveButton("Yes, Mark Paid", (dialog, which) -> {
                    FirebaseFirestore.getInstance()
                            .collection("orders")
                            .document(order.getOrderId())
                            .update("status", "Paid")
                            .addOnSuccessListener(v -> {
                                if (!isAdded()) return;
                                Toast.makeText(getContext(),
                                        "Order #" + order.getOrderId() + " marked as Paid!",
                                        Toast.LENGTH_SHORT).show();
                                // Real-time listener automatic refresh karanawa — manual call nehne
                            })
                            .addOnFailureListener(e -> {
                                if (!isAdded()) return;
                                Toast.makeText(getContext(),
                                        "Failed: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    static class PendingPaymentAdapter
            extends RecyclerView.Adapter<PendingPaymentAdapter.VH> {

        interface OnMarkPaidListener {
            void onMarkPaid(Order order, int position);
        }

        private final List<Order>          list;
        private final OnMarkPaidListener   listener;

        PendingPaymentAdapter(List<Order> list, OnMarkPaidListener listener) {
            this.list     = list;
            this.listener = listener;
        }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_pending_payment, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            Order o = list.get(pos);


            h.tvOrderId.setText("#" + (o.getOrderId() != null ? o.getOrderId() : "-"));


            String status = o.getStatus() != null ? o.getStatus() : "Pending";
            h.chipStatus.setText(status);

            if (o.getDeliveryAddress() != null) {
                String name    = o.getDeliveryAddress().getName();
                String contact = o.getDeliveryAddress().getContactNum();
                h.tvUserName.setText(name != null ? name : "Unknown");
                h.tvUserContact.setText(contact != null ? contact : "-");
            } else {
                h.tvUserName.setText("Unknown");
                h.tvUserContact.setText("-");
            }


            h.tvDate.setText(o.getOrderDate() != null ? o.getOrderDate() : "-");

            double total = 0;
            if (o.getOrderItems() != null)
                for (Order.OrderItem item : o.getOrderItems())
                    total += item.getTotalPrice();
            total += 100; // delivery fee
            h.tvTotal.setText(String.format("LKR %,.2f", total));

            h.btnMarkPaid.setOnClickListener(v -> {
                if (listener != null) listener.onMarkPaid(o, h.getAdapterPosition());
            });
        }

        @Override public int getItemCount() { return list.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView       tvOrderId, tvUserName, tvUserContact, tvTotal, tvDate;
            Chip           chipStatus;
            MaterialButton btnMarkPaid;

            VH(@NonNull View v) {
                super(v);
                tvOrderId = v.findViewById(R.id.tvPayOrderId);
                tvUserName = v.findViewById(R.id.tvPayUserName);
                tvUserContact  = v.findViewById(R.id.tvPayUserContact);
                tvTotal = v.findViewById(R.id.tvPayTotal);
                tvDate = v.findViewById(R.id.tvPayDate);
                chipStatus = v.findViewById(R.id.chipPayStatus);
                btnMarkPaid= v.findViewById(R.id.btnMarkPaid);
            }
        }
    }
}