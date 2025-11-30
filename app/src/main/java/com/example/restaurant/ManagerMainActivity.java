package com.example.restaurant;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.restaurant.constant.SQLCommand;
import com.example.restaurant.util.DBOperator;

import java.text.NumberFormat;
import java.util.Locale;

public class ManagerMainActivity extends AppCompatActivity {

    private TextView tvWelcome, tvTotalSales, tvTotalOrders, tvAvgOrder, tvActiveStaff;
    private CardView cvReports, cvStaffMgmt, cvInventory, cvMenuMgmt, cvAnalytics, cvFloorPlan;
    private DBOperator dbOperator;
    private String employeeName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_main);

        dbOperator = DBOperator.getInstance();

        // Get manager info
        SharedPreferences prefs = getSharedPreferences("RestaurantApp", MODE_PRIVATE);
        employeeName = prefs.getString("employee_name", "Manager");

        // Initialize views
        tvWelcome = findViewById(R.id.tv_welcome);
        tvTotalSales = findViewById(R.id.tv_total_sales);
        tvTotalOrders = findViewById(R.id.tv_total_orders);
        tvAvgOrder = findViewById(R.id.tv_avg_order);
        tvActiveStaff = findViewById(R.id.tv_active_staff);

        cvReports = findViewById(R.id.cv_reports);
        cvStaffMgmt = findViewById(R.id.cv_staff_mgmt);
        cvInventory = findViewById(R.id.cv_inventory);
        cvMenuMgmt = findViewById(R.id.cv_menu_mgmt);
        cvAnalytics = findViewById(R.id.cv_analytics);
        cvFloorPlan = findViewById(R.id.cv_floor_plan);

        tvWelcome.setText("Welcome, " + employeeName);

        // Load dashboard metrics
        loadDashboardMetrics();

        // Set click listeners
        cvReports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ManagerMainActivity.this, ReportsActivity.class));
            }
        });

        cvStaffMgmt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ManagerMainActivity.this, StaffManagementActivity.class));
            }
        });

        cvInventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ManagerMainActivity.this, InventoryActivity.class));
            }
        });

        cvMenuMgmt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ManagerMainActivity.this, MenuManagementActivity.class));
            }
        });

        cvAnalytics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ManagerMainActivity.this, QueryActivity.class));
            }
        });

        cvFloorPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ManagerMainActivity.this, TablesActivity.class));
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
            // Total Sales
            Cursor salesCursor = dbOperator.execQuery(
                    "SELECT SUM(Order_Total) FROM Orders WHERE Order_status NOT IN ('Cancelled')"
            );
            double totalSales = 0;
            if (salesCursor != null && salesCursor.moveToFirst()) {
                totalSales = salesCursor.getDouble(0);
                salesCursor.close();
            }
            NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.US);
            tvTotalSales.setText(currency.format(totalSales));

            // Total Orders
            Cursor ordersCursor = dbOperator.execQuery(
                    "SELECT COUNT(*) FROM Orders WHERE Order_status NOT IN ('Cancelled')"
            );
            int totalOrders = 0;
            if (ordersCursor != null && ordersCursor.moveToFirst()) {
                totalOrders = ordersCursor.getInt(0);
                ordersCursor.close();
            }
            tvTotalOrders.setText(String.valueOf(totalOrders));

            // Average Order Value
            double avgOrder = totalOrders > 0 ? totalSales / totalOrders : 0;
            tvAvgOrder.setText(currency.format(avgOrder));

            // Active Staff (currently on shift)
            Cursor staffCursor = dbOperator.execQuery(
                    "SELECT COUNT(*) FROM Employee"
            );
            int activeStaff = 0;
            if (staffCursor != null && staffCursor.moveToFirst()) {
                activeStaff = staffCursor.getInt(0);
                staffCursor.close();
            }
            tvActiveStaff.setText(String.valueOf(activeStaff));

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading metrics", Toast.LENGTH_SHORT).show();
        }
    }
}
