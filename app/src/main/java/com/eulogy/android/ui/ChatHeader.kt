package com.eulogy.android.ui


import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.res.stringResource
import com.eulogy.android.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Constraints
import com.eulogy.android.core.ui.utils.singleOrTripleClickable
import com.eulogy.android.ui.theme.AppMonospaceFont
import com.eulogy.android.geohash.LocationChannelManager.PermissionState
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset

/**
 * Header components for ChatScreen
 * Extracted from ChatScreen.kt for better organization
 */


/**
 * Reactive helper to compute favorite state from fingerprint mapping
 * This eliminates the need for static isFavorite parameters and makes
 * the UI reactive to fingerprint manager changes
 */
@Composable
fun isFavoriteReactive(
    peerID: String,
    peerFingerprints: Map<String, String>,
    favoritePeers: Set<String>
): Boolean {
    return remember(peerID, peerFingerprints, favoritePeers) {
        val fingerprint = peerFingerprints[peerID]
        fingerprint != null && favoritePeers.contains(fingerprint)
    }
}

@Composable
fun TorStatusDot(
    modifier: Modifier = Modifier
) {
    val torStatus by com.eulogy.android.net.TorManager.statusFlow.collectAsState()
    
    if (torStatus.mode != com.eulogy.android.net.TorMode.OFF) {
        val dotColor = when {
            torStatus.running && torStatus.bootstrapPercent < 100 -> Color(0xFFFFD6A5) // Soft peach - bootstrapping
            torStatus.running && torStatus.bootstrapPercent >= 100 -> Color(0xFFB5EAD7) // Calm mint - connected
            else -> Color.Red // Red - error/disconnected
        }
        Canvas(
            modifier = modifier
        ) {
            val radius = size.minDimension / 2
            drawCircle(
                color = dotColor,
                radius = radius,
                center = Offset(size.width / 2, size.height / 2)
            )
        }
    }
}

@Composable
fun NoiseSessionIcon(
    sessionState: String?,
    modifier: Modifier = Modifier
) {
    val (icon, color, contentDescription) = when (sessionState) {
        "uninitialized" -> Triple(
            Icons.Outlined.NoEncryption,
            Color(0xFFA1A1AA), // Soft gray - ready to establish
            stringResource(R.string.cd_ready_for_handshake)
        )
        "handshaking" -> Triple(
            Icons.Outlined.Sync,
            Color(0xFFA1A1AA), // Soft gray - in progress
            stringResource(R.string.cd_handshake_in_progress)
        )
        "established" -> Triple(
            Icons.Filled.Lock,
            Color(0xFFA785E0), // Deep lavender - secure
            stringResource(R.string.cd_encrypted)
        )
        else -> { // "failed" or any other state
            Triple(
                Icons.Outlined.Warning,
                Color(0xFFF28B82), // Pastel red - error
                stringResource(R.string.cd_handshake_failed)
            )
        }
    }
    
    Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = color
    )
}

@Composable
fun NicknameEditor(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    
    // Auto-scroll to end when text changes (simulates cursor following)
    LaunchedEffect(value) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.at_symbol),
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = AppMonospaceFont,
            color = colorScheme.primary.copy(alpha = 0.8f)
        )
        
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = colorScheme.primary,
                fontFamily = AppMonospaceFont
            ),
            cursorBrush = SolidColor(colorScheme.primary),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { 
                    focusManager.clearFocus()
                }
            ),
            modifier = Modifier
                .widthIn(max = 120.dp)
                .horizontalScroll(scrollState)
        )
    }
}

