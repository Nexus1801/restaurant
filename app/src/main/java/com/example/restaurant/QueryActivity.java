package com.example.restaurant;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.restaurant.constant.SQLCommand;
import com.example.restaurant.util.DBOperator;
import com.example.restaurant.view.TableView;

public class QueryActivity extends AppCompatActivity {

    private Spinner spinnerQuery;
    private Button btnExecute;
    private ScrollView scrollView;
    private DBOperator dbOperator;

    private String[] queryNames = {
            "Select a query",
            "1. Customer Order History (CUST001)",
            "2. Pending Reservations",
            "3. Active Orders by Status",
            "4. Available Menu Items",
            "5a. Top 5 Best Selling Items",
            "5b. Most Frequent Party Size",
            "6. Employee Shift Schedule",
            "7. Available Tables (Party Size 4+)",
            "8. Low Inventory Alerts",
            "9a. Highest Payment",
            "9b. Lowest Payment",
            "9c. Average Payment",
            "9d. Median Payment",
            "9e. Payments by Day of Week",
            "10. Order Details (ORD001)",
            "11. Customer Reservations (CUST001)",
            "12. Employee Section Assignments",
            "13. Peak Order Times"
    };

    private String[] queries = {
            "",
            SQLCommand.QUERY_CUSTOMER_ORDER_HISTORY,
            SQLCommand.QUERY_RESERVATIONS,
            SQLCommand.QUERY_ACTIVE_ORDERS,
            SQLCommand.QUERY_MENU_AVAILABLE,
            SQLCommand.QUERY_TOP_SELLING,
            SQLCommand.QUERY_FREQUENT_PARTY_SIZE,
            SQLCommand.QUERY_EMPLOYEE_SHIFTS,
            SQLCommand.QUERY_AVAILABLE_TABLES,
            SQLCommand.QUERY_LOW_INVENTORY,
            SQLCommand.QUERY_HIGHEST_PAYMENT,
            SQLCommand.QUERY_LOWEST_PAYMENT,
            SQLCommand.QUERY_MEAN_PAYMENT,
            SQLCommand.QUERY_MEDIAN_PAYMENT,
            SQLCommand.QUERY_PAYMENT_BY_DAY,
            SQLCommand.QUERY_ORDER_DETAILS,
            SQLCommand.QUERY_CUSTOMER_RESERVATIONS,
            SQLCommand.QUERY_SECTION_ASSIGNMENTS,
            SQLCommand.QUERY_PEAK_TIMES
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query);

        dbOperator = DBOperator.getInstance();

        // Initialize views
        spinnerQuery = findViewById(R.id.spinner_query);
        btnExecute = findViewById(R.id.btn_execute_query);
        scrollView = findViewById(R.id.scroll_results);

        // Setup spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, queryNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerQuery.setAdapter(adapter);

        // Execute query button
        btnExecute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = spinnerQuery.getSelectedItemPosition();
                if (position == 0) {
                    Toast.makeText(QueryActivity.this,
                            "Please select a query", Toast.LENGTH_SHORT).show();
                    return;
                }
                executeQuery(position);
            }
        });
    }

    private void executeQuery(int position) {
        try {
            scrollView.removeAllViews();

            String query = queries[position];
            Cursor cursor;

            // Handle queries with parameters
            switch (position) {
                case 1: // Customer Order History
                    cursor = dbOperator.execQuery(query, new String[]{"CUST001"});
                    break;
                case 8: // Available Tables
                    cursor = dbOperator.execQuery(query, new String[]{"4"});
                    break;
                case 15: // Order Details
                    cursor = dbOperator.execQuery(query, new String[]{"ORD001"});
                    break;
                case 16: // Customer Reservations
                    cursor = dbOperator.execQuery(query, new String[]{"CUST001"});
                    break;
                default:
                    cursor = dbOperator.execQuery(query);
                    break;
            }

            if (cursor != null) {
                TableView tableView = new TableView(this, cursor);
                scrollView.addView(tableView);

                int rowCount = cursor.getCount();
                Toast.makeText(this,
                        "Query executed: " + rowCount + " results",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No results found", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this,
                    "Query error: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }
}