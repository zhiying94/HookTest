package com.zgh.hooktest.hook

import android.content.Intent
import android.os.Handler
import android.os.Message
import android.util.Log
import com.zgh.hooktest.utils.FieldUtil.getAccessField


/**
 *页面：
 *@author zhangguihao
 */
class HCallback(val hHandler: Handler) : Handler.Callback {
    companion object {
        const val LAUNCH_ACTIVITY = 100;
        private const val EXECUTE_TRANSACTION = 159
    }

    override fun handleMessage(msg: Message): Boolean {
        if (msg.what == LAUNCH_ACTIVITY) {
            val r = msg.obj
            try {
                val intentFailed = r.javaClass.getAccessField("intent")
                intentFailed.get(r)?.let {
                    it as Intent
                }?.run {
                    getParcelableExtra<Intent?>(HookHelper.TARGET_INTENT)
                }?.let {
                    intentFailed.set(r, it)
                }
                hHandler.handleMessage(msg)
                return true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else
            if (msg.what == EXECUTE_TRANSACTION) { //这是跳转的时候,要对intent进行还原
                try {
                    val ClientTransactionClz =
                        Class.forName("android.app.servertransaction.ClientTransaction")
                    val LaunchActivityItemClz =
                        Class.forName("android.app.servertransaction.LaunchActivityItem")
                    //ClientTransaction的成员
                    val mActivityCallbacksField =
                        ClientTransactionClz.getAccessField("mActivityCallbacks")
                    //类型判定，好习惯
                    if (!ClientTransactionClz.isInstance(msg.obj)) return true
                    //根据源码，在这个分支里面,msg.obj就是 ClientTransaction类型,所以，直接用
                    val mActivityCallbacksObj: Any = mActivityCallbacksField.get(msg.obj)
                    //拿到了ClientTransaction的List<ClientTransactionItem> mActivityCallbacks;
                    val list = mActivityCallbacksObj as List<*>
                    if (list.size == 0)
                        return true
                    //所以这里直接就拿到第一个就好了
                    val LaunchActivityItemObj = list[0]!!
                    if (!LaunchActivityItemClz.isInstance(LaunchActivityItemObj)) return true
                    //这里必须判定 LaunchActivityItemClz，
                    // 因为 最初的ActivityResultItem传进去之后都被转化成了这LaunchActivityItemClz的实例
                    val mIntentField = LaunchActivityItemClz.getAccessField("mIntent")
                    mIntentField.get(LaunchActivityItemObj)?.let {
                        it as Intent
                    }?.run {
                        getParcelableExtra<Intent?>(HookHelper.TARGET_INTENT)
                    }?.let {
                        mIntentField.set(LaunchActivityItemObj, it)
                    }
                    hHandler.handleMessage(msg)
                    return true
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        return false
    }

}