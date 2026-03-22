<div align="center">
  <h1>SmartAttend</h1>
  <h3>for University of Perpetual Help System DALTA - Molino Campus</h3>
  <p><b>Final Project in Computer Programming 2 : Java</b><br>
  <i><a href="https://github.com/chwhiz">Gabriel Martin Guillergan</a> | <a href="https://github.com/ICTZen">ICT 11-02</a></i></p>

  <p>A student attendance tracking system built to replace traditional issuance of admission slips featuring RFID scanning and TOTP 2FA authentication (for admins).</p>

  <!-- Badges -->
  <img src="https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white" />
  <img src="https://img.shields.io/badge/NetBeans-1B6AC6?style=for-the-badge&logo=apache-netbeans&logoColor=white" />                                       
  <img src="https://img.shields.io/badge/RFID-Scanner-brightgreen?style=for-the-badge" />
</div>

<br/>

## 📑 Table of Contents
- [About the Project](#-about-the-project)
- [Key Features](#-key-features)
- [System Architecture](#-system-architecture)
  - [Logic Layer](#logic-layer)
  - [Model Layer](#model-layer)
  - [User Interface (UI)](#user-interface)
- [Getting Started](#-getting-started)

## 📖 About the Project
**SmartAttend** is a Java-based desktop application designed to streamline the way student attendance is recorded and managed. Built with a robust UI and secure backend logic, it minimizes manual entry errors and ensures administrative data remains protected through Time-Based One-Time Passwords (TOTP).

## ✨ Key Features
- **💳 RFID Integration:** Quickly scan student IDs for seamless checking in and out.
- **🔐 Admin Security:** Two-Factor Authentication via standard TOTP for admin dashboards.
- **📅 Schedule Management:** Dynamically configure time slots and subjects.
- **📊 Detailed Logs:** View, track, and manage student attendance history.

## 🏗️ System Architecture

### Logic Layer
Handles the core business rules, database connectivity, and security features.
| Component | Description |
|-----------|-------------|
| ⚙️ [`AttendanceProcessor.java`](src/logic/AttendanceProcessor.java) | Processes entry timestamps. Determines prompt, late, or invalid scans. |
| 🗄️ [`DatabaseManager.java`](src/logic/DatabaseManager.java) | Manages database connections and abstracts CRUD operations. |
| 🔑 [`SimpleTOTP.java`](src/logic/SimpleTOTP.java) | Implements TOTP algorithm to secure admin access. |

### Model Layer
Defines the core data structures and objects within the system.
| Component | Description |
|-----------|-------------|
| 👤 [`AdminUser.java`](src/model/AdminUser.java) | Represents the admin entity with credentials and metadata. |
| 📩 [`AttendanceResult.java`](src/model/AttendanceResult.java) | Encapsulates the outcome of a scan to pass status messages safely. |
| 🕒 [`ScheduleItem.java`](src/model/ScheduleItem.java) | Holds data for class schedules, time slots, and subjects. |

### User Interface
The presentation layer, consisting of interactive Swing UI components.
| Component | Description |
|-----------|-------------|
| 🖥️ [`MainFrame.java`](src/ui/MainFrame.java) | Main entry point and the primary application window. |
| 🛡️ [`AdminAuthDialog.java`](src/ui/AdminAuthDialog.java) | Login form for system administrators to authenticate securely. |
| 🎛️ [`AdminManagerFrame.java`](src/ui/AdminManagerFrame.java) | Dashboard granting access to various management modules (students, sections, logs). |
| 📋 [`AttendanceLogFrame.java`](src/ui/AttendanceLogFrame.java) | Module for viewing historical table logs of recorded attendances. |
| 🛠️ [`DevModeFrame.java`](src/ui/DevModeFrame.java) | Advanced developer tools, diagnostic features, or manual overrides. |
| 📻 [`RFIDCaptureDialog.java`](src/ui/RFIDCaptureDialog.java) | Dedicated dialog that listens to and interprets input from connected RFID barcode scanners. |
| 🕒 [`ScheduleManagerFrame.java`](src/ui/ScheduleManagerFrame.java) | Interface for modifying, adding, and reviewing class time schedules. |
| 📚 [`SectionManagerDialog.java`](src/ui/SectionManagerDialog.java) | Allows setting up grade sections and category assignments. |
| ⏳ [`SplashScreen.java`](src/ui/SplashScreen.java) | The introductory screen shown during initial loading times. |
| 👥 [`StudentManagerFrame.java`](src/ui/StudentManagerFrame.java) | Central interface to register new students, edit details, or assign RFID tags. |
| 💬 [`ToastNotification.java`](src/ui/ToastNotification.java) | Non-intrusive popup element used to show brief feedback messages. |
| 📥 [`TOTPOnboardingDialog.java`](src/ui/TOTPOnboardingDialog.java) | Guides the admin through 2FA setup (providing the initial QR secret). |
| 🖌️ [`UIBuilder.java`](src/ui/UIBuilder.java) | Utility factory containing helper methods to assemble and style UI components uniformly. |

## 🚀 Getting Started

### Prerequisites
- **Java Development Kit (JDK) 8+**
- **Apache NetBeans IDE**
- RFID Scanner (125khz)
- MariaDB Server (i used the mariadb in my homelab server and used wireguard but it is recommended that you run the kiosk and server in the same network for reliability.)

### Installation & Execution
1. Open **NetBeans IDE**.
2. Select `File` > `Open Project` and select the root directory: **ICT 11-02 Attendance System**.
3. **Clean and Build** the project.
4. Run `MainFrame.java` to start the application.

---
<div align="center">
  <i>Developed for the Senior High School Department</i>
  </br>
  <b><i>Soar High, Senior High!</i></b>
</div>
