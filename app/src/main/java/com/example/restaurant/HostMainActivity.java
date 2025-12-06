package com.example.restaurant;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.restaurant.util.DBOperator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HostMainActivity extends AppCompatActivity {

    private TextView tvWelcome, tvPendingReservations, tvAvailableTables;
    private CardView cvFloorPlan, cvReservations, cvWaitlist, cvSeating;
    private RecyclerView rvUpcomingReservations;
    private Button btnNewReservation, btnLogout;
    private DBOperator dbOperator;
    private String employeeName;

    // For date/time picker
    private Calendar selectedDateTime = Calendar.getInstance();
    private TextView tvSelectedDateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_main);

        dbOperator = DBOperator.getInstance();

        // Get host info
        SharedPreferences prefs = getSharedPreferences("RestaurantApp", MODE_PRIVATE);
        employeeName = prefs.getString("employee_name", "Host");

        // Initialize views
        tvWelcome = findViewById(R.id.tv_welcome);
        tvPendingReservations = findViewById(R.id.tv_pending_reservations);
        tvAvailableTables = findViewById(R.id.tv_available_tables);

        cvFloorPlan = findViewById(R.id.cv_floor_plan);
        cvReservations = findViewById(R.id.cv_reservations);
        cvWaitlist = findViewById(R.id.cv_waitlist);
        cvSeating = findViewById(R.id.cv_seating);

        rvUpcomingReservations = findViewById(R.id.rv_upcoming_reservations);
        rvUpcomingReservations.setLayoutManager(new LinearLayoutManager(this));

        btnNewReservation = findViewById(R.id.btn_new_reservation);
        btnLogout = findViewById(R.id.btn_logout);

        tvWelcome.setText("Welcome, " + employeeName);

        // Load dashboard data
        loadDashboardData();
        loadUpcomingReservations();

        // Set click listeners
        cvFloorPlan.setOnClickListener(v -> {
            startActivity(new Intent(HostMainActivity.this, TablesActivity.class));
        });

        cvReservations.setOnClickListener(v -> {
            startActivity(new Intent(HostMainActivity.this, ReservationsActivity.class));
        });

        cvWaitlist.setOnClickListener(v -> {
            Toast.makeText(this, "Waitlist feature coming soon", Toast.LENGTH_SHORT).show();
        });

        cvSeating.setOnClickListener(v -> {
            showSeatingDialog();
        });

        btnNewReservation.setOnClickListener(v -> {
            showNewReservationDialog();
        });

        // Logout button click listener
        btnLogout.setOnClickListener(v -> {
            showLogoutConfirmation();
        });
    }

    // Handle back button press
    @Override
    public void onBackPressed() {
        showLogoutConfirmation();
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    logout();
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void logout() {
        // Clear saved preferences
        SharedPreferences prefs = getSharedPreferences("RestaurantApp", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        // Show logout message
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Navigate back to LoginActivity
        Intent intent = new Intent(HostMainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardData();
        loadUpcomingReservations();
    }

    private void loadDashboardData() {
        try {
            // Count pending reservations
            Cursor resCursor = dbOperator.execQuery(
                    "SELECT COUNT(*) FROM Reservation WHERE Res_status = 'Confirmed'"
            );
            int pendingRes = 0;
            if (resCursor != null && resCursor.moveToFirst()) {
                pendingRes = resCursor.getInt(0);
                resCursor.close();
            }
            tvPendingReservations.setText(String.valueOf(pendingRes));

            // Count available tables (tables not in active orders)
            Cursor tableCursor = dbOperator.execQuery(
                    "SELECT COUNT(*) FROM Dining_table WHERE DT_id NOT IN " +
                            "(SELECT Order_DT_id FROM Orders WHERE Order_status IN ('Open', 'Placed', 'Preparing'))"
            );
            int availableTables = 0;
            if (tableCursor != null && tableCursor.moveToFirst()) {
                availableTables = tableCursor.getInt(0);
                tableCursor.close();
            }
            tvAvailableTables.setText(String.valueOf(availableTables));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadUpcomingReservations() {
        List<Reservation> reservations = new ArrayList<>();

        try {
            Cursor cursor = dbOperator.execQuery(
                    "SELECT r.Res_id, c.Cust_name, c.Cust_Number, r.Res_party_size, " +
                            "r.Res_date, r.Res_status, d.DT_number " +
                            "FROM Reservation r " +
                            "JOIN Customer c ON r.Res_Cust_id = c.Cust_id " +
                            "JOIN Dining_table d ON r.Res_DT_id = d.DT_id " +
                            "WHERE r.Res_status IN ('Confirmed', 'Pending') " +
                            "ORDER BY r.Res_date LIMIT 10"
            );

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Reservation res = new Reservation();
                    res.id = cursor.getString(0);
                    res.customerName = cursor.getString(1);
                    res.phone = cursor.getString(2);
                    res.partySize = cursor.getInt(3);
                    res.dateTime = cursor.getString(4);
                    res.status = cursor.getString(5);
                    res.tableNumber = cursor.getInt(6);

                    reservations.add(res);
                }
                cursor.close();
            }

            ReservationAdapter adapter = new ReservationAdapter(reservations);
            rvUpcomingReservations.setAdapter(adapter);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showNewReservationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_new_reservation, null);
        builder.setView(dialogView);
        builder.setTitle("New Reservation");

        EditText etCustomerName = dialogView.findViewById(R.id.et_customer_name);
        EditText etPhone = dialogView.findViewById(R.id.et_phone);
        EditText etPartySize = dialogView.findViewById(R.id.et_party_size);
        Button btnSelectDate = dialogView.findViewById(R.id.btn_select_date);
        Button btnSelectTime = dialogView.findViewById(R.id.btn_select_time);
        tvSelectedDateTime = dialogView.findViewById(R.id.tv_selected_datetime);

        // Reset selected date/time
        selectedDateTime = Calendar.getInstance();
        updateDateTimeDisplay();

        // Date picker - Beyond Basics Feature!
        btnSelectDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        selectedDateTime.set(Calendar.YEAR, year);
                        selectedDateTime.set(Calendar.MONTH, month);
                        selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateDateTimeDisplay();
                    },
                    selectedDateTime.get(Calendar.YEAR),
                    selectedDateTime.get(Calendar.MONTH),
                    selectedDateTime.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        // Time picker - Beyond Basics Feature!
        btnSelectTime.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    this,
                    (view, hourOfDay, minute) -> {
                        selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        selectedDateTime.set(Calendar.MINUTE, minute);
                        updateDateTimeDisplay();
                    },
                    selectedDateTime.get(Calendar.HOUR_OF_DAY),
                    selectedDateTime.get(Calendar.MINUTE),
                    false
            );
            timePickerDialog.show();
        });

        builder.setPositiveButton("Create", (dialog, which) -> {
            String name = etCustomerName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String partySizeStr = etPartySize.getText().toString().trim();

            if (name.isEmpty() || phone.isEmpty() || partySizeStr.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            int partySize = Integer.parseInt(partySizeStr);
            createReservation(name, phone, partySize);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void updateDateTimeDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.US);
        if (tvSelectedDateTime != null) {
            tvSelectedDateTime.setText(sdf.format(selectedDateTime.getTime()));
        }
    }

    private void createReservation(String customerName, String phone, int partySize) {
        try {
            // Generate unique IDs using timestamp
            long timestamp = System.currentTimeMillis();
            String customerId = "CUST" + (timestamp % 100000);
            String resId = "RES" + ((timestamp + 1) % 100000);

            // Find available table for party size
            Cursor tableCursor = dbOperator.execQuery(
                    "SELECT DT_id FROM Dining_table WHERE DT_party_size >= " + partySize +
                            " AND DT_id NOT IN (SELECT Res_DT_id FROM Reservation WHERE Res_status = 'Confirmed') " +
                            "ORDER BY DT_party_size LIMIT 1"
            );

            String tableId = null;
            if (tableCursor != null && tableCursor.moveToFirst()) {
                tableId = tableCursor.getString(0);
                tableCursor.close();
            }

            if (tableId == null) {
                Toast.makeText(this, "No available table for party size " + partySize, Toast.LENGTH_SHORT).show();
                return;
            }

            // Format date for SQLite
            SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
            String dateStr = dbFormat.format(selectedDateTime.getTime());

            // *** FIXED: Use execSQL for INSERT statements ***

            // Insert customer using execSQL with parameters
            dbOperator.execSQL(
                    "INSERT INTO Customer (Cust_id, Cust_name, Cust_Number) VALUES (?, ?, ?)",
                    new Object[]{customerId, customerName, phone}
            );

            // Insert reservation using execSQL with parameters
            dbOperator.execSQL(
                    "INSERT INTO Reservation (Res_id, Res_Cust_id, Res_DT_id, Res_date, Res_start_item, Res_party_size, Res_status) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    new Object[]{resId, customerId, tableId, dateStr, dateStr, partySize, "Confirmed"}
            );

            Toast.makeText(this, "Reservation created successfully!", Toast.LENGTH_SHORT).show();

            // Refresh the dashboard and reservations list
            loadDashboardData();
            loadUpcomingReservations();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error creating reservation: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showSeatingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_seat_party, null);
        builder.setView(dialogView);
        builder.setTitle("Seat Walk-in Party");

        EditText etPartySize = dialogView.findViewById(R.id.et_party_size_seat);
        Button btnFindTables = dialogView.findViewById(R.id.btn_find_tables);
        RecyclerView rvAvailableTables = dialogView.findViewById(R.id.rv_available_tables_dialog);
        rvAvailableTables.setLayoutManager(new LinearLayoutManager(this));

        btnFindTables.setOnClickListener(v -> {
            String partySizeStr = etPartySize.getText().toString().trim();
            if (partySizeStr.isEmpty()) {
                Toast.makeText(this, "Enter party size", Toast.LENGTH_SHORT).show();
                return;
            }

            int partySize = Integer.parseInt(partySizeStr);
            loadAvailableTables(rvAvailableTables, partySize);
        });

        builder.setNegativeButton("Close", null);
        builder.show();
    }

    private void loadAvailableTables(RecyclerView recyclerView, int partySize) {
        List<Table> tables = new ArrayList<>();

        try {
            Cursor cursor = dbOperator.execQuery(
                    "SELECT DT_id, DT_number, DT_party_size, DT_table_type FROM Dining_table " +
                            "WHERE DT_party_size >= " + partySize +
                            " AND DT_id NOT IN (SELECT Order_DT_id FROM Orders WHERE Order_status IN ('Open', 'Placed', 'Preparing')) " +
                            "ORDER BY DT_party_size"
            );

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Table table = new Table();
                    table.id = cursor.getString(0);
                    table.number = cursor.getInt(1);
                    table.capacity = cursor.getInt(2);
                    table.type = cursor.getString(3);

                    tables.add(table);
                }
                cursor.close();
            }

            if (tables.isEmpty()) {
                Toast.makeText(this, "No available tables for party of " + partySize, Toast.LENGTH_SHORT).show();
            }

            TableSelectionAdapter adapter = new TableSelectionAdapter(tables);
            recyclerView.setAdapter(adapter);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // *** Methods to update reservation status ***

    private void seatReservation(Reservation res) {
        try {
            // Use execSQL for UPDATE statements
            dbOperator.execSQL(
                    "UPDATE Reservation SET Res_status = ? WHERE Res_id = ?",
                    new Object[]{"Completed", res.id}
            );
            Toast.makeText(this, res.customerName + " has been seated", Toast.LENGTH_SHORT).show();
            loadDashboardData();
            loadUpcomingReservations();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error seating reservation", Toast.LENGTH_SHORT).show();
        }
    }

    private void cancelReservation(Reservation res) {
        try {
            // Use execSQL for UPDATE statements
            dbOperator.execSQL(
                    "UPDATE Reservation SET Res_status = ? WHERE Res_id = ?",
                    new Object[]{"Cancelled", res.id}
            );
            Toast.makeText(this, "Reservation cancelled", Toast.LENGTH_SHORT).show();
            loadDashboardData();
            loadUpcomingReservations();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error cancelling reservation", Toast.LENGTH_SHORT).show();
        }
    }

    // Data classes
    private static class Reservation {
        String id;
        String customerName;
        String phone;
        int partySize;
        String dateTime;
        String status;
        int tableNumber;
    }

    private static class Table {
        String id;
        int number;
        int capacity;
        String type;
    }

    // Adapters
    private class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ViewHolder> {
        private List<Reservation> reservations;

        public ReservationAdapter(List<Reservation> reservations) {
            this.reservations = reservations;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_reservation, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Reservation res = reservations.get(position);

            holder.tvCustomerName.setText(res.customerName);
            holder.tvPhone.setText(res.phone);
            holder.tvPartySize.setText("Party of " + res.partySize);
            holder.tvTable.setText("Table " + res.tableNumber);
            holder.tvStatus.setText(res.status);

            // Color code status
            if (res.status.equals("Confirmed")) {
                holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#10B981"));
            } else {
                holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#F59E0B"));
            }

            holder.btnSeat.setOnClickListener(v -> {
                seatReservation(res);
            });

            holder.btnCancel.setOnClickListener(v -> {
                cancelReservation(res);
            });
        }

        @Override
        public int getItemCount() {
            return reservations.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvCustomerName, tvPhone, tvPartySize, tvTable, tvStatus;
            Button btnSeat, btnCancel;

            public ViewHolder(View itemView) {
                super(itemView);
                tvCustomerName = itemView.findViewById(R.id.tv_res_customer_name);
                tvPhone = itemView.findViewById(R.id.tv_res_phone);
                tvPartySize = itemView.findViewById(R.id.tv_res_party_size);
                tvTable = itemView.findViewById(R.id.tv_res_table);
                tvStatus = itemView.findViewById(R.id.tv_res_status);
                btnSeat = itemView.findViewById(R.id.btn_seat);
                btnCancel = itemView.findViewById(R.id.btn_cancel_res);
            }
        }
    }

    private class TableSelectionAdapter extends RecyclerView.Adapter<TableSelectionAdapter.ViewHolder> {
        private List<Table> tables;

        public TableSelectionAdapter(List<Table> tables) {
            this.tables = tables;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_table_selection, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Table table = tables.get(position);

            holder.tvTableNumber.setText("Table " + table.number);
            holder.tvTableType.setText(table.type);
            holder.tvCapacity.setText(table.capacity + " seats");

            holder.itemView.setOnClickListener(v -> {
                Toast.makeText(HostMainActivity.this,
                        "Selected Table " + table.number, Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public int getItemCount() {
            return tables.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTableNumber, tvTableType, tvCapacity;

            public ViewHolder(View itemView) {
                super(itemView);
                tvTableNumber = itemView.findViewById(R.id.tv_table_number_sel);
                tvTableType = itemView.findViewById(R.id.tv_table_type_sel);
                tvCapacity = itemView.findViewById(R.id.tv_table_capacity_sel);
            }
        }
    }
}