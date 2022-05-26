package com.zgh.hooktest.hook;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.zgh.hooktest.StubActivity;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * 页面：
 * @author zhangguihao
 */
class IActivityManagerProxy2 implements InvocationHandler {
    private static final String TAG = "IActivityManagerProxy2";
    private Object amp;

    public IActivityManagerProxy2(Object amp) {
        this.amp = amp;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Log.d(TAG, "invoke: method.name=" + method.getName());
        if ("startActivity".equals(method.getName())) {

            Intent origIntent = null;
            int index = -1;
            for (int i = 0; i < args.length; i++) {
                Object obj = args[i];
                if (obj instanceof Intent) {
                    origIntent = (Intent) obj;
                    index = i;
                    break;
                }
            }
            if (origIntent != null) {
                Intent subIntent = new Intent();
                subIntent.setClassName("com.zgh.hooktest", StubActivity.class.getName());
                subIntent.putExtra(HookHelper.TARGET_INTENT, origIntent);
                args[index] = subIntent;
            }
        }
        return method.invoke(amp, args);
    }
}
