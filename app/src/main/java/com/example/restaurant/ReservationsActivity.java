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
class ReservationsActivity extends AppCompatActivity {

    private RecyclerView rvReservations;
    private DBOperator dbOperator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservations);

        dbOperator = DBOperator.getInstance();

        rvReservations = findViewById(R.id.rv_all_reservations);
        rvReservations.setLayoutManager(new LinearLayoutManager(this));

        loadAllReservations();
    }

    private void loadAllReservations() {
        try {
            Cursor cursor = dbOperator.execQuery(SQLCommand.QUERY_RESERVATIONS);

            if (cursor != null) {
                Toast.makeText(this, cursor.getCount() + " reservations found", Toast.LENGTH_SHORT).show();
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

