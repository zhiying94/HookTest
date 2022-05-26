package com.zgh.hooktest

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

/**
 *页面：
 *@author zhangguihao
 */
class TargetService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("TAG", "onCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("TAG", "onStartCommand")
        return super.onStartCommand(intent, flags, startId)
    }
}