package com.example.restaurant;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.restaurant.util.DBOperator;

import java.util.ArrayList;
import java.util.List;

public class ReservationsActivity extends AppCompatActivity {

    private RecyclerView rvAllReservations;
    private DBOperator dbOperator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservations);

        dbOperator = DBOperator.getInstance();

        rvAllReservations = findViewById(R.id.rv_all_reservations);
        rvAllReservations.setLayoutManager(new LinearLayoutManager(this));

        loadReservations();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadReservations();
    }

    private void loadReservations() {
        List<Reservation> reservations = new ArrayList<>();

        try {
            Cursor cursor = dbOperator.execQuery(
                    "SELECT r.Res_id, c.Cust_name, c.Cust_Number, r.Res_party_size, " +
                            "r.Res_date, r.Res_status, d.DT_number " +
                            "FROM Reservation r " +
                            "JOIN Customer c ON r.Res_Cust_id = c.Cust_id " +
                            "JOIN Dining_table d ON r.Res_DT_id = d.DT_id " +
                            "ORDER BY r.Res_date DESC"
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

            AllReservationsAdapter adapter = new AllReservationsAdapter(reservations);
            rvAllReservations.setAdapter(adapter);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading reservations", Toast.LENGTH_SHORT).show();
        }
    }

    private static class Reservation {
        String id;
        String customerName;
        String phone;
        int partySize;
        String dateTime;
        String status;
        int tableNumber;
    }

    private class AllReservationsAdapter extends RecyclerView.Adapter<AllReservationsAdapter.ViewHolder> {
        private List<Reservation> reservations;

        public AllReservationsAdapter(List<Reservation> reservations) {
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
            switch (res.status) {
                case "Confirmed":
                    holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#10B981"));
                    holder.btnSeat.setVisibility(View.VISIBLE);
                    holder.btnCancel.setVisibility(View.VISIBLE);
                    break;
                case "Completed":
                    holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#3B82F6"));
                    holder.btnSeat.setVisibility(View.GONE);
                    holder.btnCancel.setVisibility(View.GONE);
                    break;
                case "Cancelled":
                    holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#EF4444"));
                    holder.btnSeat.setVisibility(View.GONE);
                    holder.btnCancel.setVisibility(View.GONE);
                    break;
                default:
                    holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#F59E0B"));
                    holder.btnSeat.setVisibility(View.VISIBLE);
                    holder.btnCancel.setVisibility(View.VISIBLE);
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

    private void seatReservation(Reservation res) {
        try {
            dbOperator.execSQL(
                    "UPDATE Reservation SET Res_status = ? WHERE Res_id = ?",
                    new Object[]{"Completed", res.id}
            );
            Toast.makeText(this, res.customerName + " has been seated", Toast.LENGTH_SHORT).show();
            loadReservations();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error seating reservation", Toast.LENGTH_SHORT).show();
        }
    }

    private void cancelReservation(Reservation res) {
        try {
            dbOperator.execSQL(
                    "UPDATE Reservation SET Res_status = ? WHERE Res_id = ?",
                    new Object[]{"Cancelled", res.id}
            );
            Toast.makeText(this, "Reservation cancelled", Toast.LENGTH_SHORT).show();
            loadReservations();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error cancelling reservation", Toast.LENGTH_SHORT).show();
        }
    }
}