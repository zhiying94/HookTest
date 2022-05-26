package com.zgh.hooktest.hook

import android.app.Application
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.IInterface
import android.util.Log
import com.zgh.hooktest.utils.FieldUtil.getAccessField
import com.zgh.hooktest.utils.FieldUtil.getAccessMethod
import java.lang.Exception

/**
 *页面：
 *@author zhangguihao
 */
class ProxyService : Service() {
    companion object {
        const val TARGET_SERVICE = "target_service"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("TAG", "onCreate: ")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.hasExtra(TARGET_SERVICE) != true) {
            return START_STICKY
        }
        val serviceName = intent.getStringExtra(TARGET_SERVICE)
        if (serviceName.isNullOrBlank()) {
            return START_STICKY;
        }
        try {
            val activityThread = HookHelper.getActivityThread()
            val activityThreadClazz = HookHelper.activityThreadClass
            val getActivityThreadMethod =
                activityThreadClazz.getAccessMethod("getApplicationThread")
            val applicationThread = getActivityThreadMethod.invoke(activityThread)
            val token = (applicationThread as IInterface).asBinder()
            val serviceClazz = Service::class.java
            val attachMethod = serviceClazz.getDeclaredMethod("attach",
                Context::class.java,
                activityThreadClazz,
                String::class.java,
                IBinder::class.java,
                Application::class.java,
                Object::class.java
            ).apply {
                isAccessible = true
            }
            val mInstance = HookHelper.getAMSInstance()
            val iActivityManager = HookHelper.getActivityManager(mInstance)
            val targetService = Class.forName(serviceName).newInstance() as Service
            attachMethod.invoke(
                targetService,
                this,
                activityThread,
                intent.component?.className,
                token,
                application,
                iActivityManager
            )
            targetService.onCreate()
            targetService.onStartCommand(intent, flags, startId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}