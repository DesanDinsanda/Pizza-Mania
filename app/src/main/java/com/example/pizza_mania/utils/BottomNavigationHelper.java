package com.example.pizza_mania.utils;
import com.example.pizza_mania.FeedbackActivity;
import com.example.pizza_mania.OrderHistoryActivity;
import com.example.pizza_mania.menu.MenuActivity;
import com.example.pizza_mania.R;
import android.app.Activity;
import android.content.Intent;

import com.example.pizza_mania.customerAccount.UpdateAccount;
import com.example.pizza_mania.customerHome.CustomerHome;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BottomNavigationHelper {
    public static void setupBottomNavigation(final Activity activity, BottomNavigationView bottomNavigationView, int selectedItemId) {

        bottomNavigationView.setSelectedItemId(selectedItemId);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == selectedItemId) {
                return true;
            }

            if (itemId == R.id.nav_home) {
                activity.startActivity(new Intent(activity, CustomerHome.class));
                activity.overridePendingTransition(0, 0);
                return true;
            }
            else if (itemId == R.id.nav_menu) {
                activity.startActivity(new Intent(activity, MenuActivity.class));
                activity.overridePendingTransition(0, 0);
                return true;
            }
            else if (itemId == R.id.nav_history) {
                activity.startActivity(new Intent(activity, OrderHistoryActivity.class));
                activity.overridePendingTransition(0, 0);
                return true;
            }
            else if (itemId == R.id.nav_profile) {
                activity.startActivity(new Intent(activity, UpdateAccount.class));
                activity.overridePendingTransition(0, 0);
                return true;
            }
            else if (itemId == R.id.nav_feedback) {
                activity.startActivity(new Intent(activity, FeedbackActivity.class));
                activity.overridePendingTransition(0, 0);
                return true;
            }

            return false;
        });
    }
}
