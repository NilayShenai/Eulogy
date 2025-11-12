package com.eulogy.android.mesh

import android.util.Log
import com.eulogy.android.protocol.BitchatPacket
import com.eulogy.android.protocol.MessageType
import com.eulogy.android.protocol.MessagePadding
import com.eulogy.android.model.FragmentPayload
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages message fragmentation and reassembly - 100% iOS Compatible
 * 
 * This implementation exactly matches iOS SimplifiedBluetoothService fragmentation:
 * - Same fragment payload structure (13-byte header + data)
 * - Same MTU thresholds and fragment sizes
 * - Same reassembly logic and timeout handling
 * - Uses new FragmentPayload model for type safety
 */
class FragmentManager {
    
    companion object {
        private const val TAG = "FragmentManager"
        
        private const val FRAGMENT_SIZE_THRESHOLD = com.eulogy.android.util.AppConstants.Fragmentation.FRAGMENT_SIZE_THRESHOLD 
        private const val MAX_FRAGMENT_SIZE = com.eulogy.android.util.AppConstants.Fragmentation.MAX_FRAGMENT_SIZE        
        private const val FRAGMENT_TIMEOUT = com.eulogy.android.util.AppConstants.Fragmentation.FRAGMENT_TIMEOUT_MS     
        private const val CLEANUP_INTERVAL = com.eulogy.android.util.AppConstants.Fragmentation.CLEANUP_INTERVAL_MS     // 10 seconds cleanup check
    }
    
    
    private val incomingFragments = ConcurrentHashMap<String, MutableMap<Int, ByteArray>>()
    
    private val fragmentMetadata = ConcurrentHashMap<String, Triple<UByte, Int, Long>>() // originalType, totalFragments, timestamp
    
    // Delegate for callbacks
    var delegate: FragmentManagerDelegate? = null
    
    // Coroutines
        private var managerScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        private var cleanupJob: Job? = null
    
    init {
        startPeriodicCleanup()
    }
    
    /**
     * Create fragments from a large packet - 100% iOS Compatible
     * Matches iOS sendFragmentedPacket() implementation exactly
     */
    fun createFragments(packet: BitchatPacket): List<BitchatPacket> {
        try {
            Log.d(TAG, "🔀 Creating fragments for packet type ${packet.type}, payload: ${packet.payload.size} bytes")
        val encoded = packet.toBinaryData()
            if (encoded == null) {
                Log.e(TAG, "❌ Failed to encode packet to binary data")
                return emptyList()
            }
            Log.d(TAG, "📦 Encoded to ${encoded.size} bytes")
        
        
        val fullData = try {
                MessagePadding.unpad(encoded)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to unpad data: ${e.message}", e)
                return emptyList()
            }
            Log.d(TAG, "📏 Unpadded to ${fullData.size} bytes")
        
        
        if (fullData.size <= FRAGMENT_SIZE_THRESHOLD) {
            return listOf(packet) // No fragmentation needed
        }
        
        val fragments = mutableListOf<BitchatPacket>()
        
        
        val fragmentID = FragmentPayload.generateFragmentID()
        
        
        val fragmentChunks = stride(0, fullData.size, MAX_FRAGMENT_SIZE) { offset ->
            val endOffset = minOf(offset + MAX_FRAGMENT_SIZE, fullData.size)
            fullData.sliceArray(offset..<endOffset)
        }
        
        Log.d(TAG, "Creating ${fragmentChunks.size} fragments for ${fullData.size} byte packet (iOS compatible)")
        
        
        for (index in fragmentChunks.indices) {
            val fragmentData = fragmentChunks[index]
            
            
            val fragmentPayload = FragmentPayload(
                fragmentID = fragmentID,
                index = index,
                total = fragmentChunks.size,
                originalType = packet.type,
                data = fragmentData
            )
            
            
            val fragmentPacket = BitchatPacket(
                type = MessageType.FRAGMENT.value,
                ttl = packet.ttl,
                senderID = packet.senderID,
                recipientID = packet.recipientID,
                timestamp = packet.timestamp,
                payload = fragmentPayload.encode(),
                signature = null 
            )
            
            fragments.add(fragmentPacket)
        }
        
        Log.d(TAG, "✅ Created ${fragments.size} fragments successfully")
            return fragments
        } catch (e: Exception) {
            Log.e(TAG, "❌ Fragment creation failed: ${e.message}", e)
            Log.e(TAG, "❌ Packet type: ${packet.type}, payload: ${packet.payload.size} bytes")
            return emptyList()
        }
    }
    
