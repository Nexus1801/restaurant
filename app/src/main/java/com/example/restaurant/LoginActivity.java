package com.example.restaurant;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.restaurant.constant.SQLCommand;
import com.example.restaurant.util.DBOperator;

import java.io.IOException;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmployeeId;
    private EditText etPassword;
    private CheckBox cbClockIn;
    private Button btnLogin;
    private DBOperator dbOperator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // CRITICAL: Initialize database FIRST
        try {
            DBOperator.copyDB(getApplicationContext());
            Toast.makeText(this, "Database initialized", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Database initialization failed: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            // Continue anyway for testing
        }

        setContentView(R.layout.activity_login);

        // Initialize DBOperator
        try {
            dbOperator = DBOperator.getInstance();
            Toast.makeText(this, "Database connected", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Database connection failed: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }

        // Initialize views
        etEmployeeId = findViewById(R.id.et_employee_id);
        etPassword = findViewById(R.id.et_password);
        cbClockIn = findViewById(R.id.cb_clock_in);
        btnLogin = findViewById(R.id.btn_login);

        // Set default test values for quick testing
        etEmployeeId.setText("EMP001");

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

    private void login() {
        String employeeId = etEmployeeId.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (employeeId.isEmpty()) {
            Toast.makeText(this, "Please enter Employee ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if dbOperator is initialized
        if (dbOperator == null) {
            Toast.makeText(this, "Database not initialized. Trying again...",
                    Toast.LENGTH_SHORT).show();
            try {
                dbOperator = DBOperator.getInstance();
            } catch (Exception e) {
                Toast.makeText(this, "Database error: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
                return;
            }
        }

        // Validate employee ID from database
        try {
            Cursor cursor = dbOperator.execQuery(
                    SQLCommand.QUERY_EMPLOYEE_LOGIN,
                    new String[]{employeeId}
            );

            if (cursor != null && cursor.moveToFirst()) {
                String empId = cursor.getString(0);
                String empName = cursor.getString(1);
                String empRole = cursor.getString(2);

                // Save employee info to SharedPreferences
                SharedPreferences prefs = getSharedPreferences("RestaurantApp", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("employee_id", empId);
                editor.putString("employee_name", empName);
                editor.putString("employee_role", empRole);
                editor.putBoolean("is_clocked_in", cbClockIn.isChecked());
                editor.apply();

                cursor.close();

                // Navigate based on role
                Intent intent = null;

                if (empRole.equals("Manager")) {
                    intent = new Intent(LoginActivity.this, ManagerMainActivity.class);
                } else if (empRole.equals("Server")) {
                    intent = new Intent(LoginActivity.this, ServerMainActivity.class);
                } else if (empRole.equals("Host")) {
                    intent = new Intent(LoginActivity.this, HostMainActivity.class);
                } else if (empRole.equals("Bartender")) {
                    intent = new Intent(LoginActivity.this, ServerMainActivity.class);
                } else {
                    // Default to Server for any other role
                    intent = new Intent(LoginActivity.this, ServerMainActivity.class);
                }

                Toast.makeText(this, "Welcome, " + empName + "!", Toast.LENGTH_SHORT).show();
                startActivity(intent);
                finish();

            } else {
                Toast.makeText(this, "Invalid Employee ID: " + employeeId,
                        Toast.LENGTH_SHORT).show();
            }

            if (cursor != null) {
                cursor.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Login error: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }
}