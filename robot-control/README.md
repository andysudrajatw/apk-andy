# Robot Control Center

Web dashboard untuk mobil robot Arduino/ESP32.

## Fitur
- Dashboard status robot
- Kontrol arah: W/A/S/D, arrow key, dan tombol layar
- Emergency STOP
- Pengaturan speed/PWM
- Mode Manual, Autonomous, Line Follower, Obstacle Avoidance
- Monitoring baterai, tegangan, jarak ultrasonic, suhu
- Editor program Arduino sederhana
- Konfigurasi HTTP/WebSocket gateway
- Contoh firmware Arduino 4 roda + L298N

## Jalankan
Buka `index.html` melalui hosting statis (misalnya GitHub Pages) atau local web server.

## Arsitektur yang disarankan
Browser -> Wi-Fi -> ESP32 Gateway -> Serial -> Arduino -> L298N -> 4 Motor

Untuk komunikasi HTTP, gateway menyediakan:
POST /api/command
{
  "command": "FORWARD",
  "speed": 60
}

Telemetry:
{
  "battery": 92,
  "voltage": 11.9,
  "distance": 74,
  "temperature": 29.4,
  "mode": "MANUAL"
}

> Catatan: browser tidak dapat langsung mengendalikan pin Arduino melalui jaringan tanpa perangkat/gateway komunikasi. ESP32 dapat menjadi penghubung Wi-Fi.
