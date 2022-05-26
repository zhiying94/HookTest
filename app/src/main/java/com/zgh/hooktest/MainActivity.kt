package com.zgh.hooktest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.zgh.hooktest.hook.HookHelper
import com.zgh.hooktest.hook.ProxyService
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.btnHook).setOnClickListener {

        }
        findViewById<Button>(R.id.btnJumpTarget).setOnClickListener {
            startActivity(Intent(this, TargetActivity::class.java))
        }
        findViewById<Button>(R.id.btnStartTargetService).setOnClickListener {
            startService(Intent(this, TargetService::class.java))
        }
    }
}