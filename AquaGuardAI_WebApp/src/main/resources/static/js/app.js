/**
 * AquaGuard AI - Frontend Interactivity & Live Telemetry Poller
 * Handles Chart.js visualization, real-time simulated telemetry polling,
 * and live presentation actions.
 */

document.addEventListener('DOMContentLoaded', () => {
    initLivePolling();
    initSimulateLeakToggle();
});

let liveTelemetryPollInFlight = false;

/**
 * Periodically polls /api/telemetry to smoothly update telemetry cards and charts
 * without page reload.
 */
function initLivePolling() {
    const s1FlowEl = document.getElementById('live-s1-flow');
    const s2FlowEl = document.getElementById('live-s2-flow');
    const diffFlowEl = document.getElementById('live-diff-flow');
    const syncTimeEl = document.getElementById('last-sync-time');

    const hardwareStatusBadge = document.getElementById('hardware-status-badge');
    if (!s1FlowEl && !s2FlowEl && !hardwareStatusBadge) return;

    const pollTelemetry = async () => {
        if (liveTelemetryPollInFlight) return;
        liveTelemetryPollInFlight = true;
        try {
            const res = await fetch('/api/telemetry', { cache: 'no-store' });
            if (!res.ok) return;
            const data = await res.json();
            updateConnectionStatus(data);
            updateUsageCharts(data);

            if (data.readings && data.readings.length >= 2) {
                const s1 = data.readings[0];
                const s2 = data.readings[1];

                if (s1FlowEl) s1FlowEl.textContent = s1.flowRateLpm.toFixed(2);
                if (s2FlowEl) s2FlowEl.textContent = s2.flowRateLpm.toFixed(2);

                const s1VolEl = document.getElementById('live-s1-vol');
                const s2VolEl = document.getElementById('live-s2-vol');
                if (s1VolEl) s1VolEl.textContent = s1.totalLitres.toFixed(1) + ' L';
                if (s2VolEl) s2VolEl.textContent = s2.totalLitres.toFixed(1) + ' L';

                const s1PulsesEl = document.getElementById('live-s1-pulses');
                const s2PulsesEl = document.getElementById('live-s2-pulses');
                if (s1PulsesEl) s1PulsesEl.textContent = s1.pulseCount.toLocaleString();
                if (s2PulsesEl) s2PulsesEl.textContent = s2.pulseCount.toLocaleString();

                if (diffFlowEl && data.leakage) {
                    diffFlowEl.textContent = Math.abs(data.leakage.differentialFlowLpm).toFixed(2);
                }

                if (syncTimeEl && data.lastUpdateText) {
                    syncTimeEl.textContent = data.lastUpdateText;
                }

                updateSensorValidity(data);
                updateLiveSnapshot(data);

                // Push new point into chart if live chart is present
                if (window.liveTelemetryChart) {
                    const timeLabel = new Date().toLocaleTimeString();
                    const chart = window.liveTelemetryChart;
                    chart.data.labels.push(timeLabel);
                    chart.data.datasets[0].data.push(s1.flowRateLpm);
                    chart.data.datasets[1].data.push(s2.flowRateLpm);

                    if (chart.data.labels.length > 20) {
                        chart.data.labels.shift();
                        chart.data.datasets[0].data.shift();
                        chart.data.datasets[1].data.shift();
                    }
                    chart.update('none'); // smooth update without full animation
                }
            } else {
                ['live-s1-flow', 'live-s2-flow', 'live-s1-vol', 'live-s2-vol',
                    'live-s1-pulses', 'live-s2-pulses', 'live-diff-flow'].forEach((id) => {
                    const element = document.getElementById(id);
                    if (element) element.textContent = 'Unavailable';
                });
                document.querySelectorAll('[data-sensor-valid]').forEach((badge) => {
                    badge.textContent = 'UNAVAILABLE';
                    badge.className = 'badge bg-secondary';
                });
            }
        } catch (err) {
            console.debug('Telemetry poll error (expected during navigation):', err);
        } finally {
            liveTelemetryPollInFlight = false;
            window.setTimeout(pollTelemetry, 2000);
        }
    };

    pollTelemetry();
}

function updateUsageCharts(data) {
    const hourly = data.hourlyUsage || [];
    const daily = document.querySelector('[data-usage-range="30d"]')
        ? (data.monthlyUsage || [])
        : (data.dailyUsage || []);

    const hourlyChart = window.analyticsHourlyUsageChart;
    if (hourlyChart) {
        hourlyChart.data.labels = hourly.map(record => record.label);
        hourlyChart.data.datasets[0].data = hourly.map(record => record.sensor1FlowLpm);
        hourlyChart.data.datasets[1].data = hourly.map(record => record.sensor2FlowLpm);
        hourlyChart.update('none');
    }

    [window.analyticsDailyUsageChart, window.dashboardDailyUsageChart].forEach((chart) => {
        if (!chart) return;
        chart.data.labels = daily.map(record => record.label);
        chart.data.datasets[0].data = daily.map(record => record.consumptionLitres);
        chart.update('none');
    });

    const shareChart = window.dashboardSensorShareChart;
    if (shareChart && data.readings && data.readings.length >= 2) {
        shareChart.data.datasets[0].data = data.readings.map(reading => reading.totalLitres);
        shareChart.update('none');
    }
}

