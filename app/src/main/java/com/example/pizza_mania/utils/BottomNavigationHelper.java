package com.example.pizza_mania.utils;
import com.example.pizza_mania.R;
import android.app.Activity;
import android.content.Intent;

import com.example.pizza_mania.customerAccount.AccountSettings;
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
                activity.startActivity(new Intent(activity, CustomerHome.class));
                activity.overridePendingTransition(0, 0);
                return true;
            }
            else if (itemId == R.id.nav_history) {
                activity.startActivity(new Intent(activity, CustomerHome.class));
                activity.overridePendingTransition(0, 0);
                return true;
            }
            else if (itemId == R.id.nav_profile) {
                activity.startActivity(new Intent(activity, AccountSettings.class));
                activity.overridePendingTransition(0, 0);
                return true;
            }

            return false;
        });
    }
}
