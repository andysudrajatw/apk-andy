#include <WiFi.h>
#include <WebServer.h>

// ====== WIFI ======
const char* SSID = "NAMA_WIFI";
const char* PASSWORD = "PASSWORD_WIFI";

// Arduino UART -> ESP32 Serial2
// Sesuaikan GPIO dengan wiring Anda.
#define RXD2 16
#define TXD2 17

WebServer server(80);
String lastCommand = "STOP";
int lastSpeed = 0;

void cors() {
  server.sendHeader("Access-Control-Allow-Origin", "*");
  server.sendHeader("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
  server.sendHeader("Access-Control-Allow-Headers", "Content-Type");
}

void handleOptions() {
  cors();
  server.send(204);
}

void handleCommand() {
  cors();
  if (!server.hasArg("plain")) {
    server.send(400, "application/json", "{\"error\":\"JSON body required\"}");
    return;
  }

  String body = server.arg("plain");
  String command = "STOP";
  int speed = 60;

  // Parser minimal tanpa library JSON:
  if (body.indexOf("FORWARD") >= 0) command = "FORWARD";
  else if (body.indexOf("BACKWARD") >= 0) command = "BACKWARD";
  else if (body.indexOf("LEFT") >= 0) command = "LEFT";
  else if (body.indexOf("RIGHT") >= 0) command = "RIGHT";
  else if (body.indexOf("STOP") >= 0) command = "STOP";

  int p = body.indexOf("\"speed\"");
  if (p >= 0) {
    int colon = body.indexOf(':', p);
    int end = body.indexOf(',', colon);
    if (end < 0) end = body.indexOf('}', colon);
    if (colon >= 0 && end > colon) speed = constrain(body.substring(colon + 1, end).toInt(), 0, 100);
  }

  lastCommand = command;
  lastSpeed = speed;

  // Kirim ke Arduino: COMMAND,SPEED
  Serial2.println(command + "," + String(speed));

  String response = "{\"ok\":true,\"command\":\"" + command + "\",\"speed\":" + String(speed) + "}";
  server.send(200, "application/json", response);
}

void handleTelemetry() {
  cors();

  // Starter telemetry. Ganti dengan pembacaan sensor nyata.
  float battery = 12.0;
  float temperature = 29.0;
  int distance = 75;

  String json = "{\"battery\":" + String((int)((battery / 12.6) * 100)) +
                ",\"voltage\":" + String(battery, 1) +
                ",\"distance\":" + String(distance) +
                ",\"temperature\":" + String(temperature, 1) +
                ",\"mode\":\"MANUAL\",\"lastCommand\":\"" + lastCommand + "\"}";
  server.send(200, "application/json", json);
}

void setup() {
  Serial.begin(115200);
  Serial2.begin(115200, SERIAL_8N1, RXD2, TXD2);

  WiFi.mode(WIFI_STA);
  WiFi.begin(SSID, PASSWORD);

  while (WiFi.status() != WL_CONNECTED) {
    delay(300);
    Serial.print(".");
  }

  Serial.println();
  Serial.print("ESP32 Gateway IP: ");
  Serial.println(WiFi.localIP());

  server.on("/api/command", HTTP_OPTIONS, handleOptions);
  server.on("/api/command", HTTP_POST, handleCommand);
  server.on("/api/telemetry", HTTP_GET, handleTelemetry);
  server.begin();
}

void loop() {
  server.handleClient();
}