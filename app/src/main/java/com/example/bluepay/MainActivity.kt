package com.example.bluepay

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.bluepay.data.AppDatabase
import com.example.bluepay.security.BiometricHelper
import com.example.bluepay.security.SecurityManager
import com.example.bluepay.ui.theme.BluePayTheme

// Changed to AppCompatActivity to support BiometricPrompt
class MainActivity : AppCompatActivity() {
    private lateinit var notificationHelper: NotificationHelper
    private val selectedTabState = mutableIntStateOf(0)
    
    // New managers for Assignment Questions
    private lateinit var securityManager: SecurityManager
    private lateinit var biometricHelper: BiometricHelper
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notificationHelper = NotificationHelper(this)
        securityManager = SecurityManager()
        biometricHelper = BiometricHelper(this)
        database = AppDatabase.getDatabase(this)
        
        handleIntent(intent)
        enableEdgeToEdge()
        
        setContent {
            BluePayTheme {
                val context = LocalContext.current
                var selectedTab by remember { selectedTabState }
                
                val networkTracker = remember { NetworkStatusTracker(context) }
                val networkStatus by networkTracker.networkStatus.collectAsState(initial = NetworkStatus.Unavailable)
                
                val airplaneModeTracker = remember { AirplaneModeReceiver(context) }
                val isAirplaneModeOn by airplaneModeTracker.airplaneModeFlow().collectAsState(initial = false)

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                                label = { Text("Payment") },
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.History, contentDescription = null) },
                                label = { Text("History") },
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Info, contentDescription = null) },
                                label = { Text("Hardware") },
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 }
                            )
                        }
                    }
                ) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        StatusBanner(
                            networkStatus = networkStatus,
                            isAirplaneModeOn = isAirplaneModeOn
                        )
                        
                        when (selectedTab) {
                            0 -> PaymentScreen(
                                notificationHelper = notificationHelper,
                                biometricHelper = biometricHelper,
                                securityManager = securityManager,
                                database = database
                            )
                            1 -> HistoryScreen(database)
                            else -> HardwareScreen(context = this@MainActivity)
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == "OPEN_HISTORY") {
            selectedTabState.intValue = 1
        }
    }
}

@Composable
fun StatusBanner(networkStatus: NetworkStatus, isAirplaneModeOn: Boolean) {
    val bannerColor = when {
        isAirplaneModeOn -> Color(0xFF9E9E9E)
        networkStatus == NetworkStatus.Available -> Color(0xFF4CAF50)
        else -> Color(0xFFFF9800)
    }

    val bannerText = when {
        isAirplaneModeOn -> "Airplane Mode On - BluePay Ready"
        networkStatus == NetworkStatus.Available -> "Connected to Network"
        else -> "Disconnected - BluePay Ready"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(bannerColor)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = bannerText,
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
