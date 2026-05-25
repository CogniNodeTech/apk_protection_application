# Android Studio Setup – SafeGuard (APK Prevention) App

Follow these steps to set up Android Studio and run the SafeGuard application.

---

## 1. Install JDK 17

The project requires **JDK 17**.

- **Option A – Use Android Studio’s JDK**  
  Android Studio can bundle a JDK. During/after installation, you can point the project to it (see step 4).

- **Option B – Install JDK 17 manually**  
  1. Download [Eclipse Temurin 17 (LTS)](https://adoptium.net/temurin/releases/?version=17) or [Oracle JDK 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html).  
  2. Run the installer.  
  3. Set `JAVA_HOME` to the JDK 17 install folder (e.g. `C:\Program Files\Eclipse Adoptium\jdk-17.0.x-hotspot`).

---

## 2. Download and Install Android Studio

1. Go to: **https://developer.android.com/studio**  
2. Click **Download Android Studio**.  
3. Run the installer (`android-studio-xxxx-windows.exe`).  
4. Follow the wizard:
   - **Next** through the welcome and license screens.
   - Choose **Standard** installation.
   - Pick a UI theme (e.g. Light or Dark).
   - Click **Finish** when done.

---

## 3. First Launch – SDK and Tools

1. Start **Android Studio**.  
2. On first run you may see **“Import Android Studio Settings”** – choose **Do not import settings** (or import if you have a backup).  
3. In the setup wizard:
   - Click **Next** until you reach **Install Type**.
   - Choose **Standard** and click **Next**.
   - Confirm the **SDK location** (e.g. `C:\Users\<YourUser>\AppData\Local\Android\Sdk`).  
     Remember this path – you’ll use it for `ANDROID_HOME` / `local.properties`.
   - Click **Next**, then **Finish**.  
4. Wait for the SDK and tools to download and install.

---

## 4. Set JDK 17 for the Project (if not using bundled JDK)

1. In Android Studio: **File → Settings** (or **Ctrl+Alt+S**).  
2. Go to **Build, Execution, Deployment → Build Tools → Gradle**.  
3. Under **Gradle JDK**, select **JDK 17** (or the path where you installed JDK 17).  
4. Click **Apply** → **OK**.

---

## 5. Open the SafeGuard Project

1. In Android Studio: **File → Open**.  
2. Browse to the project root.  
   - If your repo is at `d:\Cogninode Company Projects\apk_prevention`, open the **inner** `apk_prevention` folder (the one that contains `build.gradle.kts`, `gradlew.bat`, `app`, `core`, etc.).  
   - Correct path example: `d:\Cogninode Company Projects\apk_prevention\apk_prevention`  
3. Click **OK**.  
4. If prompted “Trust and Open Project?”, click **Trust Project**.

---

## 6. Gradle Sync and SDK

1. Android Studio will start **Gradle Sync**.  
2. If it asks to install missing SDK components (e.g. “Android SDK Build-Tools 34.0.0”, “Android SDK Platform 34”), click **Install** or **Accept** and wait for downloads.  
3. If you see **“SDK location not found”**:
   - **Option A:** Set environment variable `ANDROID_HOME` to your Android SDK path (e.g. `C:\Users\<YourUser>\AppData\Local\Android\Sdk`).  
   - **Option B:** In the project root, create or edit `local.properties` and add (use your actual path):
     ```properties
     sdk.dir=C\:\\Users\\<YourUser>\\AppData\\Local\\Android\\Sdk
     ```
   - Restart Android Studio and sync again.

---

## 7. Build Variant and Run

1. In the toolbar, open the **Build Variants** tool window (or **View → Tool Windows → Build Variants**).  
2. Set the **Build Variant** to **debug** for the `app` module.  
3. Connect an Android device with **USB debugging** enabled, or start an **Android Emulator** (API 26 or higher; the app uses `minSdk 26`).  
4. In the toolbar, select the `app` run configuration and your device/emulator.  
5. Click the green **Run** button (**Run → Run 'app'** or **Shift+F10**).

The app will build, install, and launch on the device/emulator.

---

## 8. (Optional) Run from Command Line

After Android Studio has set up the SDK (and `local.properties` or `ANDROID_HOME` is set):

1. Open a terminal in the **project root** (the folder that contains `gradlew.bat`).  
2. Build the debug APK:
   ```bat
   gradlew.bat :app:assembleDebug
   ```
3. Install on a connected device:
   ```bat
   adb install -r app\build\outputs\apk\debug\app-debug.apk
   ```
   (`adb` is in the SDK `platform-tools` folder; add it to `PATH` or use its full path.)

---

## Quick Checklist

- [ ] JDK 17 installed and selected in Android Studio (Gradle JDK).  
- [ ] Android Studio installed with Android SDK.  
- [ ] Project opened from the correct folder (the one with `build.gradle.kts` and `gradlew.bat`).  
- [ ] Gradle sync completed without “SDK location not found”.  
- [ ] Build Variant set to **debug**.  
- [ ] Device or emulator (API 26+) connected.  
- [ ] **Run 'app'** to launch SafeGuard.

---

## Requirements (from README)

- **Android**: minSdk 26, targetSdk 34  
- **IDE**: Android Studio Hedgehog or later  
- **JDK**: 17  
- **Kotlin**: 1.9+

If you hit a specific error (e.g. “SDK location not found”, Gradle sync failure, or “Minimum supported Gradle version”), use the steps above that match that error (SDK path, JDK, or opening the correct folder).
