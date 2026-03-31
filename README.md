# BluePay - Offline Digital Payment Solution

BluePay is a resilient, offline-first mobile payment application designed to handle digital transactions using short-range radio waves without requiring an active internet connection. Value tokens are exchanged securely offline and finalized automatically when devices reach a network.

##  Implemented Features

This project covers the complete lifecycle of a secure fintech application, fulfilling both foundational assignments and advanced architectural concepts.

### 1. Security & Authentication (Q04 & Q02)
*   **Biometric Authorization**: Every payment requires a fingerprint or face scan using the `BiometricPrompt` API.
*   **Hardware-Backed Security**: Uses **Android Keystore** to generate and store cryptographic keys in the phone's Secure Element (StrongBox/TEE).
*   **Digital Signatures**: Transactions are digitally signed using the private key after biometric verification, ensuring tokens are tamper-proof.
*   **Double-Spend Prevention**: Implements a unique Nonce system to prevent replay attacks during offline exchange.

### 2. Offline Connectivity & Observation (Week 2 & Q10)
*   **Connectivity Manager**: Real-time monitoring of network states (Online/Offline) with a dynamic status banner.
*   **Nearby Connections API**: Integration of Google's Nearby API for P2P data exchange via Bluetooth/Wi-Fi Direct.
*   **Airplane Mode Receiver**: A broadcast receiver that detects system-wide communication changes to shift the app into "Offline Ready" mode.

### 3. Data Persistence & Synchronization (Q07 & Q08)
*   **Room Database**: All transactions are saved into a local secure SQLite database, ensuring history survives app restarts.
*   **Sync Queue**: Transactions are stored with a `PENDING_SYNC` status, ready for background processing via `WorkManager` upon network reconnection.
*   **Reactive History**: A professional Ledger screen using Kotlin Flows to provide real-time updates as payments are made.

### 4. Interactive Location & UI (Week 1 & Map Module)
*   **Interactive Maps**: A dynamic Google Maps module that centers on the user's location during payment.
*   **Geotagging**: Captures and displays Latitude/Longitude metadata for every transaction to prevent geographic fraud.
*   **Hardware Auditing**: A dedicated dashboard that checks the device for essential payment capabilities like NFC and Biometrics.
*   **Metaphorical Design**: Uses an intuitive UI with sliders for magnitude and clear icons for financial actions.

### 5. Multimedia Messaging
*   **Deep-Link Notifications**: System-wide alerts for successful payments.
*   **Smart Navigation**: Tapping a notification automatically redirects the user to the Transaction History screen.

---

## 🛠 Tech Stack
*   **UI**: Jetpack Compose (Material 3)
*   **Language**: Kotlin
*   **Database**: Room
*   **Security**: Biometric API + Android Keystore
*   **Connectivity**: Nearby Connections API + ConnectivityManager
*   **Maps**: Google Maps SDK for Android
*   **Background Tasks**: WorkManager

---

## 🔧 Setup Instructions

1.  **Google Maps API Key**:
    *   Generate a key in the [Google Cloud Console](https://console.cloud.google.com/).
    *   Enable **Maps SDK for Android**.
    *   Add the key to `AndroidManifest.xml` under `com.google.android.geo.API_KEY`.

2.  **Permissions**:
    *   The app will request **Location** and **Notification** permissions on the first launch. These are required for the map and receipt alerts to function.

3.  **Biometrics**:
    *   Ensure your device has at least one fingerprint or face enrolled in the system settings to test the payment flow.

---

## 📱 How to Use
1.  **Payment**: Go to the Payment tab, type or slide an amount, and click "Initiate Offline Payment".
2.  **Authenticate**: Scan your biometric when prompted.
3.  **View Result**: Observe the success receipt and system notification.
4.  **History**: Switch to the History tab to see your permanent, signed record of the transaction.
