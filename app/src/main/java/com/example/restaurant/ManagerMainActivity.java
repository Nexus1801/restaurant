package com.example.restaurant;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ManagerMainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toast.makeText(this, "Manager Dashboard - Coming Soon", Toast.LENGTH_SHORT).show();
        finish();
    }
}
