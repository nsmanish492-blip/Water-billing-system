// ========== FIREBASE CONFIG (FILL THESE) ==========
const firebaseConfig = {
  apiKey: "YOUR_FIREBASE_WEB_API_KEY",
  authDomain: "YOUR_PROJECT.firebaseapp.com",
  databaseURL: "https://YOUR_PROJECT-default-rtdb.firebaseio.com",
  projectId: "YOUR_PROJECT",
  storageBucket: "YOUR_PROJECT.appspot.com",
  messagingSenderId: "YOUR_SENDER_ID",
  appId: "YOUR_APP_ID"
};
// ==================================================

let deviceId = "esp32_aqua_001";
let tariffPer1000L = 5.0;
let leakThreshold = 0.5;
let offlineTimeoutSec = 45;   // consider offline if no heartbeat for this long
let dbRef = null;
let lastHeartbeat = 0;

firebase.initializeApp(firebaseConfig);
const db = firebase.database();

// Optional: anonymous auth for demo (or use email/password)
firebase.auth().signInAnonymously().catch(err => {
  console.warn("Auth:", err.message);
});

function applySettings() {
  deviceId = document.getElementById("device-id").value.trim() || "esp32_aqua_001";
  tariffPer1000L = parseFloat(document.getElementById("tariff").value) || 5;
  leakThreshold = parseFloat(document.getElementById("leak-threshold").value) || 0.5;

  if (dbRef) dbRef.off();   // remove previous listeners
  startListening();
}

function startListening() {
  const base = `waterSystems/${deviceId}`;
  dbRef = db.ref(base);

  dbRef.on("value", snapshot => {
    const data = snapshot.val();
    if (!data) {
      setOffline("Waiting for ESP32 connection");
      return;
    }
    updateUI(data);
  }, error => {
    console.error(error);
    setOffline("Firebase Connection Error");
  });
}

function updateUI(data) {
  const s1 = data.sensors?.sensor1 || {};
  const s2 = data.sensors?.sensor2 || {};
  const sys = data.system || {};
  const analytics = data.analytics || {};

  // Heartbeat / online status
  lastHeartbeat = sys.lastHeartbeat || s1.updatedAt || s2.updatedAt || 0;
  const now = Math.floor(Date.now() / 1000);
  const age = now - lastHeartbeat;
  const isOnline = lastHeartbeat > 0 && age < offlineTimeoutSec;

  const statusEl = document.getElementById("esp-status");
  if (isOnline) {
    statusEl.textContent = "ESP32 Online";
    statusEl.className = "badge online";
  } else if (lastHeartbeat === 0) {
    statusEl.textContent = "Waiting for ESP32";
    statusEl.className = "badge offline";
  } else {
    statusEl.textContent = "ESP32 Offline";
    statusEl.className = "badge offline";
  }

  document.getElementById("last-update").textContent =
    lastHeartbeat ? `Last update: ${new Date(lastHeartbeat * 1000).toLocaleString()}` : "Last update: —";

  // Values
  const totalL = (s1.totalLitres || 0) + (s2.totalLitres || 0);
  const flow1 = s1.flowRate || 0;
  const flow2 = s2.flowRate || 0;
  const currentFlow = flow1 + flow2;

  document.getElementById("total-litres").textContent = totalL.toFixed(1) + " L";
  document.getElementById("current-flow").textContent = currentFlow.toFixed(2) + " L/min";
  document.getElementById("s1-flow").textContent = flow1.toFixed(2) + " L/min";
  document.getElementById("s1-total").textContent = (s1.totalLitres || 0).toFixed(1) + " L";
  document.getElementById("s2-flow").textContent = flow2.toFixed(2) + " L/min";
  document.getElementById("s2-total").textContent = (s2.totalLitres || 0).toFixed(1) + " L";

  // Bill estimate
  const bill = (totalL / 1000) * tariffPer1000L;
  document.getElementById("est-bill").textContent = "₹ " + bill.toFixed(2);

  // Simple leakage logic
  const diff = Math.abs(flow1 - flow2);
  let leakStatus = "GREEN – Normal";
  let leakRisk = "LOW RISK";
  let pipeHealth = "Good";

  if (!isOnline) {
    leakStatus = "Insufficient data";
    leakRisk = "—";
    pipeHealth = "—";
  } else if (diff > leakThreshold * 2) {
    leakStatus = "RED – Possible Leak";
    leakRisk = "HIGH RISK";
    pipeHealth = "Inspect soon";
  } else if (diff > leakThreshold) {
    leakStatus = "YELLOW – Check difference";
    leakRisk = "MEDIUM RISK";
    pipeHealth = "Monitor";
  }

  document.getElementById("leak-status").textContent = leakStatus;
  document.getElementById("leak-risk").textContent = leakRisk;
  document.getElementById("pipe-health").textContent = pipeHealth;
}

function setOffline(msg) {
  document.getElementById("esp-status").textContent = msg;
  document.getElementById("esp-status").className = "badge offline";
}

// Start with default device
startListening();
