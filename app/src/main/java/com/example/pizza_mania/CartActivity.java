package com.example.pizza_mania;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CartActivity extends AppCompatActivity {

    private RecyclerView cartRecycleView;
    private TextView totalPriceText;
    private Button btnPlaceOrder;
    private final List<CartItem> cartItems = new ArrayList<>();
    private CartItemAdapter adapter;
    private CartDbHelper dbHelper;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        cartRecycleView = findViewById(R.id.cart_recycler_view);
        totalPriceText = findViewById(R.id.total_price_text);
        btnPlaceOrder = findViewById(R.id.btn_place_order);

        cartRecycleView.setLayoutManager(new LinearLayoutManager(this));
        dbHelper = new CartDbHelper(this);

        adapter = new CartItemAdapter(this, cartItems, this::refreshCart);
        cartRecycleView.setAdapter(adapter);

        refreshCart();

        btnPlaceOrder.setOnClickListener(v ->{
            Toast.makeText(this, "Order Placed Successfully", Toast.LENGTH_SHORT).show();

            executor.execute(() -> {
                dbHelper.clearCart();
                mainHandler.post(() -> {
                    refreshCart();
                    Toast.makeText(CartActivity.this, "Cart Cleared", Toast.LENGTH_SHORT).show();
                });
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshCart();
    }

    // load cart
    public void refreshCart() {
        executor.execute(() -> {
            List<CartItem> items = dbHelper.getAllItems();
            mainHandler.post(() -> {
                cartItems.clear();
                cartItems.addAll(items);
                adapter.notifyDataSetChanged();
                updateTotalPrice();
            });
        });
    }

    private void updateTotalPrice() {
        int total = 0;
        for (CartItem item : cartItems) {
            total += item.getPrice() * item.getQuantity();
        }
        totalPriceText.setText("Total: RS. " + total);
    }
}
