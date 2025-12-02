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

public class StaffManagementActivity extends AppCompatActivity {

    private RecyclerView rvStaff;
    private DBOperator dbOperator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_management);

        dbOperator = DBOperator.getInstance();

        rvStaff = findViewById(R.id.rv_staff);
        rvStaff.setLayoutManager(new LinearLayoutManager(this));

        loadStaff();
    }

    private void loadStaff() {
        List<Employee> employees = new ArrayList<>();

        try {
            Cursor cursor = dbOperator.execQuery(
                    "SELECT Emp_id, Emp_name, Emp_role, Emp_orders_taken FROM Employee"
            );

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Employee emp = new Employee();
                    emp.id = cursor.getString(0);
                    emp.name = cursor.getString(1);
                    emp.role = cursor.getString(2);
                    emp.ordersTaken = cursor.getInt(3);

                    employees.add(emp);
                }
                cursor.close();
            }

            StaffAdapter adapter = new StaffAdapter(employees);
            rvStaff.setAdapter(adapter);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class Employee {
        String id;
        String name;
        String role;
        int ordersTaken;
    }

    private class StaffAdapter extends RecyclerView.Adapter<StaffAdapter.ViewHolder> {
        private List<Employee> employees;

        public StaffAdapter(List<Employee> employees) {
            this.employees = employees;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_staff, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Employee emp = employees.get(position);

            holder.tvName.setText(emp.name);
            holder.tvRole.setText(emp.role);
            holder.tvOrdersTaken.setText("Orders: " + emp.ordersTaken);

            holder.itemView.setOnClickListener(v -> {
                Toast.makeText(StaffManagementActivity.this,
                        emp.name + " - " + emp.role, Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public int getItemCount() {
            return employees.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvRole, tvOrdersTaken;

            public ViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tv_staff_name);
                tvRole = itemView.findViewById(R.id.tv_staff_role);
                tvOrdersTaken = itemView.findViewById(R.id.tv_staff_orders);
            }
        }
    }
}

