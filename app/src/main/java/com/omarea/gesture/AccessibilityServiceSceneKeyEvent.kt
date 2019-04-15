package com.omarea.gesture

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.content.res.Configuration
import android.view.accessibility.AccessibilityEvent

/**
 * Created by helloklf on 2016/8/27.
 */
class AccessibilityServiceSceneKeyEvent : AccessibilityService() {
    override fun onInterrupt() {
    }
    private var floatVitualTouchBar: FloatVitualTouchBar? = null

    private fun hidePopupWindow() {
        if (floatVitualTouchBar != null) {
            floatVitualTouchBar!!.hidePopupWindow()
            floatVitualTouchBar = null
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        createPopupView()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        hidePopupWindow()
        return super.onUnbind(intent)
    }

    var isLandscapf = false

    // 监测屏幕旋转
    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        if (floatVitualTouchBar != null && newConfig != null) {
            if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                isLandscapf = false
            } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                isLandscapf = true
            }
            createPopupView()
        }
    }

    private fun createPopupView() {
        hidePopupWindow()
        floatVitualTouchBar = FloatVitualTouchBar(
                this,
                isLandscapf,
                true
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        if (floatVitualTouchBar != null) {
            floatVitualTouchBar!!.hidePopupWindow()
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
    }
}