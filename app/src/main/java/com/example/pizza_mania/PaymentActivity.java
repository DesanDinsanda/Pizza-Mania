package com.example.pizza_mania;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PaymentActivity extends AppCompatActivity {

    private PaymentSheet paymentSheet;
    private String paymentIntentClientSecret;
    private TextView statusText;
    private Button payBtn;
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // Initialize Stripe with your publishable key
        PaymentConfiguration.init(
                this,
                "pk_test_51S5janCCei93j2b1spNnOpTTU5TOzKwZ28JCnOfgukIFkS80Zot6UQjobPEDvjj7xoe3vRMMchd8GM44eXFrm1wM00zGtqMMOO" // Your publishable key here
        );

        // Initialize views
        statusText = findViewById(R.id.statusText);
        payBtn = findViewById(R.id.payBtn);

        // Initialize PaymentSheet
        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);

        payBtn.setOnClickListener(v -> createPaymentIntent());
    }

    private void createPaymentIntent() {
        statusText.setText("Creating payment...");
        payBtn.setEnabled(false);

        // Create request body
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("amount", 10.99);
        requestData.put("currency", "usd");

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
                    statusText.setText("Network error: " + e.getMessage());
                    Toast.makeText(PaymentActivity.this, "Network error", Toast.LENGTH_LONG).show();
                    payBtn.setEnabled(true);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    PaymentResponse paymentResponse = gson.fromJson(responseData, PaymentResponse.class);

                    runOnUiThread(() -> {
                        if (paymentResponse.success) {
                            paymentIntentClientSecret = paymentResponse.clientSecret;
                            statusText.setText("Payment intent created! ✅");
                            presentPaymentSheet();
                        } else {
                            statusText.setText("Error: " + paymentResponse.error);
                            payBtn.setEnabled(true);
                        }
                    });
                } else {
                    runOnUiThread(() -> {
                        statusText.setText("Server error: " + response.code());
                        payBtn.setEnabled(true);
                    });
                }
            }
        });
    }

    private void presentPaymentSheet() {
        PaymentSheet.Configuration configuration = new PaymentSheet.Configuration.Builder("Pizza Mania")
                .build();

        paymentSheet.presentWithPaymentIntent(paymentIntentClientSecret, configuration);
    }

    private void onPaymentSheetResult(final PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            statusText.setText("Payment Successful! 🎉");
            Toast.makeText(this, "Payment Success!", Toast.LENGTH_SHORT).show();
        } else if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
            statusText.setText("Payment Canceled ❌");
            Toast.makeText(this, "Payment Canceled", Toast.LENGTH_SHORT).show();
            payBtn.setEnabled(true);
        } else if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
            PaymentSheetResult.Failed failedResult = (PaymentSheetResult.Failed) paymentSheetResult;
            statusText.setText("Payment Failed 😢");
            Toast.makeText(this, "Payment Failed: " + failedResult.getError().getMessage(), Toast.LENGTH_LONG).show();
            payBtn.setEnabled(true);
        }
    }

    // Response class
    private static class PaymentResponse {
        boolean success;
        String clientSecret;
        String paymentIntentId;
        String error;
    }
}