package com.example.myapplication.utils

import android.net.Uri

object InviteUtils {
    private const val BASE_URL = "https://linkup.app/addfriend"

    fun createFriendLink(uid: String): String {
        return "$BASE_URL?uid=$uid"
    }

    fun extractUidFromLink(link: String): String? {
        return try {
            val uri = Uri.parse(link)
            if (uri.path == "/addfriend") {
                uri.getQueryParameter("uid")
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
