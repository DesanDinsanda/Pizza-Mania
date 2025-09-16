package com.example.pizza_mania.menu;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pizza_mania.cart.CartDbHelper;
import com.example.pizza_mania.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MenuItemAdapter extends RecyclerView.Adapter<MenuItemAdapter.ViewHolder> {

    private final Context context;
    private final List<MenuItem> menuItems;
    private String branchName;
    private final CartDbHelper dbHelper;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());


    public MenuItemAdapter(Context context, List<MenuItem> menuItems, String branchName) {
        this.context = context;
        this.menuItems = menuItems;
        this.branchName = branchName;
        this.dbHelper = new CartDbHelper(context);

    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.menu_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MenuItem item = menuItems.get(position);
        holder.name.setText(item.getItemName());

        Glide.with(context)
                .load(item.getItemImage())
                .into(holder.image);

        List<String> sizes = new ArrayList<>();
        boolean isPizza = "pizza".equalsIgnoreCase(item.getCategory());
        if (isPizza) {
            sizes.add("RS." + item.getSmallPrice() + " (S)");
            sizes.add("RS." + item.getMediumPrice() + " (M)");
            sizes.add("RS." + item.getLargePrice() + " (L)");
        } else {
            sizes.add("" + item.getSmallPrice());
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, sizes);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.sizeSpinner.setAdapter(spinnerAdapter);

        holder.addButton.setOnClickListener(v -> {
            int selected = holder.sizeSpinner.getSelectedItemPosition();
            String size;
            int price;
            if (isPizza) {
                if (selected == 0) {
                    size = "Small";
                    price = item.getSmallPrice();
                }
                else if (selected == 1) {
                    size = "Medium";
                    price = item.getMediumPrice();
                }
                else {
                    size = "Large";
                    price = item.getLargePrice();
                }
            } else {
                size = "Regular";
                price = item.getSmallPrice();
            }

            final String branch = branchName == null ? "" : branchName;
            // DB write on background thread
            executor.execute(() -> {
                dbHelper.addOrUpdateItem(item.getItemName(), item.getItemImage(), size, price, branch);
                mainHandler.post(() -> Toast.makeText(context, "Added to cart", Toast.LENGTH_SHORT).show());
            });
        });

    }

    @Override
    public int getItemCount() {
        return menuItems == null ? 0 : menuItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        TextView name;
        Spinner sizeSpinner;
        Button addButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.item_image);
            name = itemView.findViewById(R.id.item_name);
            sizeSpinner = itemView.findViewById(R.id.size_spinner);
            addButton = itemView.findViewById(R.id.add_button);
        }
    }





}
