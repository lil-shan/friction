package com.frictionwellbeing.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class UnlockShuffleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (
            intent.action == Intent.ACTION_USER_PRESENT &&
            AppSettings.launcherMode(context) == LauncherMode.SHUFFLE
        ) {
            AppSettings.shuffleLauncherIcons(context)
        }
    }
}