function updateConnectionStatus(data) {
    const connected = Boolean(data.hardwareConnected);
    const demoMode = Boolean(data.demoMode);
    const badge = document.getElementById('hardware-status-badge');
    const badgeText = document.getElementById('hardware-status-text');
    const dot = document.getElementById('hardware-status-dot');
    const demoBadge = document.getElementById('demo-mode-badge');
    const title = document.getElementById('global-data-mode-title');
    const message = document.getElementById('global-data-mode-description');
    const liveMode = document.getElementById('live-data-mode');
    const liveMessage = document.getElementById('live-data-message');

    if (badge) {
        badge.classList.toggle('bg-success', connected);
        badge.classList.toggle('bg-danger', !connected);
    }
    if (badgeText) badgeText.textContent = connected ? 'HARDWARE CONNECTED' : 'HARDWARE NOT CONNECTED';
    if (dot) dot.className = connected ? 'status-dot green' : 'status-dot red';
    if (demoBadge) demoBadge.classList.toggle('d-none', !demoMode);
    if (title) title.textContent = connected ? 'LIVE SENSOR TELEMETRY:' : 'NO LIVE SENSOR DATA:';
    if (message && data.diagnosticMessage) message.textContent = data.diagnosticMessage;
    if (liveMode) {
        liveMode.textContent = connected ? 'LIVE HARDWARE' : 'NO LIVE DATA';
        liveMode.className = connected
            ? 'badge bg-success-subtle text-success font-mono'
            : 'badge bg-warning text-dark font-mono';
    }
    if (liveMessage && data.diagnosticMessage) liveMessage.textContent = data.diagnosticMessage;

    const cardBadge = document.getElementById('dashboard-hardware-status');
    const diagnosticsBadge = document.getElementById('diagnostics-hardware-status');
    [cardBadge, diagnosticsBadge].forEach((statusBadge) => {
        if (!statusBadge) return;
        statusBadge.textContent = connected ? 'HARDWARE CONNECTED' : 'HARDWARE NOT CONNECTED';
        statusBadge.className = connected ? 'badge bg-success' : 'badge bg-danger';
    });
    const diagnosticsMode = document.getElementById('diagnostics-data-source-mode');
    if (diagnosticsMode) {
        diagnosticsMode.textContent = demoMode ? 'DEMO FALLBACK' : 'LIVE TELEMETRY';
        diagnosticsMode.className = demoMode ? 'badge bg-warning text-dark font-mono' : 'badge bg-success text-white font-mono';
    }
    const diagnostic = document.getElementById('dashboard-system-diagnostic');
    if (diagnostic && data.diagnosticMessage) diagnostic.textContent = data.diagnosticMessage;
    const firebaseStatus = document.getElementById('dashboard-firebase-status');
    if (firebaseStatus && data.firebaseStatus) firebaseStatus.textContent = data.firebaseStatus;
    document.querySelectorAll('[data-demo-snapshot]').forEach((row) => {
        row.classList.toggle('d-none', !demoMode);
    });
    const liveSnapshot = document.getElementById('hardware-snapshot-row');
    if (liveSnapshot) liveSnapshot.classList.toggle('d-none', demoMode);
}

function updateSensorValidity(data) {
    if (!data.readings || data.readings.length < 2) return;

    ['inlet', 'outlet'].forEach((sensor, index) => {
        const badge = document.querySelector(`[data-sensor-valid="${sensor}"]`);
        if (!badge) return;

        const reading = data.readings[index];
        badge.textContent = reading.valid
            ? (data.demoMode ? 'VALID / DEMO' : 'VALID / LIVE')
            : 'INVALID';
        badge.className = reading.valid
            ? 'badge bg-success-subtle text-success border border-success-subtle'
            : 'badge bg-danger-subtle text-danger border border-danger-subtle';
    });
}

function updateLiveSnapshot(data) {
    const row = document.getElementById('hardware-snapshot-row');
    if (!row || !data.readings || data.readings.length < 2 || data.demoMode) return;

    const [inlet, outlet] = data.readings;
    const values = {
        'hardware-snapshot-time': data.lastUpdateText,
        'hardware-snapshot-inlet': `${inlet.flowRateLpm.toFixed(2)} L/min`,
        'hardware-snapshot-outlet': `${outlet.flowRateLpm.toFixed(2)} L/min`,
        'hardware-snapshot-difference': `${data.leakage.differentialFlowLpm.toFixed(2)} L/min`,
        'hardware-snapshot-evaluation': data.leakage.statusLabel,
        'hardware-snapshot-integrity': inlet.valid && outlet.valid ? 'Both sensors valid' : 'Sensor invalid'
    };

    Object.entries(values).forEach(([id, value]) => {
        const element = document.getElementById(id);
        if (element && value != null) element.textContent = value;
    });
}

/**
 * Presentation feature: Toggle simulated leak on demand
 */
function initSimulateLeakToggle() {
    const btn = document.getElementById('btnToggleLeakSim');
    if (!btn) return;

    btn.addEventListener('click', async () => {
        try {
            btn.disabled = true;
            const res = await fetch('/api/demo/toggle-leak', { method: 'POST' });
            const data = await res.json();
            if (data.success) {
                alert(data.message + "\n\n(Click OK to reload the page with updated state)");
                window.location.reload();
            }
        } catch (e) {
            console.error('Failed to toggle leak simulation:', e);
        } finally {
            btn.disabled = false;
        }
    });
}
