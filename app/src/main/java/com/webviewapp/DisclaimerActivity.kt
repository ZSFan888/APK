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

        val tvBody = findViewById<TextView>(R.id.tvDisclaimerBody)
        tvBody.text = buildString {
            appendLine("本应用仅供技术学习、研究及个人合法用途使用。")
            appendLine()
            appendLine("【严禁用途】")
            appendLine()
            appendLine("× 实施网络诈骗、钓鱼攻击")
            appendLine("× 冒充银行、支付平台或政府机构")
            appendLine("× 传播违法、赌博、色情或有害内容")
            appendLine("× 侵犯他人知识产权、商标权或隐私权")
            appendLine("× 任何违反中华人民共和国及所在地法律法规的行为")
            appendLine()
            appendLine("【法律责任声明】")
            appendLine()
            appendLine("本应用的内容及用途由使用者自行决定，由此产生的一切法律责任均由使用者自行承担，开发者不承担连带责任。")
            appendLine()
            appendLine("【警示】")
            appendLine()
            append("网络并非法外之地，利用技术工具实施诈骗属于刑事犯罪，将面临刑事追诉。请勿以身试法。")
        }

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
