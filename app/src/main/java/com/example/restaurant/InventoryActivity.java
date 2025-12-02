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

public class InventoryActivity extends AppCompatActivity {

    private RecyclerView rvInventory;
    private TextView tvLowStockCount;
    private Button btnRefresh;
    private DBOperator dbOperator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        dbOperator = DBOperator.getInstance();

        rvInventory = findViewById(R.id.rv_inventory);
        tvLowStockCount = findViewById(R.id.tv_low_stock_count);
        btnRefresh = findViewById(R.id.btn_refresh_inventory);

        rvInventory.setLayoutManager(new LinearLayoutManager(this));

        loadInventory();

        btnRefresh.setOnClickListener(v -> {
            loadInventory();
            Toast.makeText(this, "Inventory refreshed", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadInventory() {
        List<InventoryItem> items = new ArrayList<>();

        try {
            // Get all inventory items
            String query =
                    "SELECT i.Inv_ItemID, m.Menu_name, i.Inv_item_count, i.Inv_Hard_restock " +
                            "FROM Inventory i " +
                            "JOIN Menu m ON i.Inv_Menu_dishID = m.Menu_DishID " +
                            "ORDER BY i.Inv_item_count";

            Cursor cursor = dbOperator.execQuery(query);

            int lowStockCount = 0;

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    InventoryItem item = new InventoryItem();
                    item.itemId = cursor.getString(0);
                    item.itemName = cursor.getString(1);
                    item.currentStock = cursor.getInt(2);
                    item.restockLevel = cursor.getString(3);

                    items.add(item);

                    try {
                        if (item.currentStock <= Integer.parseInt(item.restockLevel)) {
                            lowStockCount++;
                        }
                    } catch (Exception e) {
                        // Ignore parse errors
                    }
                }
                cursor.close();
            }

            tvLowStockCount.setText(lowStockCount + " items low");

            InventoryAdapter adapter = new InventoryAdapter(items);
            rvInventory.setAdapter(adapter);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class InventoryItem {
        String itemId;
        String itemName;
        int currentStock;
        String restockLevel;
    }

    private class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.ViewHolder> {
        private List<InventoryItem> items;

        public InventoryAdapter(List<InventoryItem> items) {
            this.items = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_inventory, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            InventoryItem item = items.get(position);

            holder.tvItemName.setText(item.itemName);
            holder.tvCurrentStock.setText("Stock: " + item.currentStock);
            holder.tvRestockLevel.setText("Restock at: " + item.restockLevel);

            // Color code by stock level
            try {
                int restock = Integer.parseInt(item.restockLevel);
                if (item.currentStock <= restock) {
                    holder.itemView.setBackgroundColor(android.graphics.Color.parseColor("#FEE2E2"));
                } else if (item.currentStock <= restock * 1.5) {
                    holder.itemView.setBackgroundColor(android.graphics.Color.parseColor("#FEF3C7"));
                } else {
                    holder.itemView.setBackgroundColor(android.graphics.Color.parseColor("#F9FAFB"));
                }
            } catch (Exception e) {
                // Ignore
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvItemName, tvCurrentStock, tvRestockLevel;

            public ViewHolder(View itemView) {
                super(itemView);
                tvItemName = itemView.findViewById(R.id.tv_inv_item_name);
                tvCurrentStock = itemView.findViewById(R.id.tv_inv_current_stock);
                tvRestockLevel = itemView.findViewById(R.id.tv_inv_restock_level);
            }
        }
    }
}

