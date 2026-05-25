// SafeGuard — droppers, packers, and runtime-evasion patterns.

rule SafeGuard_Generic_Dropper {
  meta:
    author = "SafeGuard"
    family = "Dropper"
    severity = 85
    description = "Two-stage dropper: dynamic dex-loading combined with REQUEST_INSTALL_PACKAGES intent surface"
  strings:
    $dexcl1 = "dalvik.system.DexClassLoader"
    $dexcl2 = "PathClassLoader"
    $install1 = "android.intent.action.INSTALL_PACKAGE"
    $install2 = "REQUEST_INSTALL_PACKAGES"
    $base64 = "Base64.decode" nocase
    $url_payload = "/payload.dex" nocase
  condition:
    ($dexcl1 or $dexcl2) and ($install1 or $install2) and ($base64 or $url_payload)
}

rule SafeGuard_Accessibility_Overlay_Trojan {
  meta:
    author = "SafeGuard"
    family = "AccessibilityAbuse"
    severity = 95
    description = "Accessibility-service abuse coupled with overlay window — banker pattern"
  strings:
    $acc1 = "android.accessibilityservice.AccessibilityService"
    $acc2 = "performGlobalAction"
    $ovl1 = "TYPE_APPLICATION_OVERLAY"
    $ovl2 = "TYPE_SYSTEM_ALERT_WINDOW"
    $ovl3 = "WindowManager.LayoutParams"
    $screencast = "MediaProjectionManager" nocase
  condition:
    ($acc1 and $acc2) and (($ovl1 or $ovl2 or $ovl3) or $screencast)
}

rule SafeGuard_AVS_Evasion_Strings {
  meta:
    author = "SafeGuard"
    family = "Evasion"
    severity = 75
    description = "Hard-coded AVS / sandbox / emulator detection strings — typical packer telltales"
  strings:
    $emu1 = "goldfish"
    $emu2 = "ranchu"
    $emu3 = "QEMU_PROPERTY"
    $emu4 = "/system/bin/qemu-props"
    $sandbox1 = "isProbablySandbox"
    $sandbox2 = "isAvsInstalled" nocase
    $av1 = "com.avast.android.mobilesecurity"
    $av2 = "com.kaspersky.security"
    $av3 = "com.eset.ems2.gp"
  condition:
    3 of them
}

rule SafeGuard_BadPack_ZipTamper {
  meta:
    author = "SafeGuard"
    family = "BadPack"
    severity = 90
    description = "BadPack-style ZIP local-header tampering: APKs that abuse zip parser disagreements to hide their real manifest from analysers"
  strings:
    // Classic BadPack header bytes: PK\x03\x04 followed by atypical version / general
    // purpose flag tuples that surface in obfuscated APKs but never in clean Android
    // builds. The fixed prefix 50 4B 03 04 is the local file header magic; the trailing
    // mask catches the unusual general purpose / extra-field combinations BadPack uses.
    $hdr = { 50 4B 03 04 ?? ?? 09 08 ?? ?? }
    $tag = "BadPack" nocase
    $unicode_flag = { 50 4B 03 04 ?? ?? 0B 08 }
  condition:
    $hdr or $unicode_flag or $tag
}

rule SafeGuard_Frida_Gadget {
  meta:
    author = "SafeGuard"
    family = "InstrumentationFramework"
    severity = 70
    description = "Frida gadget / server strings — common in repackaged APKs used for credential theft and licence bypass"
  strings:
    $f1 = "frida-gadget"
    $f2 = "frida-server"
    $f3 = "FridaGadget"
    $f4 = "REframework"
  condition:
    any of them
}
