package com.zgh.hooktest

import android.app.Application
import android.content.Context
import com.zgh.hooktest.hook.HookHelper
import com.zgh.hooktest.utils.HiddenApiBypassHelper
import java.lang.Exception

/**
 *页面：
 *@author zhangguihao
 */
class HookApplication : Application() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        try {
            HookHelper.hook()
//            HookHelper.hookInstrumentation(base)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreate() {
        super.onCreate()
//        HiddenApiBypassHelper.initHiddenApiBypass()
    }
}