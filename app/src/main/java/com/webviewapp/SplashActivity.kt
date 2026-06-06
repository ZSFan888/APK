package com.webviewapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        @Suppress("DEPRECATION")
        window.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,
            android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_splash)

        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val agreed = prefs.getBoolean("disc_agreed", false)

        Handler(Looper.getMainLooper()).postDelayed({
            if (agreed) {
                // 已同意过，直接进主界面
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                // 首次启动，显示免责声明
                startActivity(Intent(this, DisclaimerActivity::class.java))
            }
            finish()
        }, 800)
    }
}
