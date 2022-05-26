package com.zgh.hooktest.utils

import android.os.Build
import org.lsposed.hiddenapibypass.HiddenApiBypass

/**
 *页面：
 *@author zhangguihao
 */
object HiddenApiBypassHelper {
    @Volatile
    private var inited = false

    @JvmStatic
    fun initHiddenApiBypass() {
        if (inited) {
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            HiddenApiBypass.addHiddenApiExemptions("L");
        }
        inited = true
    }
}