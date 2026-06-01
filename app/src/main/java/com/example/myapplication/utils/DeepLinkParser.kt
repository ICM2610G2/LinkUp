package com.example.myapplication.utils

import android.content.Intent

object DeepLinkParser {
    fun extractUid(intent: Intent?): String? {
        val data = intent?.data ?: return null
        
        // Soporta el nuevo esquema linkup://addfriend?uid=XXXX
        if (data.scheme == "linkup" && data.host == "addfriend") {
            return data.getQueryParameter("uid")
        }
        
        // Mantiene compatibilidad con el antiguo https://linkup.app/addfriend?uid=XXXX
        if (data.scheme == "https" && data.host == "linkup.app" && data.path == "/addfriend") {
            return data.getQueryParameter("uid")
        }
        
        return null
    }
}