@Composable
fun PeerCounter(
    connectedPeers: List<String>,
    joinedChannels: Set<String>,
    hasUnreadChannels: Map<String, Int>,
    isConnected: Boolean,
    selectedLocationChannel: com.eulogy.android.geohash.ChannelID?,
    geohashPeople: List<GeoPerson>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    
    
    val (peopleCount, countColor) = when (selectedLocationChannel) {
        is com.eulogy.android.geohash.ChannelID.Location -> {
            // Geohash channel: show geohash participants
            val count = geohashPeople.size
            val green = Color(0xFFB5EAD7) // Calm mint for connected
            Pair(count, if (count > 0) green else Color.Gray)
        }
        is com.eulogy.android.geohash.ChannelID.Mesh,
        null -> {
            // Mesh channel: show Bluetooth-connected peers (excluding self)
            val count = connectedPeers.size
            val meshBlue = Color(0xFFA3C4F3) // Pastel sky blue for mesh
            Pair(count, if (isConnected && count > 0) meshBlue else Color.Gray)
        }
    }
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clickable { onClick() }
            .padding(end = 8.dp) 
    ) {
        Text(
            text = "$peopleCount",
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = AppMonospaceFont,
            color = countColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        
        if (joinedChannels.isNotEmpty()) {
            Text(
                text = stringResource(R.string.channel_count_prefix) + "${joinedChannels.size}",
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = AppMonospaceFont,
                color = if (isConnected) Color(0xFFB5EAD7) else Color(0xFFF28B82), // Mint or pastel red
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ChatHeaderContent(
    selectedPrivatePeer: String?,
    currentChannel: String?,
    nickname: String,
    viewModel: ChatViewModel,
    onBackClick: () -> Unit,
    onSidebarClick: () -> Unit,
    onTripleClick: () -> Unit,
    onShowAppInfo: () -> Unit,
    onLocationChannelsClick: () -> Unit,
    onLocationNotesClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    when {
        selectedPrivatePeer != null -> {
            // Private chat header - Fully reactive state tracking
            val favoritePeers by viewModel.favoritePeers.observeAsState(emptySet())
            val peerFingerprints by viewModel.peerFingerprints.observeAsState(emptyMap())
            val peerSessionStates by viewModel.peerSessionStates.observeAsState(emptyMap())
            val peerNicknames by viewModel.peerNicknames.observeAsState(emptyMap())
            
            // Reactive favorite computation - no more static lookups!
            val isFavorite = isFavoriteReactive(
                peerID = selectedPrivatePeer,
                peerFingerprints = peerFingerprints,
                favoritePeers = favoritePeers
            )
            val sessionState = peerSessionStates[selectedPrivatePeer]
            
            Log.d("ChatHeader", "Header recomposing: peer=$selectedPrivatePeer, isFav=$isFavorite, sessionState=$sessionState")
            
            // Pass geohash context and people for NIP-17 chat title formatting
            val selectedLocationChannel by viewModel.selectedLocationChannel.observeAsState()
            val geohashPeople by viewModel.geohashPeople.observeAsState(emptyList())

            PrivateChatHeader(
                peerID = selectedPrivatePeer,
                peerNicknames = peerNicknames,
                isFavorite = isFavorite,
                sessionState = sessionState,
                selectedLocationChannel = selectedLocationChannel,
                geohashPeople = geohashPeople,
                onBackClick = onBackClick,
                onToggleFavorite = { viewModel.toggleFavorite(selectedPrivatePeer) },
                viewModel = viewModel
            )
        }
        currentChannel != null -> {
            // Channel header
            ChannelHeader(
                channel = currentChannel,
                onBackClick = onBackClick,
                onLeaveChannel = { viewModel.leaveChannel(currentChannel) },
                onSidebarClick = onSidebarClick
            )
        }
        else -> {
            // Main header
            MainHeader(
                nickname = nickname,
                onNicknameChange = viewModel::setNickname,
                onTitleClick = onShowAppInfo,
                onTripleTitleClick = onTripleClick,
                onSidebarClick = onSidebarClick,
                onLocationChannelsClick = onLocationChannelsClick,
                onLocationNotesClick = onLocationNotesClick,
                viewModel = viewModel
            )
        }
    }
}

@Composable
private fun PrivateChatHeader(
    peerID: String,
    peerNicknames: Map<String, String>,
    isFavorite: Boolean,
    sessionState: String?,
    selectedLocationChannel: com.eulogy.android.geohash.ChannelID?,
    geohashPeople: List<GeoPerson>,
    onBackClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    viewModel: ChatViewModel
) {
    val colorScheme = MaterialTheme.colorScheme
    val isNostrDM = peerID.startsWith("nostr_") || peerID.startsWith("nostr:")
    // Determine mutual favorite state for this peer (supports mesh ephemeral 16-hex via favorites lookup)
    val isMutualFavorite = remember(peerID, peerNicknames) {
        try {
            if (isNostrDM) return@remember false
            if (peerID.length == 64 && peerID.matches(Regex("^[0-9a-fA-F]+$"))) {
                val noiseKeyBytes = peerID.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
                com.eulogy.android.favorites.FavoritesPersistenceService.shared.getFavoriteStatus(noiseKeyBytes)?.isMutual == true
            } else if (peerID.length == 16 && peerID.matches(Regex("^[0-9a-fA-F]+$"))) {
                com.eulogy.android.favorites.FavoritesPersistenceService.shared.getFavoriteStatus(peerID)?.isMutual == true
            } else false
        } catch (_: Exception) { false }
    }

    
    val titleText: String = if (isNostrDM) {
        // For geohash DMs, get the actual source geohash and proper display name
        val (conversationGeohash, baseName) = try {
            val repoField = com.eulogy.android.ui.GeohashViewModel::class.java.getDeclaredField("repo")
            repoField.isAccessible = true
            val repo = repoField.get(viewModel.geohashViewModel) as com.eulogy.android.nostr.GeohashRepository
            val gh = repo.getConversationGeohash(peerID) ?: "geohash"
            val fullPubkey = com.eulogy.android.nostr.GeohashAliasRegistry.get(peerID) ?: ""
            val displayName = if (fullPubkey.isNotEmpty()) {
                repo.displayNameForGeohashConversation(fullPubkey, gh)
            } else {
                peerNicknames[peerID] ?: "unknown"
            }
            Pair(gh, displayName)
        } catch (e: Exception) { 
            Pair("geohash", peerNicknames[peerID] ?: "unknown")
        }
        
        "#$conversationGeohash/@$baseName"
    } else {
        // Prefer live mesh nickname; fallback to favorites nickname (supports 16-hex), finally short key
        peerNicknames[peerID] ?: run {
            val titleFromFavorites = try {
                if (peerID.length == 64 && peerID.matches(Regex("^[0-9a-fA-F]+$"))) {
                    val noiseKeyBytes = peerID.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
                    com.eulogy.android.favorites.FavoritesPersistenceService.shared.getFavoriteStatus(noiseKeyBytes)?.peerNickname
                } else if (peerID.length == 16 && peerID.matches(Regex("^[0-9a-fA-F]+$"))) {
                    com.eulogy.android.favorites.FavoritesPersistenceService.shared.getFavoriteStatus(peerID)?.peerNickname
                } else null
            } catch (_: Exception) { null }
            titleFromFavorites ?: peerID.take(12)
        }
    }
    
    Box(modifier = Modifier.fillMaxWidth()) {
        // Back button - positioned all the way to the left with minimal margin
        Button(
            onClick = onBackClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = colorScheme.primary
            ),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp), // Reduced horizontal padding
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = (-8).dp) // Move even further left to minimize margin
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    modifier = Modifier.size(16.dp),
                    tint = colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.chat_back),
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = AppMonospaceFont,
                    color = colorScheme.primary
                )
            }
        }
        
        // Title - perfectly centered regardless of other elements
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.align(Alignment.Center)
        ) {
            
            Text(
                text = titleText,
                style = MaterialTheme.typography.titleMedium,
                fontFamily = AppMonospaceFont,
                color = Color(0xFFFFD6A5) // Soft peach
            )

            Spacer(modifier = Modifier.width(4.dp))

            // Show a globe when chatting via Nostr alias, or when mesh session not established but mutual favorite exists
            val showGlobe = isNostrDM || (sessionState != "established" && isMutualFavorite)
            if (showGlobe) {
                Icon(
                    imageVector = Icons.Outlined.Public,
                contentDescription = stringResource(R.string.cd_nostr_reachable),
                    modifier = Modifier.size(14.dp),
                    tint = Color(0xFFB185DB) // Muted violet
                )
            } else {
                NoiseSessionIcon(
                    sessionState = sessionState,
                    modifier = Modifier.size(14.dp)
                )
            }

        }
        
        // Favorite button - positioned on the right
        IconButton(
            onClick = {
                Log.d("ChatHeader", "Header toggle favorite: peerID=$peerID, currentFavorite=$isFavorite")
                onToggleFavorite()
            },
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = if (isFavorite) stringResource(R.string.cd_remove_favorite) else stringResource(R.string.cd_add_favorite),
                modifier = Modifier.size(18.dp), // Slightly larger than sidebar icon
                tint = if (isFavorite) Color(0xFFFDE68A) else Color(0xFFA1A1AA) // Soft yellow or gray
            )
        }
    }
}

