package com.example.restaurant;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.restaurant.constant.SQLCommand;
import com.example.restaurant.util.DBOperator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class KitchenActivity extends AppCompatActivity {

    private RecyclerView rvOrders;
    private TextView tvOrderQueue, tvInProgress;
    private Button btnRefresh;
    private DBOperator dbOperator;
    private OrderAdapter adapter;
    private Handler autoRefreshHandler;
    private Runnable autoRefreshRunnable;

    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kitchen);

        dbOperator = DBOperator.getInstance();

        // Initialize views
        rvOrders = findViewById(R.id.rv_kitchen_orders);
        tvOrderQueue = findViewById(R.id.tv_order_queue);
        tvInProgress = findViewById(R.id.tv_in_progress);
        btnRefresh = findViewById(R.id.btn_refresh);
        btnLogout = findViewById(R.id.btn_logout);

        rvOrders.setLayoutManager(new LinearLayoutManager(this));

        // Load orders
        loadKitchenOrders();

        // Refresh button
        btnRefresh.setOnClickListener(v -> {
            loadKitchenOrders();
            Toast.makeText(this, "Orders refreshed", Toast.LENGTH_SHORT).show();
        });

        // Logout button click listener
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutConfirmation();
            }
        });

        // Auto-refresh every 30 seconds
        autoRefreshHandler = new Handler();
        autoRefreshRunnable = new Runnable() {
            @Override
            public void run() {
                loadKitchenOrders();
                autoRefreshHandler.postDelayed(this, 30000); // 30 seconds
            }
        };
        autoRefreshHandler.postDelayed(autoRefreshRunnable, 30000);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (autoRefreshHandler != null) {
            autoRefreshHandler.removeCallbacks(autoRefreshRunnable);
        }
    }

    private void loadKitchenOrders() {
        List<KitchenOrder> orders = new ArrayList<>();

        try {
            // Get all active orders
            String query =
                    "SELECT o.Order_id, dt.DT_number, o.Order_status, " +
                            "GROUP_CONCAT(m.Menu_name || ' (x' || oi.Orditem_Quantity || ')') AS Items, " +
                            "oi.Orditem_time_ordered " +
                            "FROM Orders o " +
                            "JOIN Dining_table dt ON o.Order_DT_id = dt.DT_id " +
                            "JOIN Order_Item oi ON o.Order_id = oi.Orditem_Order_id " +
                            "JOIN Menu m ON oi.Orditem_Menu_dishID = m.Menu_DishID " +
                            "WHERE o.Order_status IN ('Placed', 'Preparing') " +
                            "GROUP BY o.Order_id " +
                            "ORDER BY oi.Orditem_time_ordered";

            Cursor cursor = dbOperator.execQuery(query);

            int queueCount = 0;
            int progressCount = 0;

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    KitchenOrder order = new KitchenOrder();
                    order.orderId = cursor.getString(0);
                    order.tableNumber = cursor.getInt(1);
                    order.status = cursor.getString(2);
                    order.items = cursor.getString(3);
                    order.timeOrdered = cursor.getString(4);

                    orders.add(order);

                    if (order.status.equals("Placed")) {
                        queueCount++;
                    } else if (order.status.equals("Preparing")) {
                        progressCount++;
                    }
                }
                cursor.close();
            }

            tvOrderQueue.setText(String.valueOf(queueCount));
            tvInProgress.setText(String.valueOf(progressCount));

            adapter = new OrderAdapter(orders);
            rvOrders.setAdapter(adapter);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading orders", Toast.LENGTH_SHORT).show();
        }
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    logout();
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void logout() {
        // Clear saved preferences
        SharedPreferences prefs = getSharedPreferences("RestaurantApp", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        // Show logout message
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Navigate back to LoginActivity
        Intent intent = new Intent(KitchenActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // Handle back button press
    @Override
    public void onBackPressed() {
        showLogoutConfirmation();
    }

    private void updateOrderStatus(String orderId, String newStatus) {
        try {
            dbOperator.execSQL(
                    SQLCommand.UPDATE_ORDER_STATUS,
                    new Object[]{newStatus, orderId}
            );
            loadKitchenOrders();
            Toast.makeText(this, "Order " + orderId + " marked as " + newStatus, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error updating order", Toast.LENGTH_SHORT).show();
        }
    }

    // Kitchen Order model
    private static class KitchenOrder {
        String orderId;
        int tableNumber;
        String status;
        String items;
        String timeOrdered;
    }

    // Adapter for kitchen orders
    private class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

        private List<KitchenOrder> orders;

        public OrderAdapter(List<KitchenOrder> orders) {
            this.orders = orders;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_kitchen_order, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            KitchenOrder order = orders.get(position);

            holder.tvOrderId.setText("Order #" + order.orderId);
            holder.tvTable.setText("Table " + order.tableNumber);
            holder.tvItems.setText(order.items);

            // Calculate time elapsed
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                Date orderTime = sdf.parse(order.timeOrdered);
                long elapsed = (new Date().getTime() - orderTime.getTime()) / 60000; // minutes
                holder.tvTimeElapsed.setText(elapsed + " min ago");

                // Color code by urgency
                if (elapsed > 30) {
                    holder.cardView.setCardBackgroundColor(Color.parseColor("#FEE2E2")); // Red
                    holder.tvTimeElapsed.setTextColor(Color.parseColor("#991B1B"));
                } else if (elapsed > 15) {
                    holder.cardView.setCardBackgroundColor(Color.parseColor("#FEF3C7")); // Yellow
                    holder.tvTimeElapsed.setTextColor(Color.parseColor("#92400E"));
                } else {
                    holder.cardView.setCardBackgroundColor(Color.parseColor("#DCFCE7")); // Green
                    holder.tvTimeElapsed.setTextColor(Color.parseColor("#166534"));
                }
            } catch (Exception e) {
                holder.tvTimeElapsed.setText("Just now");
            }

            // Status badge
            holder.tvStatus.setText(order.status);
            if (order.status.equals("Placed")) {
                holder.tvStatus.setBackgroundColor(Color.parseColor("#3B82F6")); // Blue
            } else if (order.status.equals("Preparing")) {
                holder.tvStatus.setBackgroundColor(Color.parseColor("#EAB308")); // Yellow
            }

            // Button actions
            if (order.status.equals("Placed")) {
                holder.btnAction.setText("Start Preparing");
                holder.btnAction.setOnClickListener(v -> {
                    updateOrderStatus(order.orderId, "Preparing");
                });
            } else if (order.status.equals("Preparing")) {
                holder.btnAction.setText("Mark Ready");
                holder.btnAction.setOnClickListener(v -> {
                    updateOrderStatus(order.orderId, "Ready");
                });
            }
        }

        @Override
        public int getItemCount() {
            return orders.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            CardView cardView;
            TextView tvOrderId, tvTable, tvItems, tvTimeElapsed, tvStatus;
            Button btnAction;

            public ViewHolder(View itemView) {
                super(itemView);
                cardView = itemView.findViewById(R.id.card_kitchen_order);
                tvOrderId = itemView.findViewById(R.id.tv_kitchen_order_id);
                tvTable = itemView.findViewById(R.id.tv_kitchen_table);
                tvItems = itemView.findViewById(R.id.tv_kitchen_items);
                tvTimeElapsed = itemView.findViewById(R.id.tv_time_elapsed);
                tvStatus = itemView.findViewById(R.id.tv_kitchen_status);
                btnAction = itemView.findViewById(R.id.btn_kitchen_action);
            }
        }
    }
}