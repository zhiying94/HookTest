package com.zgh.hooktest.hook

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import androidx.fragment.app.Fragment
import com.zgh.hooktest.StubActivity
import java.lang.Exception
import kotlin.jvm.Throws

/**
 *页面：
 *@author zhangguihao
 */
@SuppressLint("DiscouragedPrivateApi")
class InstrumentationProxy(
    val instrumentation: Instrumentation,
    val packageManager: PackageManager
) : Instrumentation() {
    private val execStartActivityMethod by lazy {
        Instrumentation::class.java.getDeclaredMethod(
            "execStartActivity",
            Context::class.java,
            IBinder::class.java,
            IBinder::class.java,
            Activity::class.java,
            Intent::class.java,
            Int::class.javaPrimitiveType,
            Bundle::class.java
        ).apply {
            isAccessible = true
        }
    }

    fun execStartActivity(
        who: Context, contextThread: IBinder?, token: IBinder?, target: Activity?,
        intent: Intent, requestCode: Int, options: Bundle?
    ): ActivityResult? {
        val infos = packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        if (infos.isNullOrEmpty()) {
            intent.putExtra(HookHelper.TARGET_INTENT_NAME, intent.component?.className)
            intent.setClassName(who, StubActivity::class.java.name)
        }
        try {
            return execStartActivityMethod.invoke(
                instrumentation,
                who,
                contextThread,
                token,
                target,
                intent,
                requestCode,
                options
            ) as ActivityResult
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    @Throws(
        InstantiationException::class,
        IllegalAccessException::class,
        ClassNotFoundException::class
    )
    override fun newActivity(cl: ClassLoader, className: String, intent: Intent): Activity {
        val intentName = intent.getStringExtra(HookHelper.TARGET_INTENT_NAME)
        if (intentName.isNullOrBlank().not()) {
            return super.newActivity(cl, intentName, intent)
        }
        return super.newActivity(cl, className, intent)
    }

}