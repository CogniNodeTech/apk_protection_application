// SafeGuard — Android banker family content rules.
//
// These are deliberately conservative: each rule asks for several independent string /
// hex atoms before firing, so a benign app that happens to mention one keyword (e.g. a
// security blog) does not get flagged. Severity tiering:
//   95 = single hit is enough to BLOCK (mapped to MALICIOUS by Layer 7)
//   75-89 = multi-hit needed; SUSPICIOUS-tier
//   60-74 = lower-confidence pattern; surfaces but won't single-handedly block

rule SafeGuard_Anatsa_Banker {
  meta:
    author = "SafeGuard"
    family = "Anatsa"
    severity = 95
    description = "Anatsa / TeaBot banking trojan command and overlay strings"
    reference = "https://www.threatfabric.com/blogs/anatsa-banker-2024.html"
  strings:
    $cmd_overlay = "OverlayService"
    $cmd_inject = "injectorWebSocket" nocase
    $sec_screen = "SecureWindowSession"
    $accservice = "AccessibilityClickService"
    $c2_path = "/api/getInjects"
    $bot_id = "BOT_ID="
  condition:
    3 of them
}

rule SafeGuard_Hydra_Banker {
  meta:
    author = "SafeGuard"
    family = "Hydra"
    severity = 90
    description = "Hydra / BianLian Android banker (overlay + USSD abuse)"
  strings:
    $tag1 = "Hydra-Bot" nocase
    $tag2 = "ussdControl"
    $tag3 = "screenStreamer"
    $tag4 = "OnAccessibilityEvent_Hydra"
    $tag5 = "HVNC_Module"
  condition:
    2 of them
}

rule SafeGuard_BankBot_Generic {
  meta:
    author = "SafeGuard"
    family = "BankBot"
    severity = 85
    description = "Generic BankBot / Cerberus-lineage overlay banker artefacts"
  strings:
    $a = "com.banker.overlay" nocase
    $b = "InjectActivity"
    $c = "SmsForwardService"
    $d = "ussd_command"
    $e = "ATSEngine"
    $f = "stealCardModule"
  condition:
    3 of them
}

rule SafeGuard_GodFather_Banker {
  meta:
    author = "SafeGuard"
    family = "GodFather"
    severity = 90
    description = "GodFather banker — successor to Anubis"
  strings:
    $g1 = "godFatherInject"
    $g2 = "GFC2Endpoint" nocase
    $g3 = "victim_targets.json"
    $g4 = "moduleScreencast"
  condition:
    2 of them
}
