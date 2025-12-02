package com.example.restaurant;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.restaurant.constant.SQLCommand;
import com.example.restaurant.util.DBOperator;

import java.util.ArrayList;
import java.util.List;

public class TablesActivity extends AppCompatActivity {

    private RecyclerView rvTables;
    private TableAdapter adapter;
    private DBOperator dbOperator;
    private List<Table> tableList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tables);

        dbOperator = DBOperator.getInstance();

        // Initialize views
        rvTables = findViewById(R.id.rv_tables);
        rvTables.setLayoutManager(new GridLayoutManager(this, 2));

        // Load tables
        loadTables();
    }

    private void loadTables() {
        tableList = new ArrayList<>();

        try {
            Cursor cursor = dbOperator.execQuery(SQLCommand.QUERY_ALL_TABLES);

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Table table = new Table();

                    // Database structure (4 columns):
                    // 0: DT_id (CHAR)
                    // 1: DT_number (INT)
                    // 2: DT_party_size (INT)
                    // 3: DT_table_type (VARCHAR)

                    table.id = cursor.getString(0);        // DT_id
                    table.number = cursor.getInt(1);       // DT_number
                    table.partySize = cursor.getInt(2);    // DT_party_size
                    table.type = cursor.getString(3);      // DT_table_type

                    tableList.add(table);
                }
                cursor.close();
            }

            if (tableList.isEmpty()) {
                Toast.makeText(this, "No tables found in database", Toast.LENGTH_LONG).show();
            } else {
                adapter = new TableAdapter(tableList);
                rvTables.setAdapter(adapter);
                Toast.makeText(this, tableList.size() + " tables loaded", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading tables: " + e.getMessage(), Toast.LENGTH_LONG).show();
            android.util.Log.e("TablesActivity", "Error: ", e);
        }
    }

    // Table model class
    private static class Table {
        String id;
        int number;
        int partySize;
        String type;
    }

    // Adapter for RecyclerView
    private class TableAdapter extends RecyclerView.Adapter<TableAdapter.ViewHolder> {

        private List<Table> tables;

        public TableAdapter(List<Table> tables) {
            this.tables = tables;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_table, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Table table = tables.get(position);

            holder.tvTableNumber.setText("Table " + table.number);
            holder.tvTableType.setText(table.type != null ? table.type : "Standard");
            holder.tvPartySize.setText(table.partySize + " seats");

            int backgroundColor, textColor;

            String type = table.type != null ? table.type.toLowerCase() : "standard";

            switch (type) {
                case "booth":
                    backgroundColor = Color.parseColor("#DBEAFE"); // Light blue
                    textColor = Color.parseColor("#1E40AF");
                    break;
                case "outdoor":
                    backgroundColor = Color.parseColor("#D1FAE5"); // Light green
                    textColor = Color.parseColor("#065F46");
                    break;
                case "window":
                    backgroundColor = Color.parseColor("#FEF3C7"); // Light yellow
                    textColor = Color.parseColor("#92400E");
                    break;
                case "party":
                case "large":
                    backgroundColor = Color.parseColor("#FCE7F3"); // Light pink
                    textColor = Color.parseColor("#9F1239");
                    break;
                default: // Standard
                    backgroundColor = Color.parseColor("#F3F4F6"); // Light gray
                    textColor = Color.parseColor("#374151");
                    break;
            }

            holder.cardView.setCardBackgroundColor(backgroundColor);
            holder.tvTableType.setTextColor(textColor);

            holder.itemView.setOnClickListener(v -> {
                Toast.makeText(TablesActivity.this,
                        "Table " + table.number + " (" + table.type + ") - " + table.partySize + " seats",
                        Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public int getItemCount() {
            return tables.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            CardView cardView;
            TextView tvTableNumber, tvTableType, tvPartySize;

            public ViewHolder(View itemView) {
                super(itemView);
                cardView = itemView.findViewById(R.id.card_table);
                tvTableNumber = itemView.findViewById(R.id.tv_table_number);
                tvTableType = itemView.findViewById(R.id.tv_table_type);
                tvPartySize = itemView.findViewById(R.id.tv_party_size);
            }
        }
    }
}