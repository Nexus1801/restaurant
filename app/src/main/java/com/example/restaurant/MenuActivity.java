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

public class MenuActivity extends AppCompatActivity {

    private static final String TAG = "MenuActivity";
    private RecyclerView rvMenu;
    private TextView tvTitle;
    private DBOperator dbOperator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        dbOperator = DBOperator.getInstance();

        tvTitle = findViewById(R.id.tv_menu_title);
        rvMenu = findViewById(R.id.rv_menu);
        rvMenu.setLayoutManager(new LinearLayoutManager(this));

        loadMenu();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMenu();
    }

    private void loadMenu() {
        List<Object> menuItems = new ArrayList<>();

        try {
            Log.d(TAG, "Loading menu...");

            // Get all categories first
            Cursor catCursor = dbOperator.execQuery(
                    "SELECT DISTINCT Menu_cat FROM Menu ORDER BY Menu_cat"
            );

            List<String> categories = new ArrayList<>();
            if (catCursor != null) {
                while (catCursor.moveToNext()) {
                    categories.add(catCursor.getString(0));
                }
                catCursor.close();
            }

            Log.d(TAG, "Found " + categories.size() + " categories");

            // For each category, get menu items
            for (String category : categories) {
                // Add category header
                menuItems.add(new CategoryHeader(category));

                // Get items in this category
                Cursor itemCursor = dbOperator.execQuery(
                        "SELECT Menu_DishID, Menu_name, Menu_price, Menu_available " +
                                "FROM Menu WHERE Menu_cat = '" + category + "' ORDER BY Menu_name"
                );

                if (itemCursor != null) {
                    while (itemCursor.moveToNext()) {
                        MenuItem item = new MenuItem();
                        item.dishId = itemCursor.getString(0);
                        item.name = itemCursor.getString(1);
                        item.price = itemCursor.getInt(2);
                        item.available = itemCursor.getString(3);

                        menuItems.add(item);
                    }
                    itemCursor.close();
                }
            }

            Log.d(TAG, "Total items loaded: " + menuItems.size());

            if (menuItems.isEmpty()) {
                Toast.makeText(this, "No menu items found", Toast.LENGTH_SHORT).show();
            }

            MenuAdapter adapter = new MenuAdapter(menuItems);
            rvMenu.setAdapter(adapter);

        } catch (Exception e) {
            Log.e(TAG, "Error loading menu: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading menu: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // Data classes
    private static class CategoryHeader {
        String name;

        CategoryHeader(String name) {
            this.name = name;
        }
    }

    private static class MenuItem {
        String dishId;
        String name;
        int price;
        String available;
    }

    // Adapter with multiple view types
    private class MenuAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int TYPE_HEADER = 0;
        private static final int TYPE_ITEM = 1;

        private List<Object> items;

        public MenuAdapter(List<Object> items) {
            this.items = items;
        }

        @Override
        public int getItemViewType(int position) {
            if (items.get(position) instanceof CategoryHeader) {
                return TYPE_HEADER;
            } else {
                return TYPE_ITEM;
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == TYPE_HEADER) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_menu_category_header, parent, false);
                return new HeaderViewHolder(view);
            } else {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_menu_dish, parent, false);
                return new ItemViewHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof HeaderViewHolder) {
                CategoryHeader header = (CategoryHeader) items.get(position);
                ((HeaderViewHolder) holder).tvCategory.setText(header.name);
            } else if (holder instanceof ItemViewHolder) {
                MenuItem item = (MenuItem) items.get(position);
                ItemViewHolder itemHolder = (ItemViewHolder) holder;

                itemHolder.tvName.setText(item.name);
                itemHolder.tvPrice.setText("$" + item.price);

                // Show availability status
                boolean isAvailable = item.available != null && item.available.equalsIgnoreCase("Yes");
                itemHolder.tvAvailability.setText(isAvailable ? "Available" : "Unavailable");
                itemHolder.tvAvailability.setTextColor(isAvailable ?
                        android.graphics.Color.parseColor("#10B981") :
                        android.graphics.Color.parseColor("#EF4444"));

                // Dim unavailable items
                itemHolder.itemView.setAlpha(isAvailable ? 1.0f : 0.5f);
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class HeaderViewHolder extends RecyclerView.ViewHolder {
            TextView tvCategory;

            HeaderViewHolder(View itemView) {
                super(itemView);
                tvCategory = itemView.findViewById(R.id.tv_category_name);
            }
        }

        class ItemViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvPrice, tvAvailability;

            ItemViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tv_dish_name);
                tvPrice = itemView.findViewById(R.id.tv_dish_price);
                tvAvailability = itemView.findViewById(R.id.tv_dish_availability);
            }
        }
    }
}