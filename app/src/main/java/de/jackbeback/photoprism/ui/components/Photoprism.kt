package de.jackbeback.photoprism.ui.components

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.webkit.URLUtil
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import de.jackbeback.photoprism.viewmodel.DownloadState
import de.jackbeback.photoprism.viewmodel.DownloadViewModel

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun Photoprism() {
    val downloadViewModel = remember {
        DownloadViewModel.instance
    }
    // Declare a string that contains a url
    val mUrl = "http://192.168.191.220:2342"

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(factory = {
            WebView(it).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                webViewClient = WebViewClient()

                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

                // Enable zoom controls
                settings.setSupportZoom(true)
                settings.builtInZoomControls = true
                settings.displayZoomControls = false

                // Set the User-Agent to mimic Chrome (optional)
                settings.userAgentString = settings.userAgentString.replace("wv", "")

                // Disable hover events to prevent the crash
                setOnHoverListener { _, _ -> true }

                // Set DownloadListener to handle file downloads
                setDownloadListener { url, userAgent, contentDisposition, mimeType, contentLength ->
                    val urlSplit = url.split("/")
                    val request = DownloadManager.Request(Uri.parse(url)).apply {
                        setMimeType(mimeType)
                        addRequestHeader("User-Agent", userAgent)
                        setDescription("Downloading ${urlSplit[5]}...")
                        setTitle("Photoprism ${urlSplit[5]}")
                        setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        setDestinationInExternalPublicDir(Environment.DIRECTORY_DCIM, URLUtil.guessFileName(url, contentDisposition, mimeType))
                    }

                    val downloadManager = it.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    val downloadId = downloadManager.enqueue(request)

                    downloadViewModel.updateState(DownloadState.DOWNLOADING)

                    // Register a BroadcastReceiver to listen for the download completion
                    val onComplete = object : BroadcastReceiver() {
                        override fun onReceive(context: Context?, intent: Intent?) {
                            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                            if (id == downloadId) {
                                downloadViewModel.updateState(DownloadState.SUCCESS)
                            }
                        }
                    }

                    // Register the receiver to listen to the DOWNLOAD_COMPLETE action
                    it.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                        Context.RECEIVER_EXPORTED)

                    // Track download progress using a Handler
                    val handler = Handler(Looper.getMainLooper())
                    handler.post(object : Runnable {
                        override fun run() {
                            val query = DownloadManager.Query().setFilterById(downloadId)
                            val cursor = downloadManager.query(query)

                            if (cursor != null && cursor.moveToFirst()) {
                                val bytesDownloaded = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                                val totalBytes = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))

                                if (totalBytes > 0) {
                                    val progress = (bytesDownloaded * 100L / totalBytes).toInt()
                                    downloadViewModel.updateProgress(progress)
                                }

                                // Check if download is complete
                                val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                                if (status == DownloadManager.STATUS_SUCCESSFUL || status == DownloadManager.STATUS_FAILED) {
                                    cursor.close()
                                    when(status){
                                        DownloadManager.STATUS_SUCCESSFUL -> {
                                            downloadViewModel.updateState(DownloadState.SUCCESS)
                                        }
                                        DownloadManager.STATUS_FAILED -> {
                                            downloadViewModel.updateState(DownloadState.ERROR)
                                        }
                                    }
                                    return
                                }
                            }
                            cursor?.close()
                            handler.postDelayed(this, 500) // Repeat every 500 milliseconds
                        }
                    })
                }

            }
        }, modifier = Modifier.fillMaxSize(), update = {
            it.loadUrl(mUrl)
        })
    }
}