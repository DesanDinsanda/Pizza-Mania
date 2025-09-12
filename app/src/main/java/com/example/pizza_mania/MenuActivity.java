package com.example.pizza_mania;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pizza_mania.utils.BottomNavigationHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Firebase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MenuActivity extends AppCompatActivity {

    private ImageView cartIcon;
    private Spinner branchSpinner;
    private Button btnPizza, btnDessert, btnDrink, btnViewCart;
    private RecyclerView menuRecycleView;
    private String selectedBranch;
    private  String selectedCategory = "pizza";
    private List<String> branchList = new ArrayList<>();
    private List<MenuItem> menuItems = new ArrayList<>();
    private MenuItemAdapter adapter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        cartIcon = findViewById(R.id.cart);
        branchSpinner = findViewById(R.id.branch_spinner);
        btnPizza = findViewById(R.id.btn_pizza);
        btnDessert = findViewById(R.id.btn_dessert);
        btnDrink = findViewById(R.id.btn_drink);
        btnViewCart = findViewById(R.id.btn_view_cart);
        menuRecycleView = findViewById(R.id.menu_recycle_view);

        menuRecycleView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new MenuItemAdapter(this, menuItems, selectedBranch);
        menuRecycleView.setAdapter(adapter);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        BottomNavigationHelper.setupBottomNavigation(this, bottomNavigationView, R.id.nav_menu);

        loadBranches();

        branchSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                selectedBranch = branchList.get(position);
                if (adapter != null) adapter.setBranchName(selectedBranch);
                loadMenuItems(selectedCategory);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {

            }
        });

        //category buttons
        btnPizza.setOnClickListener(v -> {
            selectedCategory = "pizza";
            loadMenuItems(selectedCategory);
        });
        btnDessert.setOnClickListener(v -> {
            selectedCategory = "dessert";
            loadMenuItems(selectedCategory);
        });
        btnDrink.setOnClickListener(v -> {
            selectedCategory = "drink";
            loadMenuItems(selectedCategory);
        });

        // go to cart
        cartIcon.setOnClickListener(v-> startActivity(new Intent(MenuActivity.this, CartActivity.class)));
        btnViewCart.setOnClickListener(v-> startActivity(new Intent(MenuActivity.this, CartActivity.class)));
    }

    private void loadBranches() {
        db.collection("Branch").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        branchList.clear();
                        for(QueryDocumentSnapshot doc : task.getResult()) {
                            branchList.add(doc.getString("branchName"));
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, branchList);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        branchSpinner.setAdapter(adapter);

                        if (!branchList.isEmpty()) {
                            selectedBranch = branchList.get(0);
                            loadMenuItems(selectedCategory);
                        }
                    }
                });
    }

    private void loadMenuItems(String category) {
        if (selectedBranch == null) return;

        String collection = selectedBranch.contains("Colombo") ? "MenuItem_Colombo" : "MenuItem_Galle";

        db.collection(collection).whereEqualTo("category", category)
                .whereEqualTo("availability", true)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        menuItems.clear();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            MenuItem item = doc.toObject(MenuItem.class);
                            menuItems.add(item);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}
