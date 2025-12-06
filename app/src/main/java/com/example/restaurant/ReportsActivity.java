package com.example.restaurant;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.restaurant.constant.SQLCommand;
import com.example.restaurant.util.DBOperator;
import com.example.restaurant.view.TableView;

public class ReportsActivity extends AppCompatActivity {

    private Button btnSalesReport, btnTopSelling, btnPeakTimes, btnPaymentAnalysis;
    private ScrollView scrollResults;
    private LinearLayout reportContainer;
    private DBOperator dbOperator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        dbOperator = DBOperator.getInstance();

        btnSalesReport = findViewById(R.id.btn_sales_report);
        btnTopSelling = findViewById(R.id.btn_top_selling);
        btnPeakTimes = findViewById(R.id.btn_peak_times);
        btnPaymentAnalysis = findViewById(R.id.btn_payment_analysis);
        scrollResults = findViewById(R.id.scroll_report_results);

        // Create a LinearLayout container to hold multiple views inside ScrollView
        reportContainer = new LinearLayout(this);
        reportContainer.setOrientation(LinearLayout.VERTICAL);
        reportContainer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        scrollResults.addView(reportContainer);

        btnSalesReport.setOnClickListener(v -> showReport("Sales Summary",
                "SELECT 'Total Sales' as Metric, SUM(Order_Total) as Value FROM Orders " +
                        "UNION ALL SELECT 'Total Orders', COUNT(*) FROM Orders " +
                        "UNION ALL SELECT 'Avg Order', AVG(Order_Total) FROM Orders"));

        btnTopSelling.setOnClickListener(v -> showReport("Top 5 Best Sellers",
                SQLCommand.QUERY_TOP_SELLING));

        btnPeakTimes.setOnClickListener(v -> showReport("Peak Order Times",
                SQLCommand.QUERY_PEAK_TIMES));

        btnPaymentAnalysis.setOnClickListener(v -> showReport("Payment Analysis by Day",
                SQLCommand.QUERY_PAYMENT_BY_DAY));
    }

    private void showReport(String title, String query) {
        try {
            // Clear the container, not the ScrollView
            reportContainer.removeAllViews();

            // Create title TextView
            TextView tvTitle = new TextView(this);
            tvTitle.setText(title);
            tvTitle.setTextSize(20);
            tvTitle.setTextColor(getResources().getColor(android.R.color.black));
            tvTitle.setPadding(16, 16, 16, 16);

            // Add title to container
            reportContainer.addView(tvTitle);

            // Execute query and create table
            Cursor cursor = dbOperator.execQuery(query);
            if (cursor != null && cursor.getCount() > 0) {
                TableView tableView = new TableView(this, cursor);
                reportContainer.addView(tableView);

                Toast.makeText(this, title + " generated", Toast.LENGTH_SHORT).show();
            } else {
                // Show empty state
                TextView tvEmpty = new TextView(this);
                tvEmpty.setText("No data available for this report.");
                tvEmpty.setTextSize(16);
                tvEmpty.setPadding(16, 32, 16, 32);
                tvEmpty.setTextColor(getResources().getColor(android.R.color.darker_gray));
                reportContainer.addView(tvEmpty);

                Toast.makeText(this, "No data found", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error generating report: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}