@Composable
private fun ChannelHeader(
    channel: String,
    onBackClick: () -> Unit,
    onLeaveChannel: () -> Unit,
    onSidebarClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    
    Box(modifier = Modifier.fillMaxWidth()) {
        // Back button - positioned all the way to the left with minimal margin
        Button(
            onClick = onBackClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = colorScheme.primary
            ),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp), // Reduced horizontal padding
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = (-8).dp) // Move even further left to minimize margin
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    modifier = Modifier.size(16.dp),
                    tint = colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.chat_back),
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = AppMonospaceFont,
                    color = colorScheme.primary
                )
            }
        }
        
        // Title - perfectly centered regardless of other elements
        Text(
            text = stringResource(R.string.chat_channel_prefix, channel),
            style = MaterialTheme.typography.titleMedium,
            fontFamily = AppMonospaceFont,
            color = Color(0xFFFFD6A5), // Soft peach to match theme
            modifier = Modifier
                .align(Alignment.Center)
                .clickable { onSidebarClick() }
        )
        
        // Leave button - positioned on the right
        TextButton(
            onClick = onLeaveChannel,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Text(
                text = stringResource(R.string.chat_leave),
                style = MaterialTheme.typography.bodySmall,
                fontFamily = AppMonospaceFont,
                color = Color.Red
            )
        }
    }
}

