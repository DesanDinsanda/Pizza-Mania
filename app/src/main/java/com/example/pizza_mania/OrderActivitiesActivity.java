package com.example.pizza_mania;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class OrderActivitiesActivity extends AppCompatActivity {

    Button btOrderHistory, btFeedback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_activities);

        // Handle Edge-to-Edge padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize buttons
        btOrderHistory = findViewById(R.id.btOrderHistory);
        btFeedback = findViewById(R.id.btFeedback);

        // Navigate to OrderHistoryActivity
        btOrderHistory.setOnClickListener(v -> {
            Intent intent = new Intent(OrderActivitiesActivity.this, OrderHistoryActivity.class);
            startActivity(intent);
        });

        // Navigate to FeedbackActivity
        btFeedback.setOnClickListener(v -> {
            Intent intent = new Intent(OrderActivitiesActivity.this, FeedbackActivity.class);
            startActivity(intent);
        });
    }
}
