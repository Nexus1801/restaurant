package com.example.restaurant;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.restaurant.util.DBOperator;

import java.util.ArrayList;
import java.util.List;

public class MenuManagementActivity extends AppCompatActivity {

    private static final String TAG = "MenuManagementActivity";
    private RecyclerView rvMenuMgmt;
    private DBOperator dbOperator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_management);

        Log.d(TAG, "onCreate started");

        // Initialize DBOperator
        dbOperator = DBOperator.getInstance();

        if (dbOperator == null) {
            Log.e(TAG, "DBOperator is null!");
            Toast.makeText(this, "Database error - DBOperator is null", Toast.LENGTH_LONG).show();
            return;
        }

        // Initialize views
        rvMenuMgmt = findViewById(R.id.rv_menu_mgmt);

        if (rvMenuMgmt == null) {
            Log.e(TAG, "RecyclerView not found! Check activity_menu_management.xml has rv_menu_mgmt");
            Toast.makeText(this, "Layout error - RecyclerView not found", Toast.LENGTH_LONG).show();
            return;
        }

        rvMenuMgmt.setLayoutManager(new LinearLayoutManager(this));

        // Load the menu
        loadMenu();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMenu();
    }

    private void loadMenu() {
        List<MenuItem> menuItems = new ArrayList<>();

        try {
            Log.d(TAG, "Loading menu from database...");

            // Simple query to get all menu items
            String query = "SELECT Menu_DishID, Menu_name, Menu_cat, Menu_price, Menu_available FROM Menu ORDER BY Menu_cat, Menu_name";
            Log.d(TAG, "Query: " + query);

            Cursor cursor = dbOperator.execQuery(query);

            if (cursor == null) {
                Log.e(TAG, "Cursor is null - query failed");
                Toast.makeText(this, "Error: Query returned null", Toast.LENGTH_LONG).show();
                return;
            }

            Log.d(TAG, "Cursor returned, column count: " + cursor.getColumnCount());

            // Log column names for debugging
            String[] columnNames = cursor.getColumnNames();
            for (int i = 0; i < columnNames.length; i++) {
                Log.d(TAG, "Column " + i + ": " + columnNames[i]);
            }

            int rowCount = 0;
            while (cursor.moveToNext()) {
                rowCount++;
                MenuItem item = new MenuItem();

                // Get column indices safely
                int idxDishId = cursor.getColumnIndex("Menu_DishID");
                int idxName = cursor.getColumnIndex("Menu_name");
                int idxCat = cursor.getColumnIndex("Menu_cat");
                int idxPrice = cursor.getColumnIndex("Menu_price");
                int idxAvailable = cursor.getColumnIndex("Menu_available");

                // Use indices if found, otherwise use position
                item.dishId = (idxDishId >= 0) ? cursor.getString(idxDishId) : cursor.getString(0);
                item.name = (idxName >= 0) ? cursor.getString(idxName) : cursor.getString(1);
                item.category = (idxCat >= 0) ? cursor.getString(idxCat) : cursor.getString(2);
                item.price = (idxPrice >= 0) ? cursor.getInt(idxPrice) : cursor.getInt(3);
                item.available = (idxAvailable >= 0) ? cursor.getString(idxAvailable) : cursor.getString(4);

                Log.d(TAG, "Loaded item: " + item.name + " - $" + item.price);
                menuItems.add(item);
            }
            cursor.close();

            Log.d(TAG, "Total menu items loaded: " + rowCount);

            if (menuItems.isEmpty()) {
                Toast.makeText(this, "No menu items found in database", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Menu table is empty!");
            } else {
                Toast.makeText(this, "Loaded " + menuItems.size() + " menu items", Toast.LENGTH_SHORT).show();
            }

            // Set adapter
            MenuAdapter adapter = new MenuAdapter(menuItems);
            rvMenuMgmt.setAdapter(adapter);

            Log.d(TAG, "Adapter set with " + menuItems.size() + " items");

        } catch (Exception e) {
            Log.e(TAG, "Error loading menu: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading menu: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private static class MenuItem {
        String dishId;
        String name;
        String category;
        int price;
        String available;
    }

    private class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.ViewHolder> {
        private List<MenuItem> items;

        public MenuAdapter(List<MenuItem> items) {
            this.items = items;
            Log.d(TAG, "MenuAdapter created with " + items.size() + " items");
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Log.d(TAG, "onCreateViewHolder called");
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_menu_mgmt, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            MenuItem item = items.get(position);
            Log.d(TAG, "onBindViewHolder position " + position + ": " + item.name);

            holder.tvName.setText(item.name);
            holder.tvCategory.setText(item.category);
            holder.tvPrice.setText("$" + item.price);

            String availText = (item.available != null && item.available.equalsIgnoreCase("Yes"))
                    ? "Available" : "Unavailable";
            holder.tvAvailable.setText(availText);

            // Color code availability
            if (item.available != null && item.available.equalsIgnoreCase("Yes")) {
                holder.tvAvailable.setTextColor(android.graphics.Color.parseColor("#10B981"));
            } else {
                holder.tvAvailable.setTextColor(android.graphics.Color.parseColor("#EF4444"));
            }

            holder.itemView.setOnClickListener(v -> {
                Toast.makeText(MenuManagementActivity.this,
                        item.name + " - $" + item.price, Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvCategory, tvPrice, tvAvailable;

            public ViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tv_menu_item_name);
                tvCategory = itemView.findViewById(R.id.tv_menu_item_category);
                tvPrice = itemView.findViewById(R.id.tv_menu_item_price);
                tvAvailable = itemView.findViewById(R.id.tv_menu_item_available);

                // Debug: Check if views are found
                if (tvName == null) Log.e(TAG, "tvName is null - check item_menu_mgmt.xml");
                if (tvCategory == null) Log.e(TAG, "tvCategory is null");
                if (tvPrice == null) Log.e(TAG, "tvPrice is null");
                if (tvAvailable == null) Log.e(TAG, "tvAvailable is null");
            }
        }
    }
}