private enum class HeaderSlot { Left, Center, Right }

// Ensures the top bar leaves enough room for the left/right clusters while keeping the
// brand text visually centered whenever space allows.
@Composable
private fun BalancedHeaderLayout(
    modifier: Modifier = Modifier,
    left: @Composable () -> Unit,
    center: @Composable () -> Unit,
    right: @Composable () -> Unit
) {
    SubcomposeLayout(modifier = modifier) { constraints ->
        val looseConstraints = Constraints(
            minWidth = 0,
            maxWidth = constraints.maxWidth,
            minHeight = 0,
            maxHeight = constraints.maxHeight
        )

        val leftPlaceables = subcompose(HeaderSlot.Left) { left() }
            .map { it.measure(looseConstraints) }
        val rightPlaceables = subcompose(HeaderSlot.Right) { right() }
            .map { it.measure(looseConstraints) }

        val leftWidth = leftPlaceables.maxOfOrNull { it.width } ?: 0
        val rightWidth = rightPlaceables.maxOfOrNull { it.width } ?: 0
        val hasBoundedWidth = constraints.maxWidth != Constraints.Infinity

        val maxCenterWidth = if (hasBoundedWidth) {
            (constraints.maxWidth - leftWidth - rightWidth).coerceAtLeast(0)
        } else {
            Constraints.Infinity
        }

        val centerConstraints = Constraints(
            minWidth = 0,
            maxWidth = maxCenterWidth,
            minHeight = 0,
            maxHeight = constraints.maxHeight
        )

        val centerPlaceables = subcompose(HeaderSlot.Center) { center() }
            .map { it.measure(centerConstraints) }
        val centerWidth = centerPlaceables.maxOfOrNull { it.width } ?: 0

        val maxSideWidth = maxOf(leftWidth, rightWidth)

        val layoutWidth = if (hasBoundedWidth) {
            constraints.maxWidth
        } else {
            (maxSideWidth * 2) + centerWidth
        }

        val layoutHeight = maxOf(
            leftPlaceables.maxOfOrNull { it.height } ?: 0,
            rightPlaceables.maxOfOrNull { it.height } ?: 0,
            centerPlaceables.maxOfOrNull { it.height } ?: 0,
            constraints.minHeight
        )

        layout(layoutWidth, layoutHeight) {
            val centerY: (Int) -> Int = { childHeight -> (layoutHeight - childHeight) / 2 }

            // Reserve equal space on both sides to ensure true centering
            val availableCenter = (layoutWidth - (2 * maxSideWidth)).coerceAtLeast(0)
            
            leftPlaceables.forEach { placeable ->
                placeable.placeRelative(0, centerY(placeable.height))
            }

            rightPlaceables.forEach { placeable ->
                placeable.placeRelative(layoutWidth - placeable.width, centerY(placeable.height))
            }

            centerPlaceables.forEach { placeable ->
                val centeredX = maxSideWidth + (availableCenter / 2) - (placeable.width / 2)
                placeable.placeRelative(centeredX, centerY(placeable.height))
            }
        }
    }
}

