# ESP32 Gateway Contract

Folder ini menyiapkan spesifikasi gateway ESP32.

Endpoint:
POST /api/command

Payload:
{"command":"FORWARD","speed":60}

Telemetry dapat dipublikasikan lewat:
GET /api/telemetry
atau WebSocket message JSON.

Tujuan desain: ESP32 menerima perintah web lalu meneruskannya ke Arduino melalui Serial UART, dan meneruskan data sensor Arduino kembali ke dashboard.