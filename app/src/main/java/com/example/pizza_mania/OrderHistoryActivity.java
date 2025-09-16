package com.example.pizza_mania;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderHistoryActivity extends AppCompatActivity {

    private RecyclerView rvOrderHistory;
    private OrderAdapter orderAdapter;
    private List<Order> orderList;
    private FirebaseFirestore db;
    DocumentReference cusRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        rvOrderHistory = findViewById(R.id.rvOrderHistory);
        rvOrderHistory.setLayoutManager(new LinearLayoutManager(this));

        orderList = new ArrayList<>();
        orderAdapter = new OrderAdapter(orderList);
        rvOrderHistory.setAdapter(orderAdapter);

        db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser cus = auth.getCurrentUser();
        cusRef = db.document("Customer/"+cus.getUid());

        loadOrders();
    }

    private void loadOrders() {
        db.collection("Order")
                .whereEqualTo("cusID", cusRef)
                .get()
                .addOnSuccessListener((QuerySnapshot queryDocumentSnapshots) -> {
                    orderList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String orderId = doc.getId();
                        Timestamp timestamp = doc.getTimestamp("orderDateTime");
                        String orderDate = timestamp != null ? formatTimestamp(timestamp) : "";
                        String orderStatus = doc.getString("orderStatus");
                        Double totalBill = doc.getDouble("totalBill");
                        List<DocumentReference> menuItemRefs = (List<DocumentReference>) doc.get("menuItem");

                        Order order = new Order(orderId, orderDate, orderStatus, totalBill, menuItemRefs);
                        orderList.add(order);

                        // Fetch menu item names for each order
                        fetchMenuItems(order);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(OrderHistoryActivity.this, "Failed to load orders", Toast.LENGTH_SHORT).show();
                    Log.e("OrderHistory", "Error fetching orders", e);
                });
    }

    private void fetchMenuItems(Order order) {
        if (order.menuItemRefs == null || order.menuItemRefs.isEmpty()) {
            order.setMenuItemsDisplay("No items");
            orderAdapter.notifyDataSetChanged();
            return;
        }

        StringBuilder itemsDisplay = new StringBuilder();
        for (int i = 0; i < order.menuItemRefs.size(); i++) {
            DocumentReference ref = order.menuItemRefs.get(i);
            int finalI = i;
            ref.get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    String itemName = doc.getString("itemName");
                    if (itemName != null) {
                        if (itemsDisplay.length() > 0) itemsDisplay.append(", ");
                        itemsDisplay.append(itemName);
                    }
                }
                // Once last item is fetched, update order display
                if (finalI == order.menuItemRefs.size() - 1) {
                    order.setMenuItemsDisplay(itemsDisplay.toString());
                    orderAdapter.notifyDataSetChanged();
                }
            }).addOnFailureListener(e -> Log.e("OrderHistory", "Error fetching menu item", e));
        }
    }

    private String formatTimestamp(Timestamp timestamp) {
        Date date = timestamp.toDate();
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        return sdf.format(date);
    }

    // Order model class
    public static class Order {
        private String orderId;
        private String orderDate;
        private String orderStatus;
        private Double totalBill;
        private List<DocumentReference> menuItemRefs;
        private String menuItemsDisplay = "";

        public Order(String orderId, String orderDate, String orderStatus, Double totalBill, List<DocumentReference> menuItemRefs) {
            this.orderId = orderId;
            this.orderDate = orderDate;
            this.orderStatus = orderStatus;
            this.totalBill = totalBill;
            this.menuItemRefs = menuItemRefs;
        }

        public String getOrderId() { return orderId; }
        public String getOrderDate() { return orderDate; }
        public String getOrderStatus() { return orderStatus; }
        public Double getTotalBill() { return totalBill; }
        public String getMenuItemsDisplay() { return menuItemsDisplay; }
        public void setMenuItemsDisplay(String menuItemsDisplay) { this.menuItemsDisplay = menuItemsDisplay; }
    }

    // RecyclerView Adapter
    public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

        private List<Order> orders;

        public OrderAdapter(List<Order> orders) {
            this.orders = orders;
        }

        @NonNull
        @Override
        public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.order_item, parent, false);
            return new OrderViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
            Order order = orders.get(position);
            holder.tvOrderId.setText("Order #" + order.getOrderId());
            holder.tvOrderDate.setText(order.getOrderDate());
            holder.tvOrderItems.setText(order.getMenuItemsDisplay());
            holder.tvOrderTotal.setText("Total: $" + String.format("%.2f", order.getTotalBill()));
        }

        @Override
        public int getItemCount() {
            return orders.size();
        }

        public class OrderViewHolder extends RecyclerView.ViewHolder {
            TextView tvOrderId, tvOrderDate, tvOrderItems, tvOrderTotal;

            public OrderViewHolder(@NonNull View itemView) {
                super(itemView);
                tvOrderId = itemView.findViewById(R.id.tvOrderId);
                tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
                tvOrderItems = itemView.findViewById(R.id.tvOrderItems);
                tvOrderTotal = itemView.findViewById(R.id.tvOrderTotal);
            }
        }
    }
}
