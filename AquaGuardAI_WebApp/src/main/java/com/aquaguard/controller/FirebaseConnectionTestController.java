package com.aquaguard.controller;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@RequestMapping("/api/firebase")
public class FirebaseConnectionTestController {

    private final FirebaseDatabase database;

    public FirebaseConnectionTestController(FirebaseApp app) {
        this.database = FirebaseDatabase.getInstance(app);
    }

    @PostMapping("/connection-test")
    public ResponseEntity<Map<String, Object>> testConnection() {
        Map<String, Object> response = new HashMap<>();

        try {
            DatabaseReference ref =
                    database.getReference("connectionTest/latest");

            Map<String, Object> testData = new HashMap<>();
            testData.put(
                    "message",
                    "AquaGuardAI Firebase connection successful"
            );
            testData.put("timestamp", System.currentTimeMillis());

            // Write test data to Firebase
            ref.setValueAsync(testData).get(15, TimeUnit.SECONDS);

            // Read the data back from Firebase
            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<DataSnapshot> snapshotRef =
                    new AtomicReference<>();
            AtomicReference<DatabaseError> errorRef =
                    new AtomicReference<>();

            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    snapshotRef.set(snapshot);
                    latch.countDown();
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    errorRef.set(error);
                    latch.countDown();
                }
            });

            if (!latch.await(15, TimeUnit.SECONDS)) {
                throw new RuntimeException(
                        "Firebase read timed out"
                );
            }

            if (errorRef.get() != null) {
                throw new RuntimeException(
                        errorRef.get().getMessage()
                );
            }

            DataSnapshot snapshot = snapshotRef.get();

            String message = snapshot == null
                    ? null
                    : snapshot.child("message").getValue(String.class);

            if ("AquaGuardAI Firebase connection successful"
                    .equals(message)) {

                response.put("success", true);
                response.put(
                        "message",
                        "Firebase write and read successful"
                );
                response.put("data", snapshot.getValue());

                return ResponseEntity.ok(response);
            }

            response.put("success", false);
            response.put(
                    "message",
                    "Firebase data did not match"
            );

            return ResponseEntity.status(500).body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getClass().getSimpleName());
            response.put("details", String.valueOf(e.getMessage()));

            return ResponseEntity.status(500).body(response);
        }
    }
}