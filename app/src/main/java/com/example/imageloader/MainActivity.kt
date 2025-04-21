package com.example.imageloader

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle

import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowDown
// Add these imports for animations
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import com.example.imageloader.loaders.ImageAsyncTaskLoader
import com.example.imageloader.service.ImageLoaderService
import com.example.imageloader.tasks.ImageLoadAsyncTask
import com.example.imageloader.ui.theme.ImageLoaderTheme

class MainActivity : ComponentActivity() {
    private var internetConnected = mutableStateOf(true)
    private val connectivityReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateConnectivityStatus()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate called, savedInstanceState: $savedInstanceState")

        // Check initial connectivity
        updateConnectivityStatus()

        // Register for connectivity changes
        registerReceiver(
            connectivityReceiver,
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        )

        // Start the background service
        val serviceIntent = Intent(this, ImageLoaderService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)

        setContent {
            ImageLoaderTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ImageLoaderApp(internetConnected.value)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MainActivity", "onDestroy called")
        unregisterReceiver(connectivityReceiver)
    }

    private fun updateConnectivityStatus() {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.activeNetwork?.let {
            connectivityManager.getNetworkCapabilities(it)
        }
        internetConnected.value = networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }
}

@Composable
fun ImageLoaderApp(isConnected: Boolean) {
    val context = LocalContext.current
    var urlText by remember { mutableStateOf(TextFieldValue("https://picsum.photos/300")) }
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var loadingState by rememberSaveable { mutableStateOf(LoadingState.IDLE) }
    var useAsyncTaskLoader by rememberSaveable { mutableStateOf(false) }
    var currentAsyncTask by remember { mutableStateOf<ImageLoadAsyncTask?>(null) }

    // Custom colors
    val gradientBackground = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        )
    )

    val accentColor = MaterialTheme.colorScheme.primary
    val cardBgColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)

    val loaderCallback = remember {
        object : LoaderManager.LoaderCallbacks<Bitmap?> {
            override fun onCreateLoader(id: Int, args: Bundle?): Loader<Bitmap?> {
                val url = args?.getString("url") ?: ""
                return ImageAsyncTaskLoader(context, url)
            }
            override fun onLoadFinished(loader: Loader<Bitmap?>, data: Bitmap?) {
                loadingState = if (data != null) {
                    imageBitmap = data; LoadingState.SUCCESS
                } else {
                    LoadingState.ERROR
                }
            }
            override fun onLoaderReset(loader: Loader<Bitmap?>) {
                imageBitmap = null
            }
        }
    }

    LaunchedEffect(useAsyncTaskLoader) {
        if (useAsyncTaskLoader && loadingState == LoadingState.SUCCESS) {
            val args = Bundle().apply { putString("url", urlText.text) }
            LoaderManager.getInstance(context as MainActivity)
                .initLoader(0, args, loaderCallback)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Header with nice styling
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(accentColor, CircleShape)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Image Loader",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Content Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = cardBgColor)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    // Connection status - enhanced visual
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(50.dp))
                            .background(
                                if (isConnected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                            )
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = if (isConnected) Icons.Filled.CheckCircle else Icons.Filled.Close,
                            contentDescription = null,
                            tint = if (isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = if (isConnected) "Internet Connected" else "No Internet Connection",
                            color = if (isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    // URL Input with better styling
                    OutlinedTextField(
                        value = urlText,
                        onValueChange = { urlText = it },
                        label = { Text("Image URL") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        singleLine = true
                    )

                    Spacer(Modifier.height(24.dp))

                    // Loader selection with better styling
                    Text(
                        "Loading Method",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (!useAsyncTaskLoader) MaterialTheme.colorScheme.primaryContainer
                                    else Color.Transparent
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = !useAsyncTaskLoader,
                                onClick = { useAsyncTaskLoader = false },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "AsyncTask",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (!useAsyncTaskLoader)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(Modifier.width(8.dp))

                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (useAsyncTaskLoader) MaterialTheme.colorScheme.primaryContainer
                                    else Color.Transparent
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = useAsyncTaskLoader,
                                onClick = { useAsyncTaskLoader = true },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "AsyncTaskLoader",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (useAsyncTaskLoader)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Load button with fancy styling
                    Button(
                        onClick = {
                            loadingState = LoadingState.LOADING
                            imageBitmap = null
                            val url = urlText.text

                            if (useAsyncTaskLoader) {
                                val args = Bundle().apply { putString("url", url) }
                                LoaderManager.getInstance(context as MainActivity)
                                    .restartLoader(0, args, loaderCallback)
                            } else {
                                currentAsyncTask?.cancel(true)
                                val task = ImageLoadAsyncTask(context) { result ->
                                    loadingState = if (result != null) {
                                        imageBitmap = result; LoadingState.SUCCESS
                                    } else {
                                        LoadingState.ERROR
                                    }
                                }
                                currentAsyncTask = task
                                task.execute(url)
                            }
                        },
                        enabled = isConnected,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowDown,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Load Image",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Preview Section with better styling
            Text(
                text = "Image Preview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Enhanced preview card
            Card(
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = cardBgColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 4.dp, vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    when (loadingState) {
                        LoadingState.IDLE -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FavoriteBorder,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    "Enter a URL and press Load Image",
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                        LoadingState.LOADING -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(48.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 4.dp
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    "Loading image...",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                        LoadingState.SUCCESS -> {
                            imageBitmap?.let {
                                // Fixed AnimatedVisibility without relying on ColumnScope
                                androidx.compose.animation.AnimatedVisibility(
                                    visible = true,
                                    enter = fadeIn(animationSpec = tween(300)) +
                                            slideInVertically(
                                                initialOffsetY = { it / 2 },
                                                animationSpec = tween(
                                                    durationMillis = 300,
                                                    easing = LinearOutSlowInEasing
                                                )
                                            )
                                ) {
                                    Image(
                                        bitmap = it.asImageBitmap(),
                                        contentDescription = "Loaded Image",
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                        LoadingState.ERROR -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    "Failed to load image",
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Please check the URL and try again",
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

enum class LoadingState {
    IDLE, LOADING, SUCCESS, ERROR
}