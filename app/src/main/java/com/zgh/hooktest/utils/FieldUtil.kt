package com.zgh.hooktest.utils

import java.lang.reflect.Field
import java.lang.reflect.Method
import kotlin.jvm.Throws

/**
 *页面：
 *@author zhangguihao
 */
object FieldUtil {
    @Throws(Exception::class)
    fun Class<*>.getAccessField(target: Any?, name: String): Any? {
        return this.getAccessField(name).get(target)
    }

    fun Class<*>.getAccessField(name: String): Field {
        val field = getDeclaredField(name)
        field.isAccessible = true
        return field
    }

    fun Class<*>.getAccessMethod(name: String): Method {
        val field = getDeclaredMethod(name)
        field.isAccessible = true
        return field
    }

    fun Class<*>.setAccessField(target: Any, name: String, value: Any) {
        getAccessField(name).set(target, value)
    }

    fun Class<*>.invokeAccessMethod(target: Any, name: String, vararg value: Any?) {
//        if (value.isNotEmpty() ==true){
            getAccessMethod(name).invoke(target,* value)
//        }else{
//            getAccessMethod(name).invoke(target)
//        }
    }

}