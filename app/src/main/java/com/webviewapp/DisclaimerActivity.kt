package com.webviewapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DisclaimerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_disclaimer)

        // 将 strings.xml 里的 \n 转为真正的换行符
        val tvBody = findViewById<TextView>(R.id.tvDisclaimerBody)
        tvBody.text = getString(R.string.disclaimer_body).replace("\\n", "\n")

        val btnDecline = findViewById<Button>(R.id.btnDecline)
        val btnAccept  = findViewById<Button>(R.id.btnAccept)

        btnDecline.setOnClickListener { finishAffinity() }
        btnAccept.setOnClickListener { proceed() }
    }

    private fun proceed() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}
