package com.college.aquaguardai.firebase;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.college.aquaguardai.model.SensorReading;
import com.college.aquaguardai.model.SystemStatus;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseRepository {

    private final DatabaseReference root;
    private String deviceId = "esp32_aqua_001";
    private ValueEventListener listener;

    private final MutableLiveData<SensorReading> sensor1 = new MutableLiveData<>();
    private final MutableLiveData<SensorReading> sensor2 = new MutableLiveData<>();
    private final MutableLiveData<SystemStatus> systemStatus = new MutableLiveData<>();
    private final MutableLiveData<String> connectionState = new MutableLiveData<>("Waiting for ESP32");
    private final MutableLiveData<Double> totalConsumption = new MutableLiveData<>(0.0);

    public FirebaseRepository() {
        root = FirebaseDatabase.getInstance().getReference();
    }

    public void setDeviceId(String id) {
        if (listener != null) {
            root.child("waterSystems").child(deviceId).removeEventListener(listener);
        }
        this.deviceId = id;
        startListening();
    }

    public void startListening() {
        DatabaseReference ref = root.child("waterSystems").child(deviceId);

        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    connectionState.setValue("Waiting for ESP32 connection");
                    return;
                }

                DataSnapshot s1 = snapshot.child("sensors").child("sensor1");
                DataSnapshot s2 = snapshot.child("sensors").child("sensor2");
                DataSnapshot sys = snapshot.child("system");

                SensorReading r1 = s1.getValue(SensorReading.class);
                SensorReading r2 = s2.getValue(SensorReading.class);
                SystemStatus st = sys.getValue(SystemStatus.class);

                if (r1 != null) sensor1.setValue(r1);
                if (r2 != null) sensor2.setValue(r2);
                if (st != null) systemStatus.setValue(st);

                double total = 0;
                if (r1 != null) total += r1.totalLitres;
                if (r2 != null) total += r2.totalLitres;
                totalConsumption.setValue(total);

                // Online detection from heartbeat
                long now = System.currentTimeMillis() / 1000;
                long hb = (st != null) ? st.lastHeartbeat : 0;
                if (hb == 0 && r1 != null) hb = r1.updatedAt;
                if (hb == 0 && r2 != null) hb = r2.updatedAt;

                if (hb == 0) {
                    connectionState.setValue("Waiting for ESP32 connection");
                } else if (now - hb < 45) {
                    connectionState.setValue("ESP32 Online");
                } else {
                    connectionState.setValue("ESP32 Offline");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                connectionState.setValue("Firebase Connection Error");
            }
        };

        ref.addValueEventListener(listener);
    }

    public LiveData<SensorReading> getSensor1() { return sensor1; }
    public LiveData<SensorReading> getSensor2() { return sensor2; }
    public LiveData<SystemStatus> getSystemStatus() { return systemStatus; }
    public LiveData<String> getConnectionState() { return connectionState; }
    public LiveData<Double> getTotalConsumption() { return totalConsumption; }
}
