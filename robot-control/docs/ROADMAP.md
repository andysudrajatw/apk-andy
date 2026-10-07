# Roadmap Robot Control Center

## Fase 1 — selesai
- Dashboard web
- Kontrol arah dan keyboard
- Pengaturan speed
- Mode operasi
- Editor program Arduino
- Monitoring dasar
- Kontrak API
- Firmware ESP32 gateway
- GitHub Pages workflow

## Fase 2 — integrasi nyata
1. ESP32 menerima Wi-Fi.
2. ESP32 menerima POST /api/command.
3. ESP32 meneruskan command ke Arduino via Serial2.
4. Arduino menjalankan motor + fail-safe STOP.
5. Arduino mengirim telemetry.
6. ESP32 menyajikan /api/telemetry.
7. Dashboard melakukan polling/WebSocket telemetry.

## Fase 3 — multi-perangkat
- QR code menuju alamat dashboard.
- Semua HP/laptop pada Wi-Fi robot dapat melihat status.
- Command diberi session/client ID.
- Hanya satu client menjadi controller aktif.
- Client lain menjadi monitor/read-only.
- Tombol Emergency STOP tetap tersedia untuk semua client.

## Fase 4 — autonomous
- Line follower.
- Obstacle avoidance.
- Waypoint.
- Speed profile.
- Encoder/odometry.
- PID motor.

## Fase 5 — pemrograman
- Template sketch per jenis board.
- Pin configurator.
- Library checklist.
- Validasi compile dasar.
- Upload melalui gateway/PC agent.

## Fase 6 — keamanan
- Token/password perangkat.
- Pairing client.
- Rate limit command.
- Audit event log.
