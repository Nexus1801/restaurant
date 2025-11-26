package com.example.restaurant;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.restaurant.constant.SQLCommand;
import com.example.restaurant.util.DBOperator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewOrderActivity extends AppCompatActivity {

    private Spinner spinnerTable;
    private EditText etSearch;
    private RecyclerView rvMenuItems;
    private TextView tvCartCount;
    private ImageButton btnCart;
    private DBOperator dbOperator;
    private MenuAdapter adapter;
    private List<MenuItem> menuItems;
    private List<MenuItem> filteredItems;
    private Map<String, Integer> cart; // dishID -> quantity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_order);

        dbOperator = DBOperator.getInstance();
        cart = new HashMap<>();

        // Initialize views
        spinnerTable = findViewById(R.id.spinner_table);
        etSearch = findViewById(R.id.et_search);
        rvMenuItems = findViewById(R.id.rv_menu_items);
        tvCartCount = findViewById(R.id.tv_cart_count);
        btnCart = findViewById(R.id.btn_cart);

        rvMenuItems.setLayoutManager(new GridLayoutManager(this, 2));

        // Load available tables
        loadAvailableTables();

        // Load menu items
        loadMenuItems();

        // Search functionality
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterMenuItems(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Cart button
        btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cart.isEmpty()) {
                    Toast.makeText(NewOrderActivity.this, "Cart is empty", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(NewOrderActivity.this, CartActivity.class);
                    // Pass cart data
                    startActivity(intent);
                }
            }
        });
    }

    private void loadAvailableTables() {
        List<String> tables = new ArrayList<>();
        tables.add("Select Table");

        try {
            Cursor cursor = dbOperator.execQuery(
                    "SELECT DT_number FROM Dining_table WHERE DT_status = 'Available' ORDER BY DT_number"
            );

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    tables.add("Table " + cursor.getInt(0));
                }
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, tables);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTable.setAdapter(adapter);
    }

    private void loadMenuItems() {
        menuItems = new ArrayList<>();

        try {
            Cursor cursor = dbOperator.execQuery(SQLCommand.QUERY_MENU_AVAILABLE);

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    MenuItem item = new MenuItem();
                    item.dishID = cursor.getString(0);
                    item.name = cursor.getString(1);
                    item.category = cursor.getString(2);
                    item.price = cursor.getInt(3);
                    item.available = cursor.getString(4);

                    menuItems.add(item);
                }
                cursor.close();
            }

            filteredItems = new ArrayList<>(menuItems);
            adapter = new MenuAdapter(filteredItems);
            rvMenuItems.setAdapter(adapter);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading menu", Toast.LENGTH_SHORT).show();
        }
    }

    private void filterMenuItems(String query) {
        filteredItems.clear();

        if (query.isEmpty()) {
            filteredItems.addAll(menuItems);
        } else {
            String lowerQuery = query.toLowerCase();
            for (MenuItem item : menuItems) {
                if (item.name.toLowerCase().contains(lowerQuery) ||
                        item.category.toLowerCase().contains(lowerQuery)) {
                    filteredItems.add(item);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void addToCart(MenuItem item) {
        if (cart.containsKey(item.dishID)) {
            cart.put(item.dishID, cart.get(item.dishID) + 1);
        } else {
            cart.put(item.dishID, 1);
        }
        updateCartCount();
        Toast.makeText(this, item.name + " added to cart", Toast.LENGTH_SHORT).show();
    }

    private void updateCartCount() {
        int totalItems = 0;
        for (int qty : cart.values()) {
            totalItems += qty;
        }
        tvCartCount.setText(String.valueOf(totalItems));
        tvCartCount.setVisibility(totalItems > 0 ? View.VISIBLE : View.GONE);
    }

    // MenuItem model class
    private static class MenuItem {
        String dishID;
        String name;
        String category;
        int price;
        String available;
    }

    // Adapter for menu items
    private class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.ViewHolder> {

        private List<MenuItem> items;

        public MenuAdapter(List<MenuItem> items) {
            this.items = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_menu, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            MenuItem item = items.get(position);

            holder.tvName.setText(item.name);
            holder.tvCategory.setText(item.category);
            holder.tvPrice.setText("$" + item.price);

            holder.btnAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addToCart(item);
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvCategory, tvPrice;
            Button btnAdd;

            public ViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tv_item_name);
                tvCategory = itemView.findViewById(R.id.tv_item_category);
                tvPrice = itemView.findViewById(R.id.tv_item_price);
                btnAdd = itemView.findViewById(R.id.btn_add_item);
            }
        }
    }
}
