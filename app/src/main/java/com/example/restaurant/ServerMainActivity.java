package com.example.restaurant;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.restaurant.constant.SQLCommand;
import com.example.restaurant.util.DBOperator;

public class ServerMainActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private CardView cvMyTables, cvNewOrder, cvActiveOrders, cvMenu;
    private TextView tvActiveOrdersCount;
    private DBOperator dbOperator;
    private String employeeId, employeeName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_main);

        dbOperator = DBOperator.getInstance();

        // Get employee info
        SharedPreferences prefs = getSharedPreferences("RestaurantApp", MODE_PRIVATE);
        employeeId = prefs.getString("employee_id", "");
        employeeName = prefs.getString("employee_name", "");

        // Initialize views
        tvWelcome = findViewById(R.id.tv_welcome);
        cvMyTables = findViewById(R.id.cv_my_tables);
        cvNewOrder = findViewById(R.id.cv_new_order);
        cvActiveOrders = findViewById(R.id.cv_active_orders);
        cvMenu = findViewById(R.id.cv_menu);
        tvActiveOrdersCount = findViewById(R.id.tv_active_orders_count);

        tvWelcome.setText("Welcome, " + employeeName);

        // Load active orders count
        loadActiveOrdersCount();

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadActiveOrdersCount();
    }

    private void loadActiveOrdersCount() {
        try {
            Cursor cursor = dbOperator.execQuery(SQLCommand.QUERY_ACTIVE_ORDERS);
            int count = 0;
            if (cursor != null) {
                count = cursor.getCount();
                cursor.close();
            }
            tvActiveOrdersCount.setText(count + " Pending");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
