# SafeGuard – Verify All Updated Changes

After you **uninstall the old app** and **install the new build**, use this checklist to confirm all updates are present.

---

## Install the new build

**Option A – From Android Studio (recommended)**  
1. Open project: `apk_prevention\apk_prevention`  
2. **File → Sync Project with Gradle Files**  
3. **Build → Rebuild Project**  
4. Select your device/emulator → click **Run** (green play) or **Shift+F10**

**Option B – Install APK manually**  
- APK path: `app\build\outputs\apk\debug\app-debug.apk`  
- Copy to your device and open the file to install (allow “Install from unknown sources” if prompted).

---

## Verification checklist

### 1. Version (confirm new build)
- [ ] Open **Settings** (gear on Dashboard)
- [ ] Scroll to the very bottom
- [ ] You see **App version 1.0.1 (2)**  
  - If you see 1.0.0 (1), the old build is still installed — uninstall SafeGuard and install again.

### 2. Dashboard
- [ ] **Protection status** card with shield icon and “Active” (green)
- [ ] **Security score** card with a **circular gauge** (arc), number, and text **“out of 100”**
- [ ] **Threat database** card under Real-Time Shield (green / amber / red dot, “Updated N min/h/d ago” headline). On a fresh install before the first sync this shows red “Threat database has never synced”.
- [ ] **Recent scans** section (title says “Recent scans”, not “Recent activity”)
- [ ] **AI model ready** and **Cloud check available** line
- [ ] **Did you know?** card with a tip
- [ ] **Scan an APK file** – solid green primary button
- [ ] **View Quarantine** – outlined secondary button
- [ ] **Settings** – button with gear icon in the top bar

### 3. Scan Results (after scanning an APK)
- [ ] **Verdict banner** (green / amber / red by result)
- [ ] **Risk gauge** with score and “Risk score” label
- [ ] **How we decided** with layer breakdown cards
  - [ ] **Layer 2 (Hash database)**: on a clean APK shows SHA-256 *and* SHA-512 in the evidence (both present after the single-pass refactor)
  - [ ] **Layer 7 (Pattern rules)**: present in the breakdown for every scan; clean APKs read “No YARA rules matched”, malicious-pattern matches list the rule names
- [ ] **Done** button

### 4. Quarantine
- [ ] List of cards (or empty state: “No files in quarantine”)
- [ ] Each item has **Restore** and **Delete**
- [ ] **Restore** opens a confirmation dialog; confirm shows a snackbar

### 5. Settings
- [ ] **Protection:** Real-time monitoring, Deep scan mode (switches)
- [ ] **Notifications:** Notification level with description
- [ ] **Privacy:** Privacy policy row
- [ ] **Advanced:** Family Guardian “Coming soon”
- [ ] **App version 1.0.1 (2)** at the bottom

---

If every item matches, all updated changes are present.
