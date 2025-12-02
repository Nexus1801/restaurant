package com.example.restaurant;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
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

public class ActiveOrdersActivity extends AppCompatActivity {

    private RecyclerView rvOrders;
    private TextView tvOrderCount;
    private Button btnRefresh;
    private DBOperator dbOperator;
    private OrderAdapter adapter;
    private List<Order> orders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_orders);

        dbOperator = DBOperator.getInstance();

        // Initialize views
        rvOrders = findViewById(R.id.rv_active_orders);
        tvOrderCount = findViewById(R.id.tv_order_count);
        btnRefresh = findViewById(R.id.btn_refresh_orders);

        rvOrders.setLayoutManager(new LinearLayoutManager(this));

        // Load orders
        loadActiveOrders();

        // Refresh button
        btnRefresh.setOnClickListener(v -> {
            loadActiveOrders();
            Toast.makeText(this, "Orders refreshed", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadActiveOrders();
    }

    private void loadActiveOrders() {
        orders = new ArrayList<>();

        try {
            // Query for active orders with all details
            String query =
                    "SELECT o.Order_id, dt.DT_number, c.Cust_name, e.Emp_name, " +
                            "o.Order_status, o.Order_Total, " +
                            "GROUP_CONCAT(m.Menu_name || ' (x' || oi.Orditem_Quantity || ')', ', ') AS Items, " +
                            "MIN(oi.Orditem_time_ordered) AS Time_Ordered " +
                            "FROM Orders o " +
                            "LEFT JOIN Customer c ON o.Order_Cust_id = c.Cust_id " +
                            "JOIN Dining_table dt ON o.Order_DT_id = dt.DT_id " +
                            "JOIN Employee e ON o.Order_Emp_id = e.Emp_id " +
                            "JOIN Order_Item oi ON o.Order_id = oi.Orditem_Order_id " +
                            "JOIN Menu m ON oi.Orditem_Menu_dishID = m.Menu_DishID " +
                            "WHERE o.Order_status IN ('Placed', 'Preparing', 'Ready') " +
                            "GROUP BY o.Order_id " +
                            "ORDER BY MIN(oi.Orditem_time_ordered) DESC";

            Cursor cursor = dbOperator.execQuery(query);

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Order order = new Order();
                    order.orderId = cursor.getString(0);
                    order.tableNumber = cursor.getInt(1);
                    order.customerName = cursor.getString(2);
                    order.serverName = cursor.getString(3);
                    order.status = cursor.getString(4);
                    order.total = cursor.getInt(5);
                    order.items = cursor.getString(6);
                    order.timeOrdered = cursor.getString(7);

                    orders.add(order);
                }
                cursor.close();
            }

            // Update count
            tvOrderCount.setText(orders.size() + " Active Orders");

            // Update RecyclerView
            adapter = new OrderAdapter(orders);
            rvOrders.setAdapter(adapter);

            if (orders.isEmpty()) {
                Toast.makeText(this, "No active orders", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading orders: " + e.getMessage(), Toast.LENGTH_LONG).show();
            android.util.Log.e("ActiveOrders", "Error loading orders", e);
        }
    }

    private void updateOrderStatus(String orderId, String newStatus) {
        try {
            dbOperator.execSQL(
                    SQLCommand.UPDATE_ORDER_STATUS,
                    new Object[]{newStatus, orderId}
            );

            Toast.makeText(this, "Order " + orderId + " updated to " + newStatus, Toast.LENGTH_SHORT).show();
            loadActiveOrders(); // Refresh list

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error updating order: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showOrderDetails(Order order) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Order #" + order.orderId);

        String details =
                "Table: " + order.tableNumber + "\n" +
                        "Server: " + order.serverName + "\n" +
                        "Customer: " + (order.customerName != null ? order.customerName : "Walk-in") + "\n" +
                        "Status: " + order.status + "\n" +
                        "Total: $" + order.total + "\n\n" +
                        "Items:\n" + order.items.replace(", ", "\n");

        builder.setMessage(details);
        builder.setPositiveButton("Close", null);
        builder.show();
    }

    // Order model class
    private static class Order {
        String orderId;
        int tableNumber;
        String customerName;
        String serverName;
        String status;
        int total;
        String items;
        String timeOrdered;
    }

    // Adapter for orders
    private class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

        private List<Order> orders;

        public OrderAdapter(List<Order> orders) {
            this.orders = orders;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_active_order, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Order order = orders.get(position);

            // Order header
            holder.tvOrderId.setText("Order #" + order.orderId.substring(Math.max(0, order.orderId.length() - 6)));
            holder.tvTable.setText("Table " + order.tableNumber);
            holder.tvServer.setText("Server: " + order.serverName);

            // Order details
            holder.tvItems.setText(order.items);
            holder.tvTotal.setText(String.format("$%d", order.total));

            // Calculate time elapsed
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                Date orderTime = sdf.parse(order.timeOrdered);
                long elapsed = (new Date().getTime() - orderTime.getTime()) / 60000; // minutes
                holder.tvTimeElapsed.setText(elapsed + " min ago");

                // Color code by urgency
                if (elapsed > 30) {
                    holder.tvTimeElapsed.setTextColor(Color.parseColor("#DC2626")); // Red
                } else if (elapsed > 15) {
                    holder.tvTimeElapsed.setTextColor(Color.parseColor("#EA580C")); // Orange
                } else {
                    holder.tvTimeElapsed.setTextColor(Color.parseColor("#16A34A")); // Green
                }
            } catch (Exception e) {
                holder.tvTimeElapsed.setText("Just now");
            }

            // Status badge
            holder.tvStatus.setText(order.status);
            switch (order.status) {
                case "Placed":
                    holder.tvStatus.setBackgroundColor(Color.parseColor("#3B82F6")); // Blue
                    holder.cardView.setCardBackgroundColor(Color.parseColor("#EFF6FF"));
                    break;
                case "Preparing":
                    holder.tvStatus.setBackgroundColor(Color.parseColor("#F59E0B")); // Amber
                    holder.cardView.setCardBackgroundColor(Color.parseColor("#FFFBEB"));
                    break;
                case "Ready":
                    holder.tvStatus.setBackgroundColor(Color.parseColor("#10B981")); // Green
                    holder.cardView.setCardBackgroundColor(Color.parseColor("#F0FDF4"));
                    break;
            }

            // Status update buttons
            switch (order.status) {
                case "Placed":
                    holder.btnStatusAction.setText("Start Preparing");
                    holder.btnStatusAction.setVisibility(View.VISIBLE);
                    holder.btnStatusAction.setOnClickListener(v ->
                            updateOrderStatus(order.orderId, "Preparing")
                    );
                    break;
                case "Preparing":
                    holder.btnStatusAction.setText("Mark Ready");
                    holder.btnStatusAction.setVisibility(View.VISIBLE);
                    holder.btnStatusAction.setOnClickListener(v ->
                            updateOrderStatus(order.orderId, "Ready")
                    );
                    break;
                case "Ready":
                    holder.btnStatusAction.setText("Mark Served");
                    holder.btnStatusAction.setVisibility(View.VISIBLE);
                    holder.btnStatusAction.setOnClickListener(v ->
                            updateOrderStatus(order.orderId, "Served")
                    );
                    break;
                default:
                    holder.btnStatusAction.setVisibility(View.GONE);
                    break;
            }

            // View details button
            holder.btnViewDetails.setOnClickListener(v -> showOrderDetails(order));

            // Card click to view details
            holder.cardView.setOnClickListener(v -> showOrderDetails(order));
        }

        @Override
        public int getItemCount() {
            return orders.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            CardView cardView;
            TextView tvOrderId, tvTable, tvServer, tvItems, tvTotal, tvTimeElapsed, tvStatus;
            Button btnStatusAction, btnViewDetails;

            public ViewHolder(View itemView) {
                super(itemView);
                cardView = itemView.findViewById(R.id.card_active_order);
                tvOrderId = itemView.findViewById(R.id.tv_order_id);
                tvTable = itemView.findViewById(R.id.tv_order_table);
                tvServer = itemView.findViewById(R.id.tv_order_server);
                tvItems = itemView.findViewById(R.id.tv_order_items);
                tvTotal = itemView.findViewById(R.id.tv_order_total);
                tvTimeElapsed = itemView.findViewById(R.id.tv_order_time);
                tvStatus = itemView.findViewById(R.id.tv_order_status);
                btnStatusAction = itemView.findViewById(R.id.btn_status_action);
                btnViewDetails = itemView.findViewById(R.id.btn_view_details);
            }
        }
    }
}
