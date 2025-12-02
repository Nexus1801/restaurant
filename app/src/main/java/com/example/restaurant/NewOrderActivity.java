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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewOrderActivity extends AppCompatActivity {

    private Spinner spinnerTable;
    private EditText etSearch;
    private RecyclerView rvMenuItems;
    private TextView tvCartCount, tvCartItemsCount;
    private ImageButton btnCart;
    private Button btnViewCart;
    private DBOperator dbOperator;
    private MenuAdapter adapter;
    private List<MenuItem> menuItems;
    private List<MenuItem> filteredItems;
    private String currentFilter = "All";

    // Cart: Map of dishID -> CartItem
    public static Map<String, CartItem> cart = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_order);

        dbOperator = DBOperator.getInstance();

        // Initialize views
        spinnerTable = findViewById(R.id.spinner_table);
        etSearch = findViewById(R.id.et_search);
        rvMenuItems = findViewById(R.id.rv_menu_items);
        tvCartCount = findViewById(R.id.tv_cart_count);
        tvCartItemsCount = findViewById(R.id.tv_cart_items_count);
        btnCart = findViewById(R.id.btn_cart);
        btnViewCart = findViewById(R.id.btn_view_cart);

        rvMenuItems.setLayoutManager(new GridLayoutManager(this, 2));

        // Load available tables
        loadAvailableTables();

        // Load menu items
        loadMenuItems();

        // Update cart count display
        updateCartCount();

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

        // Category filter buttons
        findViewById(R.id.btn_filter_all).setOnClickListener(v -> filterByCategory("All"));
        findViewById(R.id.btn_filter_entree).setOnClickListener(v -> filterByCategory("Entree"));
        findViewById(R.id.btn_filter_appetizer).setOnClickListener(v -> filterByCategory("Appetizer"));
        findViewById(R.id.btn_filter_side).setOnClickListener(v -> filterByCategory("Side"));
        findViewById(R.id.btn_filter_drink).setOnClickListener(v -> filterByCategory("Drink"));
        findViewById(R.id.btn_filter_dessert).setOnClickListener(v -> filterByCategory("Dessert"));

        // Cart button (top right icon)
        btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCart();
            }
        });

        // View Cart button (bottom bar)
        btnViewCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCart();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update cart count when returning from cart
        updateCartCount();
    }

    private void openCart() {
        if (cart.isEmpty()) {
            Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show();
        } else {
            // Get selected table
            String selectedTable = spinnerTable.getSelectedItem().toString();

            // Navigate to cart
            Intent intent = new Intent(NewOrderActivity.this, CartActivity.class);
            intent.putExtra("selected_table", selectedTable);
            startActivity(intent);
        }
    }

    private void loadAvailableTables() {
        List<String> tables = new ArrayList<>();
        tables.add("Select Table");

        try {
            // FIXED: No DT_status column - only select 4 columns
            String query = "SELECT DT_number FROM Dining_table ORDER BY DT_number";
            Cursor cursor = dbOperator.execQuery(query);

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    tables.add("Table " + cursor.getInt(0));
                }
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading tables: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "Error loading menu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void filterByCategory(String category) {
        currentFilter = category;
        filteredItems.clear();

        if (category.equals("All")) {
            filteredItems.addAll(menuItems);
        } else {
            for (MenuItem item : menuItems) {
                if (item.category.equalsIgnoreCase(category)) {
                    filteredItems.add(item);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void filterMenuItems(String query) {
        filteredItems.clear();

        if (query.isEmpty()) {
            // Apply category filter
            if (currentFilter.equals("All")) {
                filteredItems.addAll(menuItems);
            } else {
                for (MenuItem item : menuItems) {
                    if (item.category.equalsIgnoreCase(currentFilter)) {
                        filteredItems.add(item);
                    }
                }
            }
        } else {
            // Search within current category
            String lowerQuery = query.toLowerCase();
            List<MenuItem> itemsToSearch = currentFilter.equals("All") ? menuItems :
                    menuItems.stream()
                            .filter(item -> item.category.equalsIgnoreCase(currentFilter))
                            .collect(java.util.stream.Collectors.toList());

            for (MenuItem item : itemsToSearch) {
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
            // Increment quantity
            CartItem cartItem = cart.get(item.dishID);
            cartItem.quantity++;
        } else {
            // Add new item
            CartItem cartItem = new CartItem();
            cartItem.dishID = item.dishID;
            cartItem.name = item.name;
            cartItem.price = item.price;
            cartItem.quantity = 1;
            cart.put(item.dishID, cartItem);
        }

        updateCartCount();
        Toast.makeText(this, item.name + " added to cart", Toast.LENGTH_SHORT).show();
    }

    private void updateCartCount() {
        int totalItems = 0;
        for (CartItem item : cart.values()) {
            totalItems += item.quantity;
        }

        // Update badge on top right icon
        if (totalItems > 0) {
            tvCartCount.setText(String.valueOf(totalItems));
            tvCartCount.setVisibility(View.VISIBLE);
        } else {
            tvCartCount.setVisibility(View.GONE);
        }

        // Update bottom bar text
        tvCartItemsCount.setText(totalItems + " item" + (totalItems != 1 ? "s" : ""));
    }

    // MenuItem model class
    public static class MenuItem {
        public String dishID;
        public String name;
        public String category;
        public int price;
        public String available;
    }

    // CartItem model class
    public static class CartItem implements Serializable {
        public String dishID;
        public String name;
        public int price;
        public int quantity;
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