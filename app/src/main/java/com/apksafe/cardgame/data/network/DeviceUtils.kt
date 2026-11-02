package com.apksafe.cardgame.data.network

import android.content.Context
import java.util.UUID

object DeviceUtils {
    private const val PREFS_NAME = "device_identity_prefs"
    private const val KEY_DEVICE_ID = "unique_device_id"

    fun getThisDeviceId(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        var deviceId = prefs.getString(KEY_DEVICE_ID, null)
        if (deviceId == null) {
            deviceId = "dev_" + UUID.randomUUID().toString().take(8)
            prefs.edit().putString(KEY_DEVICE_ID, deviceId).apply()
        }
        return deviceId
    }
}
