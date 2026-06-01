package com.example.myapplication.utils

import android.app.Activity
import android.nfc.NfcAdapter

class NFCManager(private val activity: Activity) {
    private val nfcAdapter: NfcAdapter? by lazy {
        NfcAdapter.getDefaultAdapter(activity)
    }

    fun isAvailable(): Boolean {
        return nfcAdapter != null
    }

    fun isEnabled(): Boolean {
        return nfcAdapter?.isEnabled == true
    }
}
