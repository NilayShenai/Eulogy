package com.eulogy.android.mesh

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Network mode selection
 */
enum class NetworkMode {
    EULOGY,
    BITCHAT
}

/**
 * Manages network selection preferences with persistence
 */
object NetworkPreferenceManager {
    private const val TAG = "NetworkPreferenceManager"
    private const val PREFS_NAME = "network_preferences"
    private const val KEY_NETWORK_MODE = "network_mode"
    
    private val _modeFlow = MutableStateFlow(NetworkMode.EULOGY) // Default to Eulogy
    val modeFlow: StateFlow<NetworkMode> = _modeFlow.asStateFlow()
    
    // Listener for network changes
    var onNetworkChanged: ((NetworkMode) -> Unit)? = null
    
    private fun getPreferences(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * Get the current network mode
     */
    fun get(context: Context): NetworkMode {
        val prefs = getPreferences(context)
        val modeString = prefs.getString(KEY_NETWORK_MODE, NetworkMode.EULOGY.name)
        return try {
            NetworkMode.valueOf(modeString ?: NetworkMode.EULOGY.name)
        } catch (e: IllegalArgumentException) {
            NetworkMode.EULOGY
        }
    }
    
    /**
     * Set the network mode and persist it
     */
    fun set(context: Context, mode: NetworkMode) {
        val currentMode = get(context)
        if (currentMode != mode) {
            Log.i(TAG, "Network mode changed from $currentMode to $mode")
            val prefs = getPreferences(context)
            prefs.edit().putString(KEY_NETWORK_MODE, mode.name).apply()
            _modeFlow.value = mode
            
            // Notify listeners of the change
            onNetworkChanged?.invoke(mode)
        }
    }
    
    /**
     * Initialize the flow with the saved preference
     */
    fun initialize(context: Context) {
        _modeFlow.value = get(context)
        Log.d(TAG, "Initialized with network mode: ${_modeFlow.value}")
    }
}
