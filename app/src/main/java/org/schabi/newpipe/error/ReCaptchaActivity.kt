//
// Copyright (C) Christian Schabesberger 2015 <chris.schabesberger@mailbox.org>
// ReCaptchaActivity.kt is part of NewPipe.
//
// NewPipe is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// NewPipe is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with NewPipe.  If not, see <http://www.gnu.org/licenses/>.
//

package org.schabi.newpipe.error

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.NavUtils
import androidx.preference.PreferenceManager
import org.schabi.newpipe.DownloaderImpl
import org.schabi.newpipe.MainActivity
import org.schabi.newpipe.R
import org.schabi.newpipe.extractor.utils.Utils

class ReCaptchaActivity : ComponentActivity() {

    private var foundCookies = ""
    private var currentUrl: String? = null

    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        // ThemeHelper.setTheme(this) // We'll rely on Compose MaterialTheme
        super.onCreate(savedInstanceState)

        onBackPressedDispatcher.addCallback(
            this,
            object : androidx.activity.OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    saveCookiesAndFinish()
                }
            }
        )
        val rawUrl = intent.getStringExtra(RECAPTCHA_URL_EXTRA)
        val url = sanitizeRecaptchaUrl(rawUrl)
        setResult(RESULT_CANCELED)

        setContent {
            MaterialTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(stringResource(id = R.string.title_activity_recaptcha)) },
                            actions = {
                                IconButton(onClick = { saveCookiesAndFinish() }) {
                                    Icon(
                                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_done),
                                        contentDescription = "Done" // Consider adding a string resource if available, or R.string.done
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                ) { innerPadding ->
                    AndroidView(
                        factory = { context ->
                            WebView(context).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                settings.javaScriptEnabled = true
                                settings.userAgentString = DownloaderImpl.USER_AGENT

                                webViewClient = object : WebViewClient() {
                                    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                                        if (MainActivity.DEBUG) {
                                            Log.d(TAG, "shouldOverrideUrlLoading: url=${request.url}")
                                        }
                                        currentUrl = request.url.toString()
                                        handleCookiesFromUrl(currentUrl)
                                        return false
                                    }

                                    override fun onPageFinished(view: WebView, url: String) {
                                        super.onPageFinished(view, url)
                                        currentUrl = url
                                        handleCookiesFromUrl(url)
                                    }
                                }

                                clearCache(true)
                                clearHistory()
                                CookieManager.getInstance().removeAllCookies(null)

                                loadUrl(url)
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }

    private fun saveCookiesAndFinish() {
        handleCookiesFromUrl(currentUrl)

        if (MainActivity.DEBUG) {
            Log.d(TAG, "saveCookiesAndFinish: foundCookies=$foundCookies")
        }

        if (foundCookies.isNotEmpty()) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            val key = applicationContext.getString(R.string.recaptcha_cookies_key)
            prefs.edit().putString(key, foundCookies).apply()

            DownloaderImpl.getInstance().setCookie(RECAPTCHA_COOKIES_KEY, foundCookies)
            setResult(RESULT_OK)
        }

        // Webview destroyed with activity

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        NavUtils.navigateUpTo(this, intent)
    }

    private fun handleCookiesFromUrl(url: String?) {
        if (MainActivity.DEBUG) {
            Log.d(TAG, "handleCookiesFromUrl: url=${url ?: "null"}")
        }

        if (url == null) return

        val cookies = CookieManager.getInstance().getCookie(url)
        handleCookies(cookies)

        val abuseStart = url.indexOf("google_abuse=")
        if (abuseStart != -1) {
            val abuseEnd = url.indexOf("+path")
            try {
                handleCookies(Utils.decodeUrlUtf8(url.substring(abuseStart + 13, abuseEnd)))
            } catch (e: StringIndexOutOfBoundsException) {
                if (MainActivity.DEBUG) {
                    Log.e(TAG, "handleCookiesFromUrl: invalid google abuse starting at $abuseStart and ending at $abuseEnd for url $url", e)
                }
            }
        }
    }

    private fun handleCookies(cookies: String?) {
        if (MainActivity.DEBUG) {
            Log.d(TAG, "handleCookies: cookies=${cookies ?: "null"}")
        }

        if (cookies == null) return
        addYoutubeCookies(cookies)
    }

    private fun addYoutubeCookies(cookies: String) {
        if (cookies.contains("s_gl=") || cookies.contains("goojf=") ||
            cookies.contains("VISITOR_INFO1_LIVE=") ||
            cookies.contains("GOOGLE_ABUSE_EXEMPTION=")
        ) {
            addCookie(cookies)
        }
    }

    private fun addCookie(cookie: String) {
        if (foundCookies.contains(cookie)) return

        foundCookies = if (foundCookies.isEmpty() || foundCookies.endsWith("; ")) {
            foundCookies + cookie
        } else if (foundCookies.endsWith(";")) {
            foundCookies + " " + cookie
        } else {
            "$foundCookies; $cookie"
        }
    }

    companion object {
        const val RECAPTCHA_REQUEST = 10
        const val RECAPTCHA_URL_EXTRA = "recaptcha_url_extra"
        val TAG: String = ReCaptchaActivity::class.java.simpleName
        const val YT_URL = "https://www.youtube.com"
        const val RECAPTCHA_COOKIES_KEY = "recaptcha_cookies"

        @JvmStatic
        fun sanitizeRecaptchaUrl(url: String?): String {
            return if (url.isNullOrBlank()) {
                YT_URL
            } else {
                url.replace("&pbj=1", "")
                    .replace("pbj=1&", "")
                    .replace("?pbj=1", "")
            }
        }
    }
}
