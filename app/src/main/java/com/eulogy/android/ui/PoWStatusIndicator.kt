package com.eulogy.android.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eulogy.android.nostr.NostrProofOfWork
import androidx.compose.ui.res.stringResource
import com.eulogy.android.R
import com.eulogy.android.ui.theme.AppMonospaceFont
import com.eulogy.android.nostr.PoWPreferenceManager

/**
 * Shows the current Proof of Work status and settings
 */
@Composable
fun PoWStatusIndicator(
    modifier: Modifier = Modifier,
    style: PoWIndicatorStyle = PoWIndicatorStyle.COMPACT
) {
    val powEnabled by PoWPreferenceManager.powEnabled.collectAsState()
    val powDifficulty by PoWPreferenceManager.powDifficulty.collectAsState()
    val isMining by PoWPreferenceManager.isMining.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    val isDark = colorScheme.background.red + colorScheme.background.green + colorScheme.background.blue < 1.5f
    
    if (!powEnabled) return
    
    when (style) {
        PoWIndicatorStyle.COMPACT -> {
            Row(
                modifier = modifier,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // PoW icon with animation if mining
                if (isMining) {
                    val rotation by rememberInfiniteTransition(label = "pow-rotation").animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "pow-icon-rotation"
                    )
                    
                    Icon(
                        imageVector = Icons.Filled.Security,
                        contentDescription = stringResource(R.string.cd_mining_pow),
                        tint = Color(0xFFFFD6A5), // Soft peach for mining
                        modifier = Modifier
                            .size(12.dp)
                            .graphicsLayer { rotationZ = rotation }
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Security,
                        contentDescription = stringResource(R.string.cd_pow_enabled),
                        tint = Color(0xFFBFFCC6), // Pastel mint when ready
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
        
        PoWIndicatorStyle.DETAILED -> {
            Surface(
                modifier = modifier,
                color = colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // PoW icon
                    Icon(
                        imageVector = Icons.Filled.Security,
                        contentDescription = stringResource(R.string.cd_proof_of_work),
                        tint = if (isMining) Color(0xFFFFD6A5) else Color(0xFFBFFCC6), // Peach or mint
                        modifier = Modifier.size(14.dp)
                    )
                    
                    // Status text
                    Text(
                        text = if (isMining) {
                            stringResource(R.string.pow_mining_ellipsis)
                        } else {
                            stringResource(R.string.pow_label_format, powDifficulty)
                        },
                        fontSize = 11.sp,
                        fontFamily = AppMonospaceFont,
                        color = if (isMining) Color(0xFFFFD6A5) else Color(0xFFBFFCC6) // Peach or mint
                    )
                    
                    // Time estimate
                    if (!isMining && powDifficulty > 0) {
                        Text(
                            text = stringResource(R.string.pow_time_estimate, NostrProofOfWork.estimateMiningTime(powDifficulty)),
                            fontSize = 9.sp,
                            fontFamily = AppMonospaceFont,
                            color = colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Style options for the PoW status indicator
 */
enum class PoWIndicatorStyle {
    COMPACT,    // Small icon + difficulty number
    DETAILED    // Icon + status text + time estimate
}
