package com.webviewapp

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var progressBar: TopProgressBar
    private lateinit var overlay: View
    private lateinit var spinner: IOSSpinnerView
    private lateinit var loadingText: TextView

    private val handler = Handler(Looper.getMainLooper())
    private var overlayVisible = false

    private val dotsFrames = arrayOf("", ".", "..", "...")
    private var dotsIndex = 0
    private val dotsRunnable = object : Runnable {
        override fun run() {
            loadingText.text = "加载中${dotsFrames[dotsIndex]}"
            dotsIndex = (dotsIndex + 1) % dotsFrames.size
            handler.postDelayed(this, 500)
        }
    }

    private val timeoutRunnable = Runnable { hideOverlay() }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        @Suppress("DEPRECATION")
        window.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN or
            android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN or
            android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        setContentView(R.layout.activity_main)
        webView     = findViewById(R.id.webView)
        progressBar = findViewById(R.id.progressBar)
        overlay     = findViewById(R.id.overlay)
        spinner     = findViewById(R.id.spinner)
        loadingText = findViewById(R.id.loadingText)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        swipeRefresh.setColorSchemeColors(
            android.graphics.Color.parseColor("#6366F1")
        )
        swipeRefresh.setOnRefreshListener {
            webView.reload()
        }
        showDisclaimerIfNeeded()
        showOverlay()
        setupWebView()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled                = true
            domStorageEnabled                = true
            databaseEnabled                  = true
            useWideViewPort                  = true
            loadWithOverviewMode             = true
            setSupportZoom(false)
            builtInZoomControls              = false
            displayZoomControls              = false
            mediaPlaybackRequiresUserGesture = false
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        }
        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(webView, true)
        }
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                showOverlay()
            }

            override fun onPageFinished(view: WebView, url: String) {
                swipeRefresh.isRefreshing = false
                fetchThemeColor(view)
            }

            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val url = request.url.toString()
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) } catch (e: Exception) {}
                    return true
                }
                return false
            }
            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                if (request.isForMainFrame) {
                    hideOverlay()
                    view.loadData(errorHtml(), "text/html", "UTF-8")
                }
            }
        }
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                progressBar.setProgress(newProgress)
                if (newProgress >= 75) hideOverlay()
            }
            override fun onPermissionRequest(request: PermissionRequest) {
                request.grant(request.resources)
            }
            override fun onShowFileChooser(
                webView: WebView,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: WebChromeClient.FileChooserParams
            ): Boolean {
                try {
                    startActivityForResult(fileChooserParams.createIntent(), FILE_CHOOSER_REQUEST)
                    fileChooserCallbackRef = filePathCallback
                } catch (e: Exception) {
                    filePathCallback.onReceiveValue(null)
                }
                return true
            }
        }
        webView.setDownloadListener { url, userAgent, contentDisposition, mimetype, _ ->
            try {
                val uri = Uri.parse(url)
                val filename = android.webkit.URLUtil.guessFileName(url, contentDisposition, mimetype)
                val req = DownloadManager.Request(uri).apply {
                    setMimeType(mimetype)
                    addRequestHeader("User-Agent", userAgent)
                    setDescription("正在下载...")
                    setTitle(filename)
                    allowScanningByMediaScanner()
                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)
                }
                val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                dm.enqueue(req)
                android.widget.Toast.makeText(this, "开始下载：$filename", android.widget.Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) } catch (_: Exception) {}
            }
        }
        // 键盘弹出适配：FLAG_FULLSCREEN 下 adjustResize 失效，手动监听 Insets
        val rootView = window.decorView.rootView
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(rootView) { _, insets ->
            val imeHeight = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.ime()).bottom
            val navHeight = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars()).bottom
            webView.setPadding(0, 0, 0, if (imeHeight > 0) imeHeight - navHeight else 0)
            insets
        }

        webView.addJavascriptInterface(object {
            @JavascriptInterface
            fun onThemeColor(hex: String) {
                try {
                    val color = android.graphics.Color.parseColor(hex)
                    runOnUiThread { progressBar.setBarColor(color) }
                } catch (e: Exception) {}
            }
        }, "ThemeBridge")
        // 在 UserAgent 中加入 App 标识，网页端据此跳过免责声明弹窗
        val defaultUA = webView.settings.userAgentString
        webView.settings.userAgentString = "$defaultUA PakrApp/1.0"
        webView.loadUrl(APP_URL)
    }

    private fun fetchThemeColor(view: WebView) {
        val js = """
            (function() {
                var m = document.querySelector('meta[name="theme-color"]');
                if (m && m.content) { ThemeBridge.onThemeColor(m.content); return; }
                var el = document.elementFromPoint(window.innerWidth/2, 1);
                if (el) {
                    var bg = getComputedStyle(el).backgroundColor;
                    var r = bg.match(/rgba?\((\d+),(\d+),(\d+)/);
                    if (r) ThemeBridge.onThemeColor(
                        '#' + [r[1],r[2],r[3]].map(function(x){
                            return ('0' + parseInt(x).toString(16)).slice(-2);
                        }).join('')
                    );
                }
            })();
        """.trimIndent()
        view.evaluateJavascript(js, null)
    }

    private fun showOverlay() {
        if (overlayVisible) return
        overlayVisible = true
        overlay.alpha = 1f
        overlay.visibility = View.VISIBLE
        progressBar.visibility = View.VISIBLE
        progressBar.setProgress(0)
        spinner.start()
        dotsIndex = 0
        handler.post(dotsRunnable)
        handler.removeCallbacks(timeoutRunnable)
        handler.postDelayed(timeoutRunnable, 10_000L)
    }

    private fun hideOverlay() {
        if (!overlayVisible) return
        handler.removeCallbacks(timeoutRunnable)
        handler.removeCallbacks(dotsRunnable)
        overlayVisible = false
        overlay.animate().alpha(0f).setDuration(300).withEndAction {
            overlay.visibility = View.GONE
            spinner.stop()
            progressBar.visibility = View.GONE
        }.start()
    }

    private fun errorHtml() = """
        <html><body style="margin:0;display:flex;align-items:center;justify-content:center;
        height:100vh;font-family:sans-serif;flex-direction:column;background:#fff;color:#333;">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#999" stroke-width="1.5">
          <circle cx="12" cy="12" r="10"/>
          <line x1="12" y1="8" x2="12" y2="12"/>
          <line x1="12" y1="16" x2="12.01" y2="16"/>
        </svg>
        <p style="margin-top:16px;font-size:15px;">网络连接失败</p>
        <button onclick="location.reload()"
          style="margin-top:12px;padding:10px 24px;border:none;border-radius:999px;
          background:#000;color:#fff;font-size:14px;cursor:pointer;">重试</button>
        </body></html>
    """.trimIndent()

    private var backPressedTime = 0L
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            val now = System.currentTimeMillis()
            if (now - backPressedTime < 2000) {
                @Suppress("DEPRECATION")
                super.onBackPressed()
            } else {
                backPressedTime = now
                android.widget.Toast.makeText(this, "再按一次退出", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onPause() { super.onPause(); CookieManager.getInstance().flush() }
    override fun onDestroy() { handler.removeCallbacksAndMessages(null); webView.destroy(); super.onDestroy() }

    private var fileChooserCallbackRef: ValueCallback<Array<Uri>>? = null

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == FILE_CHOOSER_REQUEST) {
            fileChooserCallbackRef?.onReceiveValue(
                if (resultCode == RESULT_OK && data != null)
                    WebChromeClient.FileChooserParams.parseResult(resultCode, data)
                else null
            )
            fileChooserCallbackRef = null
        }
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
    }


    private fun showDisclaimerIfNeeded() {
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        if (prefs.getBoolean("disc_agreed", false)) return
        val dialog = android.app.AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert)
            .setTitle("⚠️ 免责声明")
            .setMessage(
                "本应用仅供学习、研究和个人合法用途。\n\n" +
                "禁止用于：\n" +
                "❌ 制作仿冒、钓鱼或诈骗类应用\n" +
                "❌ 封装违法、赌博等违规网站\n" +
                "❌ 侵犯他人知识产权\n" +
                "❌ 任何违反法律法规的行为\n\n" +
                "使用本应用产生的一切法律责任由使用者自行承担。"
            )
            .setCancelable(false)
            .setPositiveButton("我已阅读，同意继续") { _, _ ->
                prefs.edit().putBoolean("disc_agreed", true).apply()
            }
            .setNegativeButton("不同意，退出") { _, _ ->
                finish()
            }
            .create()
        dialog.show()
    }

    companion object {
        const val APP_URL = "{{APP_URL}}"
        private const val FILE_CHOOSER_REQUEST = 1001
    }
}
