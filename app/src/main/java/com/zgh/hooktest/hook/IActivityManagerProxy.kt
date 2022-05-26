package com.zgh.hooktest.hook

import android.content.Intent
import android.util.Log
import com.zgh.hooktest.StubActivity
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

/**
 *页面：
 *@author zhangguihao
 */
class IActivityManagerProxy(val activityManager: Any) : InvocationHandler {
    companion object {

    }

    @Throws(Throwable::class)
    override fun invoke(proxy: Any, method: Method, args: Array<Any>?): Any? {
        if (args.isNullOrEmpty()) {
            return method.invoke(activityManager)
        }
        if ("startActivity" == method.name) {
            for ((index, obj) in args.withIndex()) {
                Log.d("withIndex", "it=$obj")
                if (obj is Intent) {
                    val subIntent = Intent()
                    subIntent.setClassName("com.zgh.hooktest", StubActivity::class.java.name)
                    subIntent.putExtra(HookHelper.TARGET_INTENT, obj)
                    args[index] = subIntent
                    break
                }
            }
        } else if ("startService" == method.name) {
            for ((index, obj) in args.withIndex()) {
                Log.d("withIndex", "it=$obj")
                if (obj is Intent) {
                    obj.putExtra(ProxyService.TARGET_SERVICE, obj.component?.className)
                    obj.setClassName("com.zgh.hooktest", ProxyService::class.java.name)
//                    args[index] = subIntent
                    break
                }
            }
        }
        return method.invoke(activityManager, * args)
    }
}