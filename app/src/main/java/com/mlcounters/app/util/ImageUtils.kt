package com.mlcounters.app.util

import android.content.Context

object ImageUtils {
    private const val GITHUB_RAW_BASE = "https://raw.githubusercontent.com/Tasneem-Qureshi/MLCounters/main/app/src/main/assets/"

    fun resolveHeroPortraitUrl(context: Context, rawUrl: String): String {
        if (rawUrl.isBlank()) return ""
        if (rawUrl.startsWith("http://") || rawUrl.startsWith("https://")) return rawUrl
        if (rawUrl.startsWith("file:///android_asset/")) {
            val assetPath = rawUrl.removePrefix("file:///android_asset/")
            val exists = try {
                context.assets.open(assetPath).use { true }
            } catch (_: Exception) {
                false
            }
            if (exists) return rawUrl
            return "$GITHUB_RAW_BASE$assetPath"
        }
        return rawUrl
    }

    fun resolveItemIconUrl(context: Context, rawUrl: String): String {
        if (rawUrl.isBlank()) return ""
        if (rawUrl.startsWith("http://") || rawUrl.startsWith("https://")) return rawUrl
        if (rawUrl.startsWith("file:///android_asset/")) {
            val assetPath = rawUrl.removePrefix("file:///android_asset/")
            val exists = try {
                context.assets.open(assetPath).use { true }
            } catch (_: Exception) {
                false
            }
            if (exists) return rawUrl
            return "$GITHUB_RAW_BASE$assetPath"
        }
        return rawUrl
    }
}