@Composable
private fun MainHeader(
    nickname: String,
    onNicknameChange: (String) -> Unit,
    onTitleClick: () -> Unit,
    onTripleTitleClick: () -> Unit,
    onSidebarClick: () -> Unit,
    onLocationChannelsClick: () -> Unit,
    onLocationNotesClick: () -> Unit,
    viewModel: ChatViewModel
) {
    val colorScheme = MaterialTheme.colorScheme
    val connectedPeers by viewModel.connectedPeers.observeAsState(emptyList())
    val joinedChannels by viewModel.joinedChannels.observeAsState(emptySet())
    val hasUnreadChannels by viewModel.unreadChannelMessages.observeAsState(emptyMap())
    val hasUnreadPrivateMessages by viewModel.unreadPrivateMessages.observeAsState(emptySet())
    val isConnected by viewModel.isConnected.observeAsState(false)
    val selectedLocationChannel by viewModel.selectedLocationChannel.observeAsState()
    val geohashPeople by viewModel.geohashPeople.observeAsState(emptyList())

    BalancedHeaderLayout(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        left = {
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                NicknameEditor(
                    value = nickname,
                    onValueChange = onNicknameChange
                )
            }
        },
        center = {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.app_brand),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = AppMonospaceFont,
                    modifier = Modifier
                        .singleOrTripleClickable(
                            onSingleClick = onTitleClick,
                            onTripleClick = onTripleTitleClick
                        )
                )
            }
        },
        right = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.End),
                modifier = Modifier.padding(end = 8.dp)
            ) {

            // Unread private messages badge (click to open most recent DM)
            if (hasUnreadPrivateMessages.isNotEmpty()) {
                // Render icon directly to avoid symbol resolution issues
                Icon(
                    imageVector = Icons.Filled.Email,
                    contentDescription = stringResource(R.string.cd_unread_private_messages),
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { viewModel.openLatestUnreadPrivateChat() },
                    tint = Color(0xFFFFD6A5) // Soft peach
                )
            }

            // Location channels button with arrow indicator
            LocationChannelsButton(
                viewModel = viewModel,
                onClick = onLocationChannelsClick
            )

            // Tor status dot when Tor is enabled
            TorStatusDot(
                modifier = Modifier
                    .size(8.dp)
                    .padding(start = 0.dp, end = 2.dp)
            )
            
            // PoW status indicator
            PoWStatusIndicator(
                modifier = Modifier,
                style = PoWIndicatorStyle.COMPACT
            )
            }
        }
    )
}

@Composable
private fun LocationChannelsButton(
    viewModel: ChatViewModel,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    
    // Get current channel selection from location manager
    val selectedChannel by viewModel.selectedLocationChannel.observeAsState()
    val teleported by viewModel.isTeleported.observeAsState(false)
    
    val (badgeText, badgeColor) = when (selectedChannel) {
        is com.eulogy.android.geohash.ChannelID.Mesh -> {
            "#mesh" to Color(0xFFA3C4F3) // Pastel sky blue for mesh
        }
        is com.eulogy.android.geohash.ChannelID.Location -> {
            val geohash = (selectedChannel as com.eulogy.android.geohash.ChannelID.Location).channel.geohash
            "#$geohash" to Color(0xFFBFFCC6) // Pastel mint for location
        }
        null -> "#mesh" to Color(0xFFA3C4F3) // Default to pastel blue mesh
    }
    
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = badgeColor
        ),
        contentPadding = PaddingValues(start = 4.dp, end = 4.dp, top = 2.dp, bottom = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = badgeText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = AppMonospaceFont
                ),
                color = badgeColor,
                maxLines = 1
            )
            
            
            if (teleported) {
                Icon(
                    imageVector = Icons.Default.PinDrop,
                    contentDescription = stringResource(R.string.cd_teleported),
                    modifier = Modifier.size(12.dp),
                    tint = badgeColor
                )
            }
        }
    }
}