    /**
     * Handle incoming fragment - 100% iOS Compatible  
     * Matches iOS handleFragment() implementation exactly
     */
    fun handleFragment(packet: BitchatPacket): BitchatPacket? {
        
        if (packet.payload.size < FragmentPayload.HEADER_SIZE) {
            Log.w(TAG, "Fragment packet too small: ${packet.payload.size}")
            return null
        }
        
        
        // This would be done at a higher level but we'll include for safety
        
        try {
            // Use FragmentPayload for type-safe decoding
            val fragmentPayload = FragmentPayload.decode(packet.payload)
            if (fragmentPayload == null || !fragmentPayload.isValid()) {
                Log.w(TAG, "Invalid fragment payload")
                return null
            }
            
            
            val fragmentIDString = fragmentPayload.getFragmentIDString()
            
            Log.d(TAG, "Received fragment ${fragmentPayload.index}/${fragmentPayload.total} for fragmentID: $fragmentIDString, originalType: ${fragmentPayload.originalType}")
            
            
            if (!incomingFragments.containsKey(fragmentIDString)) {
                incomingFragments[fragmentIDString] = mutableMapOf()
                fragmentMetadata[fragmentIDString] = Triple(
                    fragmentPayload.originalType, 
                    fragmentPayload.total, 
                    System.currentTimeMillis()
                )
            }
            
            
            incomingFragments[fragmentIDString]?.put(fragmentPayload.index, fragmentPayload.data)
            
            
            val fragmentMap = incomingFragments[fragmentIDString]
            if (fragmentMap != null && fragmentMap.size == fragmentPayload.total) {
                Log.d(TAG, "All fragments received for $fragmentIDString, reassembling...")
                
                
                val reassembledData = mutableListOf<Byte>()
                for (i in 0 until fragmentPayload.total) {
                    fragmentMap[i]?.let { data ->
                        reassembledData.addAll(data.asIterable())
                    }
                }
                
                
                val originalPacket = BitchatPacket.fromBinaryData(reassembledData.toByteArray())
                if (originalPacket != null) {
                    
                    incomingFragments.remove(fragmentIDString)
                    fragmentMetadata.remove(fragmentIDString)
                    
                    // Suppress re-broadcast of the reassembled packet by zeroing TTL.
                    // We already relayed the incoming fragments; setting TTL=0 ensures
                    // PacketRelayManager will skip relaying this reconstructed packet.
                    val suppressedTtlPacket = originalPacket.copy(ttl = 0u.toUByte())
                    Log.d(TAG, "Successfully reassembled original (${reassembledData.size} bytes); set TTL=0 to suppress relay")
                    return suppressedTtlPacket
                } else {
                    val metadata = fragmentMetadata[fragmentIDString]
                    Log.e(TAG, "Failed to decode reassembled packet (type=${metadata?.first}, total=${metadata?.second})")
                }
            } else {
                val received = fragmentMap?.size ?: 0
                Log.d(TAG, "Fragment ${fragmentPayload.index} stored, have $received/${fragmentPayload.total} fragments for $fragmentIDString")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to handle fragment: ${e.message}")
        }
        
        return null
    }
    
    /**
     * Helper function to match iOS stride functionality
     * stride(from: 0, to: fullData.count, by: maxFragmentSize)
     */
    private fun <T> stride(from: Int, to: Int, by: Int, transform: (Int) -> T): List<T> {
        val result = mutableListOf<T>()
        var current = from
        while (current < to) {
            result.add(transform(current))
            current += by
        }
        return result
    }
    
    /**
     * iOS cleanup - exactly matching performCleanup() implementation
     * Clean old fragments (> 30 seconds old)
     */
    private fun cleanupOldFragments() {
        val now = System.currentTimeMillis()
        val cutoff = now - FRAGMENT_TIMEOUT
        
        
        val oldFragments = fragmentMetadata.filter { it.value.third < cutoff }.map { it.key }
        
        
        for (fragmentID in oldFragments) {
            incomingFragments.remove(fragmentID)
            fragmentMetadata.remove(fragmentID)
        }
        
        if (oldFragments.isNotEmpty()) {
            Log.d(TAG, "Cleaned up ${oldFragments.size} old fragment sets (iOS compatible)")
        }
    }
    
    /**
     * Get debug information - matches iOS debugging
     */
    fun getDebugInfo(): String {
        return buildString {
            appendLine("=== Fragment Manager Debug Info (iOS Compatible) ===")
            appendLine("Active Fragment Sets: ${incomingFragments.size}")
            appendLine("Fragment Size Threshold: $FRAGMENT_SIZE_THRESHOLD bytes")
            appendLine("Max Fragment Size: $MAX_FRAGMENT_SIZE bytes")
            
            fragmentMetadata.forEach { (fragmentID, metadata) ->
                val (originalType, totalFragments, timestamp) = metadata
                val received = incomingFragments[fragmentID]?.size ?: 0
                val ageSeconds = (System.currentTimeMillis() - timestamp) / 1000
                appendLine("  - $fragmentID: $received/$totalFragments fragments, type: $originalType, age: ${ageSeconds}s")
            }
        }
    }
    
    /**
     * Start periodic cleanup of old fragments - matches iOS maintenance timer
     */
    private fun startPeriodicCleanup() {
        cleanupJob?.cancel()
        cleanupJob = managerScope.launch {
            while (isActive) {
                delay(CLEANUP_INTERVAL)
                cleanupOldFragments()
            }
        }
    }
    
    /**
     * Clear all fragments
     */
    fun clearAllFragments() {
        incomingFragments.clear()
        fragmentMetadata.clear()
    }
    
    /**
     * Shutdown the manager
     */
    fun shutdown() {
        cleanupJob?.cancel()
        cleanupJob = null
        managerScope.cancel()
        clearAllFragments()
    }

    fun restart() {
        cleanupJob?.cancel()
        cleanupJob = null
        managerScope.cancel()
        managerScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        startPeriodicCleanup()
    }
}

/**
 * Delegate interface for fragment manager callbacks
 */
interface FragmentManagerDelegate {
    fun onPacketReassembled(packet: BitchatPacket)
}
