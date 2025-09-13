package com.example.pizza_mania;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.ViewHolder> {

    private final Context context;
    private final List<CartItem> cartItems;
    private final onCartChangeListener listener;
    private final CartDbHelper dbHelper;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public CartItemAdapter(Context context, List<CartItem> cartItems, onCartChangeListener listener) {
        this.context = context;
        this.cartItems = cartItems;
        this.listener = listener;
        this.dbHelper = new CartDbHelper(context);
    }


    public interface onCartChangeListener {
        void onCartChanged();
    }


    @NonNull
    @Override
    public CartItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cart_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartItemAdapter.ViewHolder holder, int position) {
        CartItem item = cartItems.get(position);

        holder.name.setText(item.getItemName() + " (" + item.getSize() + ")");
        holder.price.setText("Price: RS. " + item.getPrice());
        holder.quantity.setText(String.valueOf(item.getQuantity()));

        Glide.with(context).load(item.getItemImage()).into(holder.image);

        holder.btnIncrease.setOnClickListener(v -> {
            int newQty = item.getQuantity() + 1;
            executor.execute(() -> dbHelper.updateQuantity(item.getItemName(), item.getSize(), item.getBranchName(), newQty));
            item.setQuantity(newQty);
            holder.quantity.setText(String.valueOf(newQty));
            listener.onCartChanged();
        });

        holder.btnDecrease.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                int newQty = item.getQuantity() - 1;
                executor.execute(() -> dbHelper.updateQuantity(item.getItemName(), item.getSize(), item.getBranchName(), newQty));
                item.setQuantity(newQty);
                holder.quantity.setText(String.valueOf(newQty));
                listener.onCartChanged();
            }
        });

        holder.btnRemove.setOnClickListener(v -> {
            executor.execute(() -> dbHelper.removeItem(item.getItemName(), item.getSize(), item.getBranchName()));
            int pos = holder.getAdapterPosition();
            if (pos >= 0 && pos < cartItems.size()) {
                cartItems.remove(pos);
                notifyItemRemoved(pos);
                notifyItemRangeChanged(pos, cartItems.size());
            }
            listener.onCartChanged();
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        ImageView image;
        TextView name, price, quantity;
        Button btnIncrease, btnDecrease, btnRemove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.cart_item_image);
            name = itemView.findViewById(R.id.cart_item_name);
            price = itemView.findViewById(R.id.cart_item_price);
            quantity = itemView.findViewById(R.id.cart_item_quantity);
            btnIncrease = itemView.findViewById(R.id.btn_increase);
            btnDecrease = itemView.findViewById(R.id.btn_decrease);
            btnRemove = itemView.findViewById(R.id.btn_remove);

        }
    }

}
