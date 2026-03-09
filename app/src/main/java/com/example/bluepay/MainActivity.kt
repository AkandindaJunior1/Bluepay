package com.example.bluepay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.bluepay.ui.theme.BluePayTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BluePayTheme {
                val context = LocalContext.current
                
                // Trackers for Week 2 Assignments
                val networkTracker = remember { NetworkStatusTracker(context) }
                val networkStatus by networkTracker.networkStatus.collectAsState(initial = NetworkStatus.Unavailable)
                
                val airplaneModeTracker = remember { AirplaneModeReceiver(context) }
                val isAirplaneModeOn by airplaneModeTracker.airplaneModeFlow().collectAsState(initial = false)

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        StatusBanner(
                            networkStatus = networkStatus,
                            isAirplaneModeOn = isAirplaneModeOn
                        )
                        HardwareScreen(context = this@MainActivity)
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBanner(networkStatus: NetworkStatus, isAirplaneModeOn: Boolean) {
    val bannerColor = when {
        isAirplaneModeOn -> Color(0xFF9E9E9E) // Grey for Airplane Mode
        networkStatus == NetworkStatus.Available -> Color(0xFF4CAF50) // Green for Online
        else -> Color(0xFFFF9800) // Orange for Offline
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
