package com.college.aquaguardai.ui;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.college.aquaguardai.R;
import com.college.aquaguardai.firebase.FirebaseRepository;
import com.college.aquaguardai.model.SensorReading;
import com.college.aquaguardai.model.SystemStatus;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DiagnosticsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagnostics);

        TextView tv = findViewById(R.id.tvDiagnostics);
        FirebaseRepository repo = new FirebaseRepository();
        repo.setDeviceId("esp32_aqua_001");

        repo.getConnectionState().observe(this, state -> update(tv, repo));
        repo.getSensor1().observe(this, r -> update(tv, repo));
        repo.getSensor2().observe(this, r -> update(tv, repo));
        repo.getSystemStatus().observe(this, s -> update(tv, repo));
    }

    private void update(TextView tv, FirebaseRepository repo) {
        StringBuilder sb = new StringBuilder();
        sb.append("Connection: ").append(repo.getConnectionState().getValue()).append("\n\n");

        SystemStatus sys = repo.getSystemStatus().getValue();
        if (sys != null) {
            sb.append("Last Heartbeat: ").append(formatTime(sys.lastHeartbeat)).append("\n");
            sb.append("Firmware: ").append(sys.firmwareVersion).append("\n\n");
        }

        SensorReading s1 = repo.getSensor1().getValue();
        SensorReading s2 = repo.getSensor2().getValue();

        sb.append("Sensor 1:\n");
        if (s1 != null) {
            sb.append("  Flow: ").append(String.format("%.2f L/min\n", s1.flowRate));
            sb.append("  Total: ").append(String.format("%.1f L\n", s1.totalLitres));
            sb.append("  Updated: ").append(formatTime(s1.updatedAt)).append("\n");
        } else sb.append("  No data\n");

        sb.append("\nSensor 2:\n");
        if (s2 != null) {
            sb.append("  Flow: ").append(String.format("%.2f L/min\n", s2.flowRate));
            sb.append("  Total: ").append(String.format("%.1f L\n", s2.totalLitres));
            sb.append("  Updated: ").append(formatTime(s2.updatedAt)).append("\n");
        } else sb.append("  No data\n");

        sb.append("\nTroubleshooting:\n");
        sb.append("- Check ESP32 Serial Monitor for WiFi/Firebase errors\n");
        sb.append("- Confirm Device ID matches firmware\n");
        sb.append("- Verify Firebase rules allow authenticated read\n");
        sb.append("- Ensure sensors are powered and signal voltage is safe\n");

        tv.setText(sb.toString());
    }

    private String formatTime(long epochSec) {
        if (epochSec <= 0) return "—";
        return new SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault())
                .format(new Date(epochSec * 1000));
    }
}
