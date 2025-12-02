package com.example.restaurant;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.restaurant.constant.SQLCommand;
import com.example.restaurant.util.DBOperator;
import com.example.restaurant.view.TableView;

import java.util.ArrayList;
import java.util.List;

public class ReportsActivity extends AppCompatActivity {

    private Button btnSalesReport, btnTopSelling, btnPeakTimes, btnPaymentAnalysis;
    private ScrollView scrollResults;
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
            scrollResults.removeAllViews();

            TextView tvTitle = new TextView(this);
            tvTitle.setText(title);
            tvTitle.setTextSize(20);
            tvTitle.setPadding(16, 16, 16, 16);

            Cursor cursor = dbOperator.execQuery(query);
            if (cursor != null) {
                TableView tableView = new TableView(this, cursor);
                scrollResults.addView(tvTitle);
                scrollResults.addView(tableView);

                Toast.makeText(this, title + " generated", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error generating report", Toast.LENGTH_SHORT).show();
        }
    }
}
