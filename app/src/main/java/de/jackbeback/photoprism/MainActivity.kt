package de.jackbeback.photoprism

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import de.jackbeback.photoprism.ui.components.Photoprism
import de.jackbeback.photoprism.ui.theme.PhotoprismTheme
import de.jackbeback.photoprism.viewmodel.DownloadState
import de.jackbeback.photoprism.viewmodel.DownloadViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val downloadViewModel = DownloadViewModel.instance
            PhotoprismTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val downloadState by downloadViewModel.downloadState.collectAsState()
                    val downloadProgress by downloadViewModel.downloadProgress.collectAsState()
                    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                    val scope = rememberCoroutineScope()


                    ModalNavigationDrawer(
                        drawerContent = {
                            ModalDrawerSheet {
                                Text("Drawer title", modifier = Modifier.padding(16.dp))
                                Divider()
                                NavigationDrawerItem(
                                    label = { Text(text = "Drawer Item") },
                                    selected = false,
                                    onClick = { /*TODO*/ }
                                )
                            }
                        },
                        drawerState = drawerState,
                        gesturesEnabled = false
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Spacer(modifier = Modifier.height(innerPadding.calculateTopPadding()))
                                Photoprism()  // Your WebView composable
                            }

                            // Download Progress Indicator
                            if (downloadState == DownloadState.DOWNLOADING) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopCenter)
                                        .fillMaxWidth()
                                        .height(innerPadding.calculateTopPadding())
                                ) {
                                    LinearProgressIndicator(
                                        progress = { downloadProgress / 100F }, // Ensure downloadProgress is provided correctly
                                        modifier = Modifier
                                            .height(innerPadding.calculateTopPadding())
                                            .fillMaxWidth(),
                                        color = Color.Green
                                    )
                                    Text(
                                        text = "$downloadProgress%",
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }

                            }
                        }
                    }
                }
            }
        }
    }
}