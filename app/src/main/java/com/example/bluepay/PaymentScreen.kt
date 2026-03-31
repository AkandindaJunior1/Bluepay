package com.example.bluepay

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.bluepay.data.AppDatabase
import com.example.bluepay.data.TransactionEntity
import com.example.bluepay.security.BiometricHelper
import com.example.bluepay.security.SecurityManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
@Composable
fun PaymentScreen(
    notificationHelper: NotificationHelper,
    biometricHelper: BiometricHelper,
    securityManager: SecurityManager,
    database: AppDatabase
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var amountInput by remember { mutableStateOf("50") }
    var paymentAmount by remember { mutableFloatStateOf(50f) }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var showReceipt by remember { mutableStateOf(false) }
    
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> hasLocationPermission = isGranted }
    )

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> hasNotificationPermission = isGranted }
    )

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) locationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let { userLocation = LatLng(it.latitude, it.longitude) }
            }
        }
    }

    LaunchedEffect(amountInput) {
        val newAmount = amountInput.toFloatOrNull() ?: 0f
        if (newAmount in 0f..1000f) {
            paymentAmount = newAmount
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AttachMoney, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                            Text("Transaction Amount", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelLarge)
                        }
                        
                        BasicTextField(
                            value = amountInput,
                            onValueChange = { newValue ->
                                if (newValue.isEmpty() || (newValue.toFloatOrNull() != null && newValue.toFloat() <= 1000)) {
                                    amountInput = newValue 
                                }
                            },
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            decorationBox = { innerTextField ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("$", color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Bold)
                                    innerTextField()
                                }
                            }
                        )

                        Slider(
                            value = paymentAmount,
                            onValueChange = { newValue ->
                                paymentAmount = newValue 
                                amountInput = newValue.toInt().toString()
                            },
                            valueRange = 0f..1000f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color.White,
                                activeTrackColor = Color.White,
                                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Secure Payment Location", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (userLocation != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "GPS FIXED",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.LightGray.copy(alpha = 0.2f))
                ) {
                    if (hasLocationPermission) {
                        AndroidView(
                            factory = { ctx ->
                                MapView(ctx).apply {
                                    onCreate(null)
                                    onResume()
                                    getMapAsync { googleMap ->
                                        googleMap.uiSettings.isMyLocationButtonEnabled = true
                                        googleMap.isMyLocationEnabled = true
                                        updateMap(googleMap, userLocation)
                                    }
                                }
                            },
                            update = { mapView ->
                                mapView.getMapAsync { googleMap ->
                                    updateMap(googleMap, userLocation)
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    
                    if (userLocation == null) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        }
                    }
                }
                
                if (userLocation != null) {
                    Text(
                        text = "Lat: ${"%.4f".format(userLocation!!.latitude)}  |  Lng: ${"%.4f".format(userLocation!!.longitude)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { 
                    val signature = securityManager.getSignatureInstance()
                    biometricHelper.showBiometricPromptForSigning(
                        title = "Authorize Payment",
                        subtitle = "Confirm $${paymentAmount.toInt()} transaction",
                        cryptoObject = BiometricPrompt.CryptoObject(signature),
                        onSuccess = { result ->
                            isProcessing = true
                            scope.launch {
                                delay(2000)
                                val finalSignature = securityManager.signData(
                                    data = "${paymentAmount.toInt()}-${System.currentTimeMillis()}",
                                    signature = result.cryptoObject?.signature!!
                                )
                                val transaction = TransactionEntity(
                                    id = (1000..9999).random().toString(),
                                    amount = paymentAmount.toInt().toString(),
                                    lat = userLocation?.latitude ?: 0.0,
                                    lng = userLocation?.longitude ?: 0.0,
                                    timestamp = System.currentTimeMillis(),
                                    signature = finalSignature
                                )
                                database.transactionDao().insertTransaction(transaction)
                                isProcessing = false
                                showReceipt = true
                                notificationHelper.sendPaymentNotification(paymentAmount.toInt().toString())
                            }
                        },
                        onError = { _, _ -> /* Handle error */ }
                    )
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("INITIATE OFFLINE PAYMENT", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (isProcessing) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.padding(32.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Signing Transaction...", fontWeight = FontWeight.Bold)
                        Text("Generating Secure Token", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        if (showReceipt) {
            AlertDialog(
                onDismissRequest = { showReceipt = false },
                confirmButton = {
                    Button(onClick = { showReceipt = false }) { Text("Done") }
                },
                title = { Text("Payment Confirmed", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("The offline payment was successfully logged to your secure hardware storage.")
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Amount: $${paymentAmount.toInt()}", fontWeight = FontWeight.Bold)
                    }
                },
                shape = RoundedCornerShape(24.dp)
            )
        }
    }
}

private fun updateMap(googleMap: GoogleMap, location: LatLng?) {
    location?.let {
        googleMap.clear()
        googleMap.addMarker(MarkerOptions().position(it).title("Verified Payment Node"))
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(it, 16f))
        googleMap.uiSettings.setAllGesturesEnabled(true)
    }
}
