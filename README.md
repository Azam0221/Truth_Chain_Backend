# TruthChain - Backend Ledger & Trust Anchor 

The **TruthChain Backend** serves as the central immutable ledger and orchestration layer for the platform. It acts as a bridge between the secure hardware client (Android) and the decentralized storage network (IPFS/Pinata).

## Key Responsibilities

* **Trust Bridge:** Receives cryptographically signed evidence from trusted Android hardware.
* **Decentralized Storage (IPFS):** Automatically pins uploaded evidence to **IPFS via Pinata**, ensuring the media is immutable and uncensorable.
* **Verification Engine:** Exposes public endpoints to verify asset integrity. If an image hash is queried, it returns the associated hardware signature and metadata.
* **Metadata Registry:** Stores the "Proof of Presence" data (GPS coordinates, Timestamp, Device ID) linked to the image hash.

## Tech Stack

* **Framework:** Spring Boot 3 (Java 17)
* **Build Tool:** Maven / Gradle
* **Database:** MySQL 
* **Storage:** IPFS (via Pinata API)
* **Deployment:** Railway
* **Security:** SHA-256 Hash Verification

## API Documentation

### 1. Upload Evidence (Internal/Android)
Receives the raw image and the hardware-backed signature.

* **Endpoint:** `POST /api/evidence/upload`
* **Content-Type:** `multipart/form-data`
* **Parameters:**
    * `file`: The image file.
    * `signature`: The ECDSA signature string from the Android Keystore.
    * `metadata`: JSON string containing GPS and Timestamp.

### 2. Verify Evidence (Public/Web)
Used by the React frontend to check if a file is authentic.

* **Endpoint:** `GET /api/evidence/verify/{hash}`
* **Path Variable:** `{hash}` - The SHA-256 hash of the file being checked.
* **Response:**
    ```json
    {
      "status": "VERIFIED",
      "digitalSignature": "MEYCIQC...",
      "gpsLocation": "26.8467, 80.9462",
      "timestamp": "1735482300000",
      "deviceId": "Pixel_7_Pro_Secure_Enclave"
    }
    ```

## Architecture Flow

1.  **Ingest:** Receives `MultipartFile` and `Signature` from Android.
2.  **Hash:** Calculates the server-side SHA-256 hash of the received file to ensure integrity during transit.
3.  **Pin:** Uploads the file to **Pinata (IPFS)** and retrieves the CID (Content Identifier).
4.  **Persist:** Saves the `{Hash, CID, Signature, Metadata}` tuple to the database.

## Getting Started

### Prerequisites
* Java 17 SDK
* Maven
* Pinata API Keys

### Installation

1.  Clone the repository:
    ```bash
    git clone [https://github.com/yourusername/truthchain-backend.git](https://github.com/yourusername/truthchain-backend.git)
    ```
2.  Configure Environment Variables (`application.properties`):
    ```properties
    pinata.api.key=YOUR_KEY
    pinata.secret.key=YOUR_SECRET
    spring.datasource.url=jdbc:postgresql://...
    ```
3.  Run the application:
    ```bash
    mvn spring-boot:run
    ```

