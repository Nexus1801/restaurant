package com.example.restaurant;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.restaurant.util.DBOperator;

public class ServerMainActivity extends AppCompatActivity {

    private TextView tvWelcome, tvActiveTables, tvPendingOrders;
    private CardView cvMyTables, cvNewOrder, cvActiveOrders, cvMenu;
    private Button btnLogout;
    private DBOperator dbOperator;
    private String employeeId, employeeName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_main);

        dbOperator = DBOperator.getInstance();

        // Get server info from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("RestaurantApp", MODE_PRIVATE);
        employeeId = prefs.getString("employee_id", "");
        employeeName = prefs.getString("employee_name", "Server");

        // Initialize views
        tvWelcome = findViewById(R.id.tv_welcome);
        tvActiveTables = findViewById(R.id.tv_active_tables);
        tvPendingOrders = findViewById(R.id.tv_pending_orders);

        cvMyTables = findViewById(R.id.cv_my_tables);
        cvNewOrder = findViewById(R.id.cv_new_order);
        cvActiveOrders = findViewById(R.id.cv_active_orders);
        cvMenu = findViewById(R.id.cv_menu);

        btnLogout = findViewById(R.id.btn_logout);

        tvWelcome.setText("Welcome, " + employeeName);

        // Load dashboard metrics
        loadDashboardMetrics();

        // Set click listeners
        cvMyTables.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ServerMainActivity.this, TablesActivity.class));
            }
        });

        cvNewOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ServerMainActivity.this, NewOrderActivity.class));
            }
        });

        cvActiveOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ServerMainActivity.this, ActiveOrdersActivity.class));
            }
        });

        cvMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ServerMainActivity.this, MenuActivity.class));
            }
        });

        // Logout button click listener
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutConfirmation();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardMetrics();
    }

    private void loadDashboardMetrics() {
        try {
            // Count active tables for this server
            Cursor tablesCursor = dbOperator.execQuery(
                    "SELECT COUNT(DISTINCT Order_DT_id) FROM Orders " +
                            "WHERE Order_Emp_id = '" + employeeId + "' " +
                            "AND Order_status IN ('Open', 'Placed', 'Preparing', 'Ready')"
            );
            int activeTables = 0;
            if (tablesCursor != null && tablesCursor.moveToFirst()) {
                activeTables = tablesCursor.getInt(0);
                tablesCursor.close();
            }
            tvActiveTables.setText(String.valueOf(activeTables));

            // Count pending orders for this server
            Cursor ordersCursor = dbOperator.execQuery(
                    "SELECT COUNT(*) FROM Orders " +
                            "WHERE Order_Emp_id = '" + employeeId + "' " +
                            "AND Order_status IN ('Placed', 'Preparing')"
            );
            int pendingOrders = 0;
            if (ordersCursor != null && ordersCursor.moveToFirst()) {
                pendingOrders = ordersCursor.getInt(0);
                ordersCursor.close();
            }
            tvPendingOrders.setText(String.valueOf(pendingOrders));

        } catch (Exception e) {
            e.printStackTrace();
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
        Intent intent = new Intent(ServerMainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // Handle back button press
    @Override
    public void onBackPressed() {
        showLogoutConfirmation();
    }
}