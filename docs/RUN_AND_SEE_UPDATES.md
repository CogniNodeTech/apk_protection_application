# Run SafeGuard and See All UI Updates

If the app on your device or emulator doesn’t show the latest UI (Dashboard, Scan Results, Quarantine, Settings), use a **clean build** and **reinstall** so the new version is installed.

---

## Run in Android Studio and see real-time output

To **run the app from Android Studio** and watch **real-time logs** as you use the app:

1. **Open the project**  
   Open the **inner** folder: `apk_prevention\apk_prevention` (the one that contains `app`, `core`, `data`, etc.).

2. **Select device**  
   In the toolbar, choose your **emulator or connected device**.

3. **Run the app**  
   Click the green **Run** (Play) button or press **Shift+F10**.  
   The app will build, install, and launch on the device.

4. **Open Logcat for real-time output**  
   - Go to **View → Tool Windows → Logcat** (or the **Logcat** tab at the bottom).  
   - In the Logcat filter bar, choose your **device** and the **com.safeguard** process (or your app package).  
   - In the filter box, type **`SafeGuard`** so only this app’s logs are shown.

5. **What you’ll see in Logcat**  
   As you use the app, you’ll see lines like:
   - `SafeGuard application started` — when the app starts  
   - `MainActivity created — app running` — when the main screen loads  
   - `Dashboard init — loading dashboard`  
   - `Dashboard loaded: X scans today, Y blocked, ...`  
   - `Tab: Dashboard (dashboard)` — when you switch bottom tabs  
   - `Real-Time Shield: ON` / `OFF` — when you toggle the shield  
   - `Schedule: enabled=..., time=..., frequency=...` — when you save a schedule  
   - `Scan started: filename.apk` / `Scan completed: id=..., verdict=...` — when you run a scan  

   So you get **real-time output in Android Studio** as you tap and use the app.

---

## 1. Uninstall the old app (important)

- **Emulator:** Long-press the SafeGuard app icon → **Uninstall** (or drag to Uninstall).
- **Physical device:** **Settings → Apps → SafeGuard → Uninstall.**

This forces the next install to use the new build instead of updating in place.

---

## 2. Clean and rebuild in Android Studio

1. Open the **inner** project: `apk_prevention\apk_prevention` (the folder that contains `app`, `core`, `data`, `security`).
2. **Build → Clean Project.** Wait until it finishes. (If Clean fails because files are in use, close any Gradle/IDE windows and try again, or skip to step 3.)
3. **Build → Rebuild Project.** Wait until you see **BUILD SUCCESSFUL**.

---

## 3. Run the app

1. Select your **device or emulator** in the toolbar.
2. Click **Run** (green play) or press **Shift+F10**.
3. The app will install fresh and open.

---

## 4. Confirm you’re on the latest build

1. In the app, open **Settings** (gear in the top-right of the Dashboard).
2. Scroll to the bottom.
3. You should see **App version 1.0.1 (2)**.  
   If you see **1.0.0 (1)** or no version line, the old build is still installed — repeat steps 1–3.

---

## 5. What you should see (updated UI)

- **Dashboard:** Title “Dashboard”, gear (Settings). **Threat Level** card (e.g. “LOW - SECURE”) with green styling, **Real-Time Shielded** toggle, four quick-action circles (Scan Now, Todos, Schedule, Reports), **Recent Activity** cards with CLEAN/verdict chip and arrow (tap opens scan result).
- **Scan** (bottom tab): **Scan Logs** with filter pills (Safe Apps / Q Threats), date groups (Today, Yesterday), verdict banners (CLEAN / QUARANTINED), bullet reasons, **Scan Again** and **Install Anyway** buttons.
- **Vault** (bottom tab): **Secure Vault** with filter pills (Malware, Reports), 2-column grid of vault cards with “Malware” tag; tap to expand risks and show **Send to Cloud** / **Install Anyway**.
- **Reports:** Protection Status with score (e.g. 60/100) and status cards.
- **Settings:** Protection, Notifications, Privacy, Advanced, **Appearance** (Theme: Light/Dark/System); version at bottom.

---

## 6. Real-time APK monitoring (all accessible folders)

When **Real-time monitoring** is on (Dashboard or Settings), the app monitors **all accessible public directories** for new APK files:

- **FileObserver:** Downloads, Documents, DCIM, Movies, Music, Pictures, Ringtones, and common subfolders (Telegram, WhatsApp, WhatsApp Media, Signal, Discord, Bluetooth, APK, Apps, Install, etc.) under each.
- **MediaStore (Android 10+):** New APKs that appear in the system Downloads content provider are also detected.

When a new APK is detected it is **scanned**, **recorded** in Scan History, and if **malicious or suspicious** it is **quarantined** (moved to Secure Vault and **removed from the original location**). Scan Logs, Protection Status, and Secure Vault update automatically.

**To test:** Enable Real-time monitoring, then download or copy an APK into any of the monitored folders (e.g. Downloads, or Downloads/Telegram). You should get a notification and the scan should appear under **Scan** (Scan Logs) and, if quarantined, under **Vault**. Tapping the notification opens the **Scan Results** screen for that scan.

---

## Optional: build from command line

From the **inner** `apk_prevention` directory:

```bash
# Windows (PowerShell)
.\gradlew clean
.\gradlew :app:assembleDebug
```

APK output: `app\build\outputs\apk\debug\app-debug.apk`.  
Install it after uninstalling the previous SafeGuard build.

If the command-line build fails with **Kotlin daemon** or **Unable to delete directory** errors, build and run from **Android Studio** (Build → Rebuild Project, then Run). That usually avoids daemon and file-lock issues.
