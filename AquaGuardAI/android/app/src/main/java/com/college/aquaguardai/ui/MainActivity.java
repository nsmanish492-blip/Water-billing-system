package com.college.aquaguardai.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import com.college.aquaguardai.R;
import com.college.aquaguardai.firebase.FirebaseRepository;
import com.college.aquaguardai.model.SensorReading;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private FirebaseRepository repo;
    private TextView tvStatus, tvTotal, tvFlow, tvS1, tvS2, tvBill, tvLeak;

    private double tariff = 5.0; // ₹ per 1000 L

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        tvStatus = findViewById(R.id.tvStatus);
        tvTotal = findViewById(R.id.tvTotal);
        tvFlow = findViewById(R.id.tvFlow);
        tvS1 = findViewById(R.id.tvS1);
        tvS2 = findViewById(R.id.tvS2);
        tvBill = findViewById(R.id.tvBill);
        tvLeak = findViewById(R.id.tvLeak);

        repo = new FirebaseRepository();
        repo.setDeviceId("esp32_aqua_001");  // change in Settings later

        observeData();

        BottomNavigationView nav = findViewById(R.id.bottomNav);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                // already here
            } else if (id == R.id.nav_diagnostics) {
                startActivity(new Intent(this, DiagnosticsActivity.class));
            } else {
                Toast.makeText(this, "Feature screen – extend as needed", Toast.LENGTH_SHORT).show();
            }
            return true;
        });
    }

    private void observeData() {
        repo.getConnectionState().observe(this, state -> tvStatus.setText(state));

        repo.getSensor1().observe(this, r -> {
            if (r != null) tvS1.setText(String.format("%.2f L/min", r.flowRate));
        });
        repo.getSensor2().observe(this, r -> {
            if (r != null) tvS2.setText(String.format("%.2f L/min", r.flowRate));
        });
        repo.getTotalConsumption().observe(this, total -> {
            if (total != null) {
                tvTotal.setText(String.format("%.1f L", total));
                double bill = (total / 1000.0) * tariff;
                tvBill.setText(String.format("₹ %.2f", bill));
            }
        });

        // Simple combined flow + leakage
        repo.getSensor1().observe(this, s1 -> updateLeakAndFlow());
        repo.getSensor2().observe(this, s2 -> updateLeakAndFlow());
    }

    private void updateLeakAndFlow() {
        SensorReading s1 = repo.getSensor1().getValue();
        SensorReading s2 = repo.getSensor2().getValue();
        if (s1 == null || s2 == null) return;

        double flow = s1.flowRate + s2.flowRate;
        tvFlow.setText(String.format("%.2f L/min", flow));

        double diff = Math.abs(s1.flowRate - s2.flowRate);
        if (diff > 1.0) {
            tvLeak.setText("RED – Possible Leak");
        } else if (diff > 0.5) {
            tvLeak.setText("YELLOW – Check difference");
        } else {
            tvLeak.setText("GREEN – Normal");
        }
    }
}
