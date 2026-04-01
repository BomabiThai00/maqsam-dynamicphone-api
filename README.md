
# Maqsam DynamicPhone Webhook API

This is a production-ready Spring Boot application designed to act as a real-time call routing webhook for the **Maqsam DynamicPhone API**. 

When configured in a Maqsam IVR flow, Maqsam will send a real-time HTTP request to this application. The application processes the caller's information (like their phone number) and responds with a plain-text destination number, allowing you to dynamically route VIPs to priority queues, or return a `skip` command to let Maqsam handle the fallback routing.

## 🚀 Features

* **Dual GET & POST Support:** Seamlessly handles both `GET` (query parameters) and `POST` (`application/x-www-form-urlencoded`) requests from Maqsam on the exact same root endpoint `/`.
* **Strict Security Bypasses:** Pre-configured to handle various `Content-Type` header quirks (like injected charsets) from third-party HTTP clients.
* **Tomcat Bracket Relaxation:** Custom Spring Boot configuration that allows raw square brackets (`[` and `]`) in GET request URLs, preventing Tomcat from instantly dropping Maqsam array payloads (e.g., `&integrations[zendesk]=...`).
* **Raw Request Logging:** Includes a custom Servlet Filter (`RawRequestLoggingFilter`) that safely intercepts and caches the exact, raw HTTP network payload (Headers + Body) and writes it to a local `maqsam_raw_requests.log` file for flawless debugging.
* **Fail-Safe Logic:** Wrapped in `try-catch` blocks. If database/logic checks crash, the app safely returns `skip` so the caller is never dropped.

## 📋 Prerequisites

* **Java 25** (Ensure `JAVA_HOME` is set).
* **ngrok** (If running locally and exposing to the internet for Maqsam to reach).
* Note: You do *not* need to install Maven. The project uses the Maven Wrapper (`mvnw`).

## 🛠️ Getting Started

**1. Clone the repository and navigate into the folder:**
```bash
git clone [https://github.com/YOUR_USERNAME/maqsam-spring-boot-webhook.git](https://github.com/YOUR_USERNAME/maqsam-spring-boot-webhook.git)
cd maqsam-spring-boot-webhook
```

**2. Make the Maven wrapper executable (Linux/Mac only):**
```bash
chmod +x mvnw
```

**3. Run the application:**
```bash
./mvnw spring-boot:run
```
*The application will start on `http://localhost:8080`.*

## 🌐 Connecting to Maqsam (Local Testing)

If you are running this on your local machine, Maqsam's servers cannot reach `localhost`. You must use a tunneling service like ngrok.

1. In a new terminal window, start ngrok:
   ```bash
   ngrok http 8080
   ```
2. Copy the secure `https://...` URL provided by ngrok.
3. Open the **Maqsam Portal** -> **IVR Flows** -> **DynamicPhone Node**.
4. Paste the ngrok URL into the Endpoint URL field.
5. Set the method to `GET` or `POST`. 
6. Make a test call!

## 🧠 Modifying the Routing Logic

The core decision-making happens inside `src/main/java/com/yourcompany/telephony/service/CallRoutingService.java`. 

Currently, the application is hardcoded with a demo VIP logic:
* If the caller's number is `966115201360`, it routes the call to `201123066960`.
* All other numbers return `skip`.

To connect this to a real database, replace the `isVipCustomer` method with your actual JPA/Hibernate repository queries.

## 🛡️ Security Note

The `RawRequestLoggingFilter` writes incoming HTTP traffic to `maqsam_raw_requests.log` in the root directory. This file is explicitly ignored in `.gitignore` to prevent sensitive phone numbers and server data from being pushed to version control. **Do not remove this ignore rule.**

### 💻 The `cURL` Commands for Testing

Based on the exact logic you wrote in `CallRoutingService.java`, the endpoint is looking for a `caller_number` parameter matching `966115201360`. If it finds it, it will successfully return `201123066960`.

Here are the single-line `curl` commands to test both the POST and GET configurations of your endpoint. 

*(Note: Assuming you are testing locally on port 8080. If you are testing through ngrok, replace `http://localhost:8080/` with your ngrok URL).*

#### Test 1: The POST Request
```bash
curl -X POST http://localhost:8080/ -H "Content-Type: application/x-www-form-urlencoded" -d "id=test_post_123&caller_number=966115201360"
```
**Expected Output:** `201123066960`

#### Test 2: The GET Request
```bash
curl -X GET "http://localhost:8080/?id=test_get_456&caller_number=966115201360"
```
**Expected Output:** `201123066960`

#### Test 3: Standard Caller (Testing the Fallback)
```bash
curl -X POST http://localhost:8080/ -H "Content-Type: application/x-www-form-urlencoded" -d "id=test_standard_789&caller_number=962799999999"
```
**Expected Output:** `skip`
