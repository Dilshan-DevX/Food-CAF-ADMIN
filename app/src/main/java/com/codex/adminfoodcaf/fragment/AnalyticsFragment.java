package com.codex.adminfoodcaf.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codex.adminfoodcaf.R;
import com.codex.adminfoodcaf.model.Order;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AnalyticsFragment extends Fragment {

    private TextView        tvTotalRevenue, tvTotalOrders, tvCompletedOrders,
            tvPendingOrders, tvReportDate, tvTopCount;
    private RecyclerView    rvTopProducts, rvRecentOrders;
    private MaterialCardView cardManagePayment, cardAddProduct;

    // ── Cached data for print ──────────────────────────────────────────────────
    private List<Order> cachedOrders = new ArrayList<>();

    public AnalyticsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_analytics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvTotalRevenue    = view.findViewById(R.id.tvTotalRevenue);
        tvTotalOrders     = view.findViewById(R.id.tvTotalOrders);
        tvCompletedOrders = view.findViewById(R.id.tvCompletedOrders);
        tvPendingOrders   = view.findViewById(R.id.tvPendingOrders);
        tvReportDate      = view.findViewById(R.id.tvReportDate);
        tvTopCount        = view.findViewById(R.id.tvTopCount);
        rvTopProducts     = view.findViewById(R.id.rvTopProducts);
        rvRecentOrders    = view.findViewById(R.id.rvRecentOrders);
        cardManagePayment = view.findViewById(R.id.cardManagePayment);
        cardAddProduct    = view.findViewById(R.id.cardAddProduct);

        rvTopProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRecentOrders.setLayoutManager(new LinearLayoutManager(getContext()));

        // ── Report date ───────────────────────────────────────────────────────
        String today = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());
        tvReportDate.setText("Report as of " + today);

        // ── Navigation shortcuts ──────────────────────────────────────────────
        cardManagePayment.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new PaymentManagementFragment())
                    .addToBackStack(null)
                    .commit();
        });

        cardAddProduct.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new AddProductFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // ── Print button ──────────────────────────────────────────────────────
        view.findViewById(R.id.btnPrintReport).setOnClickListener(v -> printReport());

        // ── Load data ─────────────────────────────────────────────────────────
        loadAnalytics();
    }

    // ── Load all orders → compute analytics ───────────────────────────────────
    private void loadAnalytics() {
        FirebaseFirestore.getInstance()
                .collection("orders")
                .get()
                .addOnSuccessListener(snap -> {
                    if (!isAdded()) return;

                    cachedOrders = snap.toObjects(Order.class);

                    int total     = cachedOrders.size();
                    int completed = 0;
                    int pending   = 0;
                    double revenue = 0;

                    // product name → {qty, revenue} map for top sellers
                    Map<String, int[]> productStats = new HashMap<>();
                    // int[0] = qty, int[1] = revenue*100 (to avoid double key)

                    for (Order order : cachedOrders) {
                        boolean isPaid = "Paid".equalsIgnoreCase(order.getStatus())
                                || "Delivered".equalsIgnoreCase(order.getStatus())
                                || "true".equalsIgnoreCase(order.getStatus());

                        if (isPaid) {
                            completed++;
                            if (order.getOrderItems() != null) {
                                for (Order.OrderItem item : order.getOrderItems()) {
                                    revenue += item.getTotalPrice();

                                    // top products tracking
                                    String pName = item.getProductName() != null
                                            ? item.getProductName() : "Unknown";
                                    int[] stats = productStats.getOrDefault(pName, new int[]{0, 0});
                                    stats[0] += item.getQty();
                                    stats[1] += (int) item.getTotalPrice();
                                    productStats.put(pName, stats);
                                }
                            }
                        } else {
                            pending++;
                        }
                    }

                    // ── Stats cards ───────────────────────────────────────────
                    tvTotalOrders.setText(String.valueOf(total));
                    tvCompletedOrders.setText(String.valueOf(completed));
                    tvPendingOrders.setText(String.valueOf(pending));
                    tvTotalRevenue.setText(String.format("LKR %,.0f", revenue));

                    // ── Top 5 products ────────────────────────────────────────
                    List<Map.Entry<String, int[]>> topList = new ArrayList<>(productStats.entrySet());
                    topList.sort((a, b) -> b.getValue()[0] - a.getValue()[0]); // sort by qty desc
                    if (topList.size() > 5) topList = topList.subList(0, 5);

                    tvTopCount.setText(topList.size() + " products");
                    rvTopProducts.setAdapter(new TopProductAdapter(topList));

                    // ── Recent 10 orders ──────────────────────────────────────
                    List<Order> recent = new ArrayList<>(cachedOrders);
                    recent.sort((a, b) -> {
                        if (a.getOrderDate() == null || b.getOrderDate() == null) return 0;
                        return b.getOrderDate().compareTo(a.getOrderDate());
                    });
                    if (recent.size() > 10) recent = recent.subList(0, 10);
                    rvRecentOrders.setAdapter(new RecentOrderAdapter(recent));
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    Toast.makeText(getContext(), "Failed: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    // ── Print Report — Android PrintManager use karala HTML report print karanawa
    private void printReport() {
        if (cachedOrders.isEmpty()) {
            Toast.makeText(getContext(), "No data to print", Toast.LENGTH_SHORT).show();
            return;
        }

        String html = buildReportHtml();

        WebView webView = new WebView(requireContext());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                PrintManager printManager = (PrintManager)
                        requireActivity().getSystemService(android.content.Context.PRINT_SERVICE);

                String today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
                String jobName = "SalesReport_" + today;

                PrintAttributes attrs = new PrintAttributes.Builder()
                        .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                        .setResolution(new PrintAttributes.Resolution("pdf", "pdf", 600, 600))
                        .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                        .build();

                printManager.print(jobName,
                        webView.createPrintDocumentAdapter(jobName), attrs);
            }
        });

        webView.loadDataWithBaseURL(null, html, "text/HTML", "UTF-8", null);
    }

    // ── Build HTML report string ───────────────────────────────────────────────
    private String buildReportHtml() {
        String today = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(new Date());

        int total     = cachedOrders.size();
        int completed = 0, pending = 0;
        double revenue = 0;
        Map<String, int[]> productStats = new HashMap<>();

        for (Order order : cachedOrders) {
            boolean isPaid = "Paid".equalsIgnoreCase(order.getStatus())
                    || "Delivered".equalsIgnoreCase(order.getStatus())
                    || "true".equalsIgnoreCase(order.getStatus());
            if (isPaid) {
                completed++;
                if (order.getOrderItems() != null) {
                    for (Order.OrderItem item : order.getOrderItems()) {
                        revenue += item.getTotalPrice();
                        String pName = item.getProductName() != null ? item.getProductName() : "Unknown";
                        int[] s = productStats.getOrDefault(pName, new int[]{0, 0});
                        s[0] += item.getQty();
                        s[1] += (int) item.getTotalPrice();
                        productStats.put(pName, s);
                    }
                }
            } else pending++;
        }

        List<Map.Entry<String, int[]>> topList = new ArrayList<>(productStats.entrySet());
        topList.sort((a, b) -> b.getValue()[0] - a.getValue()[0]);

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset='UTF-8'>")
                .append("<style>")
                .append("body{font-family:sans-serif;margin:32px;color:#222}")
                .append("h1{color:#1A3D2B;font-size:24px;margin-bottom:4px}")
                .append(".sub{color:#888;font-size:12px;margin-bottom:24px}")
                .append(".stats{display:flex;gap:16px;margin-bottom:24px}")
                .append(".stat-box{flex:1;background:#f5f5f5;border-radius:10px;padding:14px;text-align:center}")
                .append(".stat-val{font-size:22px;font-weight:bold;color:#1A3D2B}")
                .append(".stat-lbl{font-size:11px;color:#888;margin-top:4px}")
                .append("table{width:100%;border-collapse:collapse;margin-top:8px}")
                .append("th{background:#1A3D2B;color:#fff;padding:10px;text-align:left;font-size:12px}")
                .append("td{padding:9px;border-bottom:1px solid #eee;font-size:12px}")
                .append("tr:nth-child(even) td{background:#f9f9f9}")
                .append(".section-title{font-size:16px;font-weight:bold;margin:24px 0 8px}")
                .append(".badge{display:inline-block;padding:2px 8px;border-radius:8px;font-size:10px;font-weight:bold}")
                .append(".paid{background:#d4edda;color:#155724}.pending{background:#fff3cd;color:#856404}")
                .append("</style></head><body>");

        sb.append("<h1>&#x1F4CA; Sales Report</h1>")
                .append("<div class='sub'>Generated: ").append(today).append("</div>");

        // Stats boxes
        sb.append("<div class='stats'>")
                .append("<div class='stat-box'><div class='stat-val'>").append(total).append("</div><div class='stat-lbl'>TOTAL ORDERS</div></div>")
                .append("<div class='stat-box'><div class='stat-val'>").append(completed).append("</div><div class='stat-lbl'>COMPLETED</div></div>")
                .append("<div class='stat-box'><div class='stat-val'>").append(pending).append("</div><div class='stat-lbl'>PENDING</div></div>")
                .append("<div class='stat-box'><div class='stat-val' style='color:#1A3D2B'>LKR ").append(String.format("%,.0f", revenue)).append("</div><div class='stat-lbl'>REVENUE</div></div>")
                .append("</div>");

        // Top products table
        sb.append("<div class='section-title'>Top Selling Products</div>")
                .append("<table><tr><th>#</th><th>Product</th><th>Qty Sold</th><th>Revenue (LKR)</th></tr>");
        int rank = 1;
        for (Map.Entry<String, int[]> e : topList) {
            sb.append("<tr><td>").append(rank++).append("</td>")
                    .append("<td>").append(e.getKey()).append("</td>")
                    .append("<td>").append(e.getValue()[0]).append("</td>")
                    .append("<td>").append(String.format("%,d", e.getValue()[1])).append("</td></tr>");
        }
        sb.append("</table>");

        // Recent orders table
        List<Order> recent = new ArrayList<>(cachedOrders);
        recent.sort((a, b) -> {
            if (a.getOrderDate() == null || b.getOrderDate() == null) return 0;
            return b.getOrderDate().compareTo(a.getOrderDate());
        });
        if (recent.size() > 20) recent = recent.subList(0, 20);

        sb.append("<div class='section-title'>Recent Orders</div>")
                .append("<table><tr><th>Order ID</th><th>Date</th><th>Status</th><th>Total (LKR)</th></tr>");
        for (Order o : recent) {
            double orderTotal = 0;
            if (o.getOrderItems() != null)
                for (Order.OrderItem it : o.getOrderItems()) orderTotal += it.getTotalPrice();
            boolean isPaid = "Paid".equalsIgnoreCase(o.getStatus())
                    || "Delivered".equalsIgnoreCase(o.getStatus());
            String badge = isPaid
                    ? "<span class='badge paid'>" + o.getStatus() + "</span>"
                    : "<span class='badge pending'>" + (o.getStatus() != null ? o.getStatus() : "Pending") + "</span>";
            sb.append("<tr>")
                    .append("<td>#").append(o.getOrderId() != null ? o.getOrderId() : "-").append("</td>")
                    .append("<td>").append(o.getOrderDate() != null ? o.getOrderDate() : "-").append("</td>")
                    .append("<td>").append(badge).append("</td>")
                    .append("<td>").append(String.format("%,.0f", orderTotal)).append("</td>")
                    .append("</tr>");
        }
        sb.append("</table></body></html>");

        return sb.toString();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Inner Adapters
    // ══════════════════════════════════════════════════════════════════════════

    // ── Top Products Adapter ──────────────────────────────────────────────────
    static class TopProductAdapter extends RecyclerView.Adapter<TopProductAdapter.VH> {
        private final List<Map.Entry<String, int[]>> list;
        TopProductAdapter(List<Map.Entry<String, int[]>> list) { this.list = list; }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new VH(LayoutInflater.from(p.getContext())
                    .inflate(R.layout.item_top_product, p, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            Map.Entry<String, int[]> e = list.get(pos);
            h.tvRank.setText(String.valueOf(pos + 1));
            h.tvName.setText(e.getKey());
            h.tvQty.setText(e.getValue()[0] + " sold");
            h.tvRev.setText("LKR " + String.format("%,d", e.getValue()[1]));
        }

        @Override public int getItemCount() { return list.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvRank, tvName, tvQty, tvRev;
            VH(@NonNull View v) {
                super(v);
                tvRank = v.findViewById(R.id.tvRank);
                tvName = v.findViewById(R.id.tvProductName);
                tvQty  = v.findViewById(R.id.tvQtySold);
                tvRev  = v.findViewById(R.id.tvProductRevenue);
            }
        }
    }

    // ── Recent Orders Adapter ─────────────────────────────────────────────────
    static class RecentOrderAdapter extends RecyclerView.Adapter<RecentOrderAdapter.VH> {
        private final List<Order> list;
        RecentOrderAdapter(List<Order> list) { this.list = list; }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new VH(LayoutInflater.from(p.getContext())
                    .inflate(R.layout.item_recent_order, p, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            Order o = list.get(pos);
            h.tvId.setText("#" + (o.getOrderId() != null ? o.getOrderId() : "-"));
            h.tvDate.setText(o.getOrderDate() != null ? o.getOrderDate() : "-");

            double total = 0;
            if (o.getOrderItems() != null)
                for (Order.OrderItem it : o.getOrderItems()) total += it.getTotalPrice();
            h.tvTotal.setText(String.format("LKR %,.0f", total));

            boolean isPaid = "Paid".equalsIgnoreCase(o.getStatus())
                    || "Delivered".equalsIgnoreCase(o.getStatus());
            h.chipStatus.setText(o.getStatus() != null ? o.getStatus() : "Pending");
            h.chipStatus.setChipBackgroundColorResource(
                    isPaid ? android.R.color.holo_green_light : android.R.color.holo_orange_light);
        }

        @Override public int getItemCount() { return list.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvId, tvDate, tvTotal;
            Chip chipStatus;
            VH(@NonNull View v) {
                super(v);
                tvId       = v.findViewById(R.id.tvOrderId);
                tvDate     = v.findViewById(R.id.tvOrderDate);
                tvTotal    = v.findViewById(R.id.tvOrderTotal);
                chipStatus = v.findViewById(R.id.chipOrderStatus);
            }
        }
    }
}