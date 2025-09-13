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

import com.google.gson.Gson;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.model.Address;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CartActivity extends AppCompatActivity {

    private RecyclerView cartRecycleView;
    private TextView totalPriceText;
    private Button btnPlaceOrder;
    private final List<CartItem> cartItems = new ArrayList<>();
    private CartItemAdapter adapter;
    private CartDbHelper dbHelper;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private PaymentSheet paymentSheet;
    private String paymentIntentClientSecret;
//    private TextView statusText;
//    private Button payBtn;
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

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

        // Initialize Stripe with your publishable key
        PaymentConfiguration.init(
                this,
                "pk_test_51S5janCCei93j2b1spNnOpTTU5TOzKwZ28JCnOfgukIFkS80Zot6UQjobPEDvjj7xoe3vRMMchd8GM44eXFrm1wM00zGtqMMOO" // Your publishable key here
        );
        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);

        btnPlaceOrder.setOnClickListener(v ->{
            createPaymentIntent();
//            Toast.makeText(this, "Order Placed Successfully", Toast.LENGTH_SHORT).show();
//
//            executor.execute(() -> {
//                dbHelper.clearCart();
//                mainHandler.post(() -> {
//                    refreshCart();
//                    Toast.makeText(CartActivity.this, "Cart Cleared", Toast.LENGTH_SHORT).show();
//                });
//            });
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
        int total = (int) getTotalPrice();
        totalPriceText.setText("Total: RS. " + total);
    }

    private float getTotalPrice(){
        float total = 0;
        for (CartItem item : cartItems){
            total += item.getPrice() * item.getQuantity();
        }
        return total;
    }

    private void createPaymentIntent() {
//        statusText.setText("Creating payment...");
//        btnPlaceOrder.setEnabled(false);
        changePlaceOrderBtnState(BtnState.LOADING);

        // Create request body
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("amount", getTotalPrice());
        requestData.put("currency", "lkr");

        String json = gson.toJson(requestData);
        RequestBody body = RequestBody.create(json, JSON);

        // Replace with your actual function URL
        String functionUrl = "https://us-central1-pizza-mania-e5436.cloudfunctions.net/createMockPaymentHTTP";

        Request request = new Request.Builder()
                .url(functionUrl)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
//                    statusText.setText("Network error: " + e.getMessage());
                    e.printStackTrace();
                    Toast.makeText(CartActivity.this, "Network error", Toast.LENGTH_LONG).show();
//                    btnPlaceOrder.setEnabled(true);
                    changePlaceOrderBtnState(BtnState.TRY_AGAIN);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    CartActivity.PaymentResponse paymentResponse = gson.fromJson(responseData, CartActivity.PaymentResponse.class);

                    runOnUiThread(() -> {
                        if (paymentResponse.success) {
                            paymentIntentClientSecret = paymentResponse.clientSecret;
//                            statusText.setText("Payment intent created! ✅");
                            presentPaymentSheet();
                        } else {
                            Toast.makeText(CartActivity.this, paymentResponse.error, Toast.LENGTH_SHORT).show();
//                            statusText.setText("Error: " + paymentResponse.error);
//                            btnPlaceOrder.setEnabled(true);
                            changePlaceOrderBtnState(BtnState.TRY_AGAIN);
                        }
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(CartActivity.this, "Server error: "+response.code(), Toast.LENGTH_SHORT).show();
//                        statusText.setText("Server error: " + response.code());
//                        btnPlaceOrder.setEnabled(true);
                        changePlaceOrderBtnState(BtnState.TRY_AGAIN);
                    });
                }
            }
        });
    }

    private void presentPaymentSheet() {
        PaymentSheet.BillingDetails billingDetails = new PaymentSheet.BillingDetails.Builder()
                .address(new PaymentSheet.Address.Builder().country("LK").build())
                .build();
        PaymentSheet.Configuration configuration = new PaymentSheet.Configuration.Builder("Pizza Mania")
                .defaultBillingDetails(billingDetails)
                .build();

        paymentSheet.presentWithPaymentIntent(paymentIntentClientSecret, configuration);
    }

    private void onPaymentSheetResult(final PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
//            statusText.setText("Payment Successful! 🎉");
            Toast.makeText(this, "Payment Success!", Toast.LENGTH_SHORT).show();
            changePlaceOrderBtnState(BtnState.DONE);
            finish();
        } else if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
//            statusText.setText("Payment Canceled ❌");
            Toast.makeText(this, "Payment Canceled", Toast.LENGTH_SHORT).show();
//            btnPlaceOrder.setEnabled(true);
            changePlaceOrderBtnState(BtnState.ENABLED);
        } else if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
            PaymentSheetResult.Failed failedResult = (PaymentSheetResult.Failed) paymentSheetResult;
//            statusText.setText("Payment Failed 😢");
            Toast.makeText(this, "Payment Failed: " + failedResult.getError().getMessage(), Toast.LENGTH_LONG).show();
//            btnPlaceOrder.setEnabled(true);
            changePlaceOrderBtnState(BtnState.TRY_AGAIN);
        }
    }

    private static class PaymentResponse {
        boolean success;
        String clientSecret;
        String paymentIntentId;
        String error;
    }

    private enum BtnState{
        ENABLED,
        LOADING,
        DONE,
        TRY_AGAIN
    }

    private void changePlaceOrderBtnState(BtnState state){
        switch(state){
            case ENABLED:
                btnPlaceOrder.setText(getString(R.string.payment_btn_enabled));
                btnPlaceOrder.setEnabled(true);
                btnPlaceOrder.setAlpha(1.0f);
                break;
            case LOADING:
                btnPlaceOrder.setText(getString(R.string.payment_btn_loading));
                btnPlaceOrder.setEnabled(false);
                btnPlaceOrder.setAlpha(0.5f);
                break;
            case DONE:
                btnPlaceOrder.setText(getString(R.string.payment_btn_done));
                btnPlaceOrder.setEnabled(false);
                btnPlaceOrder.setAlpha(1.0f);
                break;
            case TRY_AGAIN:
                btnPlaceOrder.setText(getString(R.string.payment_btn_try_again));
                btnPlaceOrder.setEnabled(true);
                btnPlaceOrder.setAlpha(1.0f);
        }
    }
}
