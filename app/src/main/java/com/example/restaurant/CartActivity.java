package com.example.restaurant;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.restaurant.constant.SQLCommand;
import com.example.restaurant.util.DBOperator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CartActivity extends AppCompatActivity {

    private RecyclerView rvCart;
    private TextView tvSubtotal, tvTax, tvTotal;
    private EditText etSpecialInstructions;
    private Button btnSubmitOrder, btnClearCart;
    private TextView tvSelectedTable;
    private CartAdapter adapter;
    private DBOperator dbOperator;
    private String selectedTable;
    private String employeeId;

    // Default customer for walk-in orders
    private static final String DEFAULT_CUSTOMER_ID = "CUST001";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        dbOperator = DBOperator.getInstance();

        // Get selected table from intent
        selectedTable = getIntent().getStringExtra("selected_table");

        // Get employee info
        SharedPreferences prefs = getSharedPreferences("RestaurantApp", MODE_PRIVATE);
        employeeId = prefs.getString("employee_id", "");

        // Initialize views
        rvCart = findViewById(R.id.rv_cart);
        tvSubtotal = findViewById(R.id.tv_subtotal);
        tvTax = findViewById(R.id.tv_tax);
        tvTotal = findViewById(R.id.tv_total);
        etSpecialInstructions = findViewById(R.id.et_special_instructions);
        btnSubmitOrder = findViewById(R.id.btn_submit_order);
        btnClearCart = findViewById(R.id.btn_clear_cart);
        tvSelectedTable = findViewById(R.id.tv_selected_table);

        rvCart.setLayoutManager(new LinearLayoutManager(this));

        // Display selected table
        tvSelectedTable.setText(selectedTable != null ? selectedTable : "No table selected");

        // Load cart items
        loadCart();

        // Submit order button
        btnSubmitOrder.setOnClickListener(v -> submitOrder());

        // Clear cart button
        btnClearCart.setOnClickListener(v -> clearCart());
    }

    private void loadCart() {
        List<NewOrderActivity.CartItem> cartItems = new ArrayList<>(NewOrderActivity.cart.values());

        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        adapter = new CartAdapter(cartItems);
        rvCart.setAdapter(adapter);

        calculateTotals();
    }

    private void calculateTotals() {
        double subtotal = 0;

        for (NewOrderActivity.CartItem item : NewOrderActivity.cart.values()) {
            subtotal += (double)(item.price * item.quantity);
        }

        double tax = subtotal * 0.08; // 8% tax
        double total = subtotal + tax;

        tvSubtotal.setText(String.format("$%.2f", subtotal));
        tvTax.setText(String.format("$%.2f", tax));
        tvTotal.setText(String.format("$%.2f", total));
    }

    private void submitOrder() {
        if (NewOrderActivity.cart.isEmpty()) {
            Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedTable == null || selectedTable.equals("Select Table")) {
            Toast.makeText(this, "Please select a table", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Extract table number from "Table X"
            String tableNumberStr = selectedTable.replace("Table ", "");
            int tableNumber = Integer.parseInt(tableNumberStr);

            // Get table ID from database
            String getTableIdQuery = "SELECT DT_id FROM Dining_table WHERE DT_number = " + tableNumber;
            android.database.Cursor cursor = dbOperator.execQuery(getTableIdQuery);

            String tableId = null;
            if (cursor != null && cursor.moveToFirst()) {
                tableId = cursor.getString(0);
                cursor.close();
            }

            if (tableId == null) {
                Toast.makeText(this, "Table not found", Toast.LENGTH_SHORT).show();
                return;
            }

            // Generate order ID
            String orderId = "ORD" + System.currentTimeMillis();

            // Calculate total
            double total = 0;
            for (NewOrderActivity.CartItem item : NewOrderActivity.cart.values()) {
                total += (double)(item.price * item.quantity);
            }
            total = total * 1.08; // Add tax

            // Insert order
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            String currentTime = sdf.format(new Date());

            // FIXED: Use DEFAULT_CUSTOMER_ID instead of null for walk-in orders
            dbOperator.execSQL(
                    SQLCommand.INSERT_ORDER,
                    new Object[]{
                            orderId,                  // Order_id
                            DEFAULT_CUSTOMER_ID,      // Order_Cust_id (FIXED - was null)
                            tableId,                  // Order_DT_id
                            employeeId,               // Order_Emp_id
                            "Placed",                 // Order_status
                            (int)total                // Order_Total
                    }
            );

            // Insert order items
            for (NewOrderActivity.CartItem item : NewOrderActivity.cart.values()) {
                String orderItemId = "OI" + System.currentTimeMillis() + item.dishID;

                dbOperator.execSQL(
                        SQLCommand.INSERT_ORDER_ITEM,
                        new Object[]{
                                orderItemId,
                                orderId,
                                item.dishID,
                                item.quantity,
                                item.price,
                                currentTime
                        }
                );

                // Small delay to ensure unique IDs
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // Clear cart
            NewOrderActivity.cart.clear();

            Toast.makeText(this, "Order submitted successfully!", Toast.LENGTH_LONG).show();

            // Return to server main
            Intent intent = new Intent(this, ServerMainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error submitting order: " + e.getMessage(), Toast.LENGTH_LONG).show();
            android.util.Log.e("CartActivity", "Submit order error", e);
        }
    }

    private void clearCart() {
        NewOrderActivity.cart.clear();
        Toast.makeText(this, "Cart cleared", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void removeFromCart(String dishID) {
        NewOrderActivity.cart.remove(dishID);
        loadCart();

        if (NewOrderActivity.cart.isEmpty()) {
            finish();
        }
    }

    private void updateQuantity(String dishID, int newQuantity) {
        if (newQuantity <= 0) {
            removeFromCart(dishID);
        } else {
            NewOrderActivity.CartItem item = NewOrderActivity.cart.get(dishID);
            if (item != null) {
                item.quantity = newQuantity;
                calculateTotals();
                adapter.notifyDataSetChanged();
            }
        }
    }

    // Adapter for cart items
    private class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

        private List<NewOrderActivity.CartItem> items;

        public CartAdapter(List<NewOrderActivity.CartItem> items) {
            this.items = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_cart, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            NewOrderActivity.CartItem item = items.get(position);

            holder.tvName.setText(item.name);
            holder.tvPrice.setText("$" + item.price + " each");
            holder.tvQuantity.setText(String.valueOf(item.quantity));

            // Calculate line total as double first, then format
            double lineTotal = (double)(item.price * item.quantity);
            holder.tvLineTotal.setText(String.format("$%.2f", lineTotal));

            // Decrease quantity
            holder.btnDecrease.setOnClickListener(v -> {
                updateQuantity(item.dishID, item.quantity - 1);
            });

            // Increase quantity
            holder.btnIncrease.setOnClickListener(v -> {
                updateQuantity(item.dishID, item.quantity + 1);
            });

            // Remove item
            holder.btnRemove.setOnClickListener(v -> {
                removeFromCart(item.dishID);
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvPrice, tvQuantity, tvLineTotal;
            Button btnDecrease, btnIncrease, btnRemove;

            public ViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tv_cart_item_name);
                tvPrice = itemView.findViewById(R.id.tv_cart_item_price);
                tvQuantity = itemView.findViewById(R.id.tv_cart_quantity);
                tvLineTotal = itemView.findViewById(R.id.tv_line_total);
                btnDecrease = itemView.findViewById(R.id.btn_decrease);
                btnIncrease = itemView.findViewById(R.id.btn_increase);
                btnRemove = itemView.findViewById(R.id.btn_remove_item);
            }
        }
    }
}