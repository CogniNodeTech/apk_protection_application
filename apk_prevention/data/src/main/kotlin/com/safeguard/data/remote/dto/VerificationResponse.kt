package com.safeguard.data.remote.dto

import com.squareup.moshi.Json

/**
 * Layer 6 cloud response. [virustotalLink] is the JSON field name for historical reasons;
 * the server may set it to a MalwareBazaar sample URL or another intel permalink — not necessarily VirusTotal.
 */
data class VerificationResponse(
    @Json(name = "verdict") val verdict: String,
    @Json(name = "confidence") val confidence: Float,
    @Json(name = "threat_name") val threatName: String?,
    @Json(name = "threat_family") val threatFamily: String?,
    @Json(name = "av_detections") val avDetections: Int?,
    @Json(name = "total_av_scanned") val totalAvScanned: Int?,
    @Json(name = "community_reports") val communityReports: Int?,
    @Json(name = "virustotal_link") val virustotalLink: String?,
    @Json(name = "evidence") val evidence: List<String>?,
    @Json(name = "recommendation") val recommendation: String?
)
