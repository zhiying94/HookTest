package com.zgh.hooktest.hook;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;

import com.zgh.hooktest.StubActivity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 页面：
 * @author zhangguihao
 */
public class InstrumentationProxyProxy2 extends Instrumentation {
    private Instrumentation instrumentation;
    private PackageManager packageManager;
    private Method execStartActivityMethod;

    private Method getExecStartActivityMethod() throws NoSuchMethodException {
        if (execStartActivityMethod == null) {
            execStartActivityMethod = Instrumentation.class.getDeclaredMethod(
                    "execStartActivity",
                    Context.class,
                    IBinder.class,
                    IBinder.class,
                    Activity.class,
                    Intent.class,
                    int.class,
                    Bundle.class
            );
            execStartActivityMethod.setAccessible(true);
        }
        return execStartActivityMethod;
    }

    public InstrumentationProxyProxy2(Instrumentation instrumentation, PackageManager packageManager) {
        this.instrumentation = instrumentation;
        this.packageManager = packageManager;
    }

    public ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, Activity target,
            Intent intent, int requestCode, Bundle options) throws InvocationTargetException, IllegalAccessException {
        List<ResolveInfo> infos = packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL);
        if (infos == null || infos.isEmpty()) {
            intent.putExtra(HookHelper.TARGET_INTENT_NAME, intent.getComponent().getClassName());
            intent.setClassName(who, StubActivity.class.getName());
        }
        try {
            return (ActivityResult) execStartActivityMethod.invoke(
                    instrumentation,
                    who,
                    contextThread,
                    token,
                    target,
                    intent,
                    requestCode,
                    options
            );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Activity newActivity(ClassLoader cl, String className, Intent intent) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        String intentName = intent.getStringExtra(HookHelper.TARGET_INTENT_NAME);
        if (intentName != null && !TextUtils.isEmpty(intentName)) {
            return super.newActivity(cl, intentName, intent);
        }
        return super.newActivity(cl, className, intent);
    }
}
