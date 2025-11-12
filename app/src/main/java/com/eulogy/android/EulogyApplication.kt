package com.eulogy.android

import android.app.Application
import com.eulogy.android.nostr.RelayDirectory
import com.eulogy.android.net.TorManager

/**
 * Main application class for Eulogy Android
 */
class EulogyApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Tor first so any early network goes over Tor
        try { TorManager.init(this) } catch (_: Exception) { }

        // Initialize relay directory (loads assets/nostr_relays.csv)
        RelayDirectory.initialize(this)

        // Initialize LocationNotesManager dependencies early so sheet subscriptions can start immediately
        try { com.eulogy.android.nostr.LocationNotesInitializer.initialize(this) } catch (_: Exception) { }

        // Initialize favorites persistence early so MessageRouter/NostrTransport can use it on startup
        try {
            com.eulogy.android.favorites.FavoritesPersistenceService.initialize(this)
        } catch (_: Exception) { }

        // Warm up Nostr identity to ensure npub is available for favorite notifications
        try {
            com.eulogy.android.nostr.NostrIdentityBridge.getCurrentNostrIdentity(this)
        } catch (_: Exception) { }

        // Initialize debug preference manager (persists debug toggles)
        try { com.eulogy.android.ui.debug.DebugPreferenceManager.init(this) } catch (_: Exception) { }

        // TorManager already initialized above
    }
}
