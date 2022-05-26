package com.zgh.hooktest.hook

import android.annotation.SuppressLint
import android.app.Instrumentation
import android.content.Context
import android.os.Build
import android.os.Handler
import com.zgh.hooktest.utils.FieldUtil.getAccessField
import com.zgh.hooktest.utils.FieldUtil.invokeAccessMethod
import com.zgh.hooktest.utils.FieldUtil.setAccessField
import java.lang.reflect.Field
import java.lang.reflect.Proxy

/**
 *页面：
 *@author zhangguihao
 */
object HookHelper {
    const val TARGET_INTENT = "target_intent"
    const val TARGET_INTENT_NAME = "target_intent_name"
    val activityThreadClass by lazy { Class.forName("android.app.ActivityThread") }

    fun hook() {
        hookActivity()
        hookService()
    }


    @SuppressLint("PrivateApi")
    fun hookActivity() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            hookATM()
        } else {
            hookAms()
        }
        hookHandler()
    }

    @SuppressLint("PrivateApi")
    fun hookService() {
        hookAms()
    }

    var isHookedAms = false

    @Synchronized
    @SuppressLint("PrivateApi")
    fun hookAms() {
        if (isHookedAms) {
            return
        }
        val objSingleton = getAMSInstance()
        objSingleton.let {
            val mInstanceField = getActivityManagerField(it)
            val iActivityManager = getActivityManager(mInstanceField, it)
            val iActivityManagerClazz = Class.forName("android.app.IActivityManager")
            val proxy = Proxy.newProxyInstance(
                Thread.currentThread().contextClassLoader,
                arrayOf(iActivityManagerClazz),
                IActivityManagerProxy(iActivityManager)
            )
            mInstanceField.set(it, proxy)
        }
        isHookedAms = true

    }


    fun hookATM() {
        val objSingleton = getATMInstance()
        objSingleton.let {
            val mInstanceField = getActivityManagerField(it)
            val iActivityManager = getActivityManager(mInstanceField, it)
            val iActivityManagerClazz =
                Class.forName("android.app.IActivityTaskManager")
            val proxy = Proxy.newProxyInstance(
                Thread.currentThread().contextClassLoader,
                arrayOf(iActivityManagerClazz),
                IActivityManagerProxy(iActivityManager)
            )
            mInstanceField.set(it, proxy)
        }
    }

    fun getActivityManager(objSingleton: Any): Any {
        val singletonClazz = Class.forName("android.util.Singleton")
        val mInstanceField = singletonClazz.getAccessField("mInstance")
        singletonClazz.invokeAccessMethod(objSingleton, "get")
        val iActivityManager = mInstanceField.get(objSingleton)
        return iActivityManager
    }

    fun getActivityManager(mInstanceField: Field, objSingleton: Any): Any {
        val singletonClazz = Class.forName("android.util.Singleton")
        singletonClazz.invokeAccessMethod(objSingleton, "get")
        val iActivityManager = mInstanceField.get(objSingleton)
        return iActivityManager
    }

    fun getActivityManagerField(objSingleton: Any): Field {
        val singletonClazz = Class.forName("android.util.Singleton")
        val mInstanceField = singletonClazz.getAccessField("mInstance")
        singletonClazz.invokeAccessMethod(objSingleton, "get")
        return mInstanceField
    }

    @JvmStatic
    fun getMInstance(): Any {
        val objSingleton = when {
            Build.VERSION.SDK_INT > Build.VERSION_CODES.P -> {
                getATMInstance()
            }
            else -> {
                getAMSInstance()
            }
        }
        return objSingleton!!
    }

    @JvmStatic
    fun getAMSInstance(): Any {
        val objSingleton = when {
            Build.VERSION.SDK_INT > Build.VERSION_CODES.O -> {
                val activityMangerClass = Class.forName("android.app.ActivityManager")
                activityMangerClass.getAccessField(null, "IActivityManagerSingleton")
            }
            else -> {
                val activityMangerNativeClass = Class.forName("android.app.ActivityManagerNative")
                activityMangerNativeClass.getAccessField("gDefault")
            }
        }
        return objSingleton!!
    }

    @JvmStatic
    fun getATMInstance(): Any {
        val activityMangerClass = Class.forName("android.app.ActivityTaskManager")
        val objSingleton = activityMangerClass.getAccessField(null, "IActivityTaskManagerSingleton")
        return objSingleton!!
    }


    @kotlin.jvm.Throws()
    fun hookInstrumentation(context: Context) {
        val contextImplClass = Class.forName("android.app.ContextImpl")
        val mMainThreadField = contextImplClass.getAccessField("mMainThread")
        val activityThread = mMainThreadField.get(context)
        val mInstrumentationFailed = activityThreadClass.getAccessField("mInstrumentation")
        val mInstrumentation = mInstrumentationFailed.get(activityThread) as Instrumentation
        mInstrumentationFailed.set(
            activityThread,
            InstrumentationProxy(mInstrumentation, context.packageManager)
        )
    }

    @SuppressLint("PrivateApi")
    @Throws()
    fun hookHandler() {
        val activityThread = getActivityThread()
        val handlerFailed = activityThreadClass.getAccessField("mH")
        val handler = handlerFailed.get(activityThread) as Handler
        Handler::class.java.setAccessField(handler, "mCallback", HCallback(handler))

    }

    fun getActivityThread(): Any? {
        val activityThread = activityThreadClass.getAccessField(null, "sCurrentActivityThread")
        return activityThread
    }
}