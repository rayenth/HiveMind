#include <WiFi.h>
#include <HTTPClient.h>
#include <ArduinoJson.h>
#include <time.h>

// WiFi credentials
const char* ssid = "Alpinia";
const char* password = "Alpinia@2025";

// Backend endpoint
const char* serverUrl = "http://192.168.100.141:8080/iot/ingest-log";

// Device ID
const char* deviceId = "esp32-sensor-01";

void setup() {
  Serial.begin(115200);

  // Connect to WiFi
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.println("Connecting to WiFi...");
  }
  Serial.println("Connected to WiFi");

  // Init time
  configTime(0, 0, "pool.ntp.org", "time.nist.gov");
}

void loop() {
  if (WiFi.status() == WL_CONNECTED) {
    HTTPClient http;

    // Create JSON payload
    StaticJsonDocument<200> doc;
    doc["deviceId"] = deviceId;
    doc["temperature"] = random(200, 300) / 10.0; // Random temp 20.0 - 30.0
    doc["status"] = "ONLINE";
    
    // Get current time in ISO 8601 format
    time_t now;
    time(&now);
    char timeString[30];
    strftime(timeString, sizeof(timeString), "%Y-%m-%dT%H:%M:%SZ", gmtime(&now));
    doc["timestamp"] = timeString;

    String requestBody;
    serializeJson(doc, requestBody);

    // Send POST request
    http.begin(serverUrl);
    http.addHeader("Content-Type", "application/json");
    
    int httpResponseCode = http.POST(requestBody);

    if (httpResponseCode > 0) {
      String response = http.getString();
      Serial.println(httpResponseCode);
      Serial.println(response);
    } else {
      Serial.print("Error on sending POST: ");
      Serial.println(httpResponseCode);
    }

    http.end();
  } else {
    Serial.println("WiFi Disconnected");
  }

  // Send log every 5 seconds
  delay(5000);
}
