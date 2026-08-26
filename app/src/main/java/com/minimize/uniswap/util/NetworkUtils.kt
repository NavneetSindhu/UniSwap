package com.minimize.uniswap.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Centralized utility for checking and observing internet connectivity.
 * Supports debug simulation of offline states for QA testing.
 */
object NetworkUtils {

    /**
     * Synchronously checks if the device has an active internet connection.
     */
    fun isInternetAvailable(context: Context): Boolean {
        if (DebugConfig.isForceOffline()) {
            return false
        }
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    /**
     * Real-time reactive flow streaming internet connectivity state (true = connected, false = disconnected).
     */
    fun observeConnectivity(context: Context): Flow<Boolean> {
        val rawNetworkFlow: Flow<Boolean> = callbackFlow {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            if (connectivityManager == null) {
                trySend(false)
                close()
                return@callbackFlow
            }

            // Send initial connectivity state immediately
            val initialActiveNetwork = connectivityManager.activeNetwork
            val initialCapabilities = connectivityManager.getNetworkCapabilities(initialActiveNetwork)
            val initialConnected = initialCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
                    initialCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            trySend(initialConnected)

            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    trySend(true)
                }

                override fun onLost(network: Network) {
                    val activeNetwork = connectivityManager.activeNetwork
                    val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
                    val stillConnected = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
                            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                    trySend(stillConnected)
                }

                override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                    val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                            networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                    trySend(hasInternet)
                }

                override fun onUnavailable() {
                    trySend(false)
                }
            }

            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            connectivityManager.registerNetworkCallback(request, callback)

            awaitClose {
                try {
                    connectivityManager.unregisterNetworkCallback(callback)
                } catch (_: Exception) {
                    // Safely ignore unregister callback failures
                }
            }
        }

        return combine(rawNetworkFlow, DebugConfig.forceOfflineMode) { isConnected, forceOffline ->
            if (DebugConfig.isDebug() && forceOffline) false else isConnected
        }.distinctUntilChanged()
    }
}
