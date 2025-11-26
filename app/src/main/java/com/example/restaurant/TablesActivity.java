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
                    table.id = cursor.getString(0);
                    table.number = cursor.getInt(1);
                    table.partySize = cursor.getInt(2);
                    table.type = cursor.getString(3);
                    table.status = cursor.getString(4);

                    tableList.add(table);
                }
                cursor.close();
            }

            adapter = new TableAdapter(tableList);
            rvTables.setAdapter(adapter);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading tables", Toast.LENGTH_SHORT).show();
        }
    }

    // Table model class
    private static class Table {
        String id;
        int number;
        int partySize;
        String type;
        String status;
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
            holder.tvTableType.setText(table.type);
            holder.tvPartySize.setText(table.partySize + " seats");
            holder.tvStatus.setText(table.status);

            // Color code by status
            int backgroundColor, statusColor;
            if (table.status.equals("Available")) {
                backgroundColor = Color.parseColor("#DCFCE7");
                statusColor = Color.parseColor("#166534");
            } else if (table.status.equals("Occupied")) {
                backgroundColor = Color.parseColor("#FEE2E2");
                statusColor = Color.parseColor("#991B1B");
            } else {
                backgroundColor = Color.parseColor("#FEF3C7");
                statusColor = Color.parseColor("#92400E");
            }

            holder.cardView.setCardBackgroundColor(backgroundColor);
            holder.tvStatus.setTextColor(statusColor);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(TablesActivity.this,
                            "Table " + table.number + " - " + table.status,
                            Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return tables.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            CardView cardView;
            TextView tvTableNumber, tvTableType, tvPartySize, tvStatus;

            public ViewHolder(View itemView) {
                super(itemView);
                cardView = itemView.findViewById(R.id.card_table);
                tvTableNumber = itemView.findViewById(R.id.tv_table_number);
                tvTableType = itemView.findViewById(R.id.tv_table_type);
                tvPartySize = itemView.findViewById(R.id.tv_party_size);
                tvStatus = itemView.findViewById(R.id.tv_status);
            }
        }
    }
}
