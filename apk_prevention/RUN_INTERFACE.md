# SafeGuard – Run Interface

Use this page to run the app and get the expected output.

---

## 1. One-time setup (if not done)

| Step | Action |
|------|--------|
| 1 | Install **Android Studio**: https://developer.android.com/studio |
| 2 | Open Android Studio → **File → Open** → select folder `d:\apk_prevention` |
| 3 | Wait for **Gradle sync** to finish (Android Studio will write `local.properties` with `sdk.dir`) |
| 4 | (Optional) Create an **Android Virtual Device**: Tools → Device Manager → Create Device |

If you prefer **no Android Studio**: set `sdk.dir` in `local.properties` to your Android SDK path (e.g. `C:\\Users\YourName\\AppData\\Local\\Android\\Sdk`).

---

## 2. Commands to run

Run from project root `d:\apk_prevention` (PowerShell or Command Prompt).

### Build (frontend + everything)

```bat
gradlew.bat assembleDebug
```

### Install and run on device/emulator

```bat
gradlew.bat installDebug
```

*(Device or emulator must be connected and authorized.)*

### Or use the run script

```bat
run.bat
```

`run.bat` builds the project and prints the APK path (or asks for SDK setup if needed).

---

## 3. Perfect output (when build succeeds)

When everything is set up correctly, you should see output like this:

```
> Task :app:compileDebugKotlin
> Task :app:mergeDebugNativeLibs
> Task :app:stripDebugDebugSymbols
> Task :app:packageDebug
> Task :app:assembleDebug

BUILD SUCCESSFUL in 2m 15s
123 actionable tasks: 123 executed
```

**APK location:**

```
app\build\outputs\apk\debug\app-debug.apk
```

**To run on device/emulator:**

```
gradlew.bat installDebug
```

Then open the **SafeGuard** app on the device.

---

## 4. If build fails

| Message | Fix |
|---------|-----|
| `SDK location not found` | Set `sdk.dir` in `local.properties` to your Android SDK path, or open project in Android Studio once. |
| `Unresolved reference: ksp` | Already fixed in `data/build.gradle.kts`. If you see it, run **File → Sync Project with Gradle Files**. |
| `FAILURE: Build failed` | Read the line above it (e.g. missing SDK, compile error). Fix that and run again. |

---

## 5. Quick reference

| Goal | Command |
|------|---------|
| Build APK | `gradlew.bat assembleDebug` |
| Install + run | `gradlew.bat installDebug` |
| Clean | `gradlew.bat clean` |
| Build using script | `run.bat` |

All commands are run from: **d:\apk_prevention**
