package com.example.bluepay

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun HardwareScreen(context: Context) {
    val pm = context.packageManager
    
    // Essential Payment Hardware Checks
    val hasNfc = pm.hasSystemFeature(PackageManager.FEATURE_NFC)
    val hasFingerprint = pm.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)
    val hasStrongBox = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        pm.hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE)
    } else false

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Security & Payment Hardware",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Device Identity Card
        HardwareItem(label = "Manufacturer", value = Build.MANUFACTURER)
        HardwareItem(label = "Model", value = Build.MODEL)
        HardwareItem(label = "Android Version", value = Build.VERSION.RELEASE)
        
        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        Text(
            text = "Capabilities",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Feature Status
        FeatureStatusRow(label = "NFC (Tap-to-Pay)", isAvailable = hasNfc)
        FeatureStatusRow(label = "Biometric Security", isAvailable = hasFingerprint)
        FeatureStatusRow(label = "Hardware StrongBox", isAvailable = hasStrongBox)
    }
}

@Composable
fun HardwareItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.Gray)
        Text(text = value, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun FeatureStatusRow(label: String, isAvailable: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isAvailable) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isAvailable) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (isAvailable) Color(0xFF2E7D32) else Color(0xFFC62828)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                fontWeight = FontWeight.SemiBold,
                color = if (isAvailable) Color(0xFF2E7D32) else Color(0xFFC62828)
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = if (isAvailable) "READY" else "MISSING",
                style = MaterialTheme.typography.labelSmall,
                color = if (isAvailable) Color(0xFF2E7D32) else Color(0xFFC62828)
            )
        }
    }
}
