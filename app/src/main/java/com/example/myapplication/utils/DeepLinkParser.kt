package com.example.myapplication.utils

import android.content.Intent

object DeepLinkParser {
    fun extractUid(intent: Intent?): String? {
        val data = intent?.data ?: return null
        // Esperamos: https://linkup.app/addfriend?uid=xxxxx
        return if (data.scheme == "https" && data.host == "linkup.app" && data.path == "/addfriend") {
            data.getQueryParameter("uid")
        } else {
            null
        }
    }
}
