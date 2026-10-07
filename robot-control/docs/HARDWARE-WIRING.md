# Wiring dasar mobil robot 4 roda

## Arsitektur
Wi-Fi Clients → ESP32 Gateway → UART → Arduino UNO → L298N → 4 motor

## Contoh koneksi UART
ESP32 GPIO17 (TX2) → Arduino RX
ESP32 GPIO16 (RX2) ← Arduino TX
GND ESP32 ↔ GND Arduino

> Gunakan level tegangan yang benar. Jangan sambungkan pin 5V Arduino langsung ke GPIO ESP32.

## Arduino ke L298N
ENA → D5 (PWM)
IN1 → D7
IN2 → D8
ENB → D6 (PWM)
IN3 → D9
IN4 → D10

Motor A/B pada L298N dapat dipasangkan sesuai sisi kiri/kanan robot. Untuk 4 roda, dua motor pada sisi kiri dapat diparalelkan sebagai channel A hanya bila driver, arus, catu daya, dan motor memang mendukungnya. Lebih aman memakai driver dua channel dengan rating arus yang cukup atau dua modul driver.

## Sensor yang bisa ditambahkan
- HC-SR04 / sensor jarak → obstacle avoidance
- Sensor IR → line follower
- INA219 / pembagi tegangan + sensor arus → telemetry baterai
- MPU6050 → kemiringan/IMU
- Encoder motor → odometri dan pengendalian kecepatan

## Catatan keselamatan
Tambahkan emergency stop hardware dan fail-safe software. Saat koneksi hilang, Arduino harus berhenti menggerakkan motor.
