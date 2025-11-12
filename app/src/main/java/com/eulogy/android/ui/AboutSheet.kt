package com.eulogy.android.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eulogy.android.R
import com.eulogy.android.ui.theme.AppMonospaceFont

/**
 * About sheet matching the LocationChannelsSheet look and feel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutSheet(
    isPresented: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val versionName = remember {
        runCatching {
            context.packageManager
                .getPackageInfo(context.packageName, 0)
                .versionName
                ?.ifBlank { null }
                ?: "1.0.0"
        }.getOrElse { "1.0.0" }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val listState = rememberLazyListState()
    val isScrolled by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
        }
    }
    val topBarAlpha by animateFloatAsState(
        targetValue = if (isScrolled) 0.95f else 0f,
        label = "aboutSheetTopBarAlpha"
    )

    val colorScheme = MaterialTheme.colorScheme

    if (isPresented) {
        ModalBottomSheet(
            modifier = modifier.statusBarsPadding(),
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = colorScheme.background,
            dragHandle = null
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 80.dp, bottom = 20.dp)
                ) {
                    item(key = "header") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .padding(bottom = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.app_name),
                                style = TextStyle(
                                    fontFamily = AppMonospaceFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 32.sp
                                ),
                                color = colorScheme.onBackground
                            )
                            Text(
                                text = "000196 Foundation",
                                fontSize = 12.sp,
                                fontFamily = AppMonospaceFont,
                                fontWeight = FontWeight.Medium,
                                color = colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                            Text(
                                text = stringResource(R.string.about_tagline),
                                fontSize = 12.sp,
                                fontFamily = AppMonospaceFont,
                                color = colorScheme.onBackground.copy(alpha = 0.7f)
                            )
                        }
                    }
                    
                    item(key = "mission") {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = colorScheme.primary.copy(alpha = 0.08f)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "An initiative by the 000196 Foundation, Eulogy is a decentralized communication network built to defend open, uncensored speech in the digital age.",
                                    fontSize = 13.sp,
                                    fontFamily = AppMonospaceFont,
                                    color = colorScheme.onBackground.copy(alpha = 0.85f),
                                    lineHeight = 18.sp
                                )
                                Text(
                                    text = "We believe that freedom of expression should never rely on the approval of corporations, governments, or algorithms. Eulogy redefines messaging as a public utility — free, private, and permissionless.",
                                    fontSize = 13.sp,
                                    fontFamily = AppMonospaceFont,
                                    color = colorScheme.onBackground.copy(alpha = 0.85f),
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }

                    item(key = "feature_offline") {
                        FeatureRow(
                            icon = Icons.Filled.Bluetooth,
                            contentDescription = stringResource(R.string.cd_offline_mesh_chat),
                            title = stringResource(R.string.about_offline_mesh_title),
                            body = stringResource(R.string.about_offline_mesh_desc)
                        )
                    }

                    item(key = "feature_geohash") {
                        FeatureRow(
                            icon = Icons.Filled.Public,
                            contentDescription = stringResource(R.string.cd_online_geohash_channels),
                            title = stringResource(R.string.about_online_geohash_title),
                            body = stringResource(R.string.about_online_geohash_desc)
                        )
                    }

                    item(key = "feature_encryption") {
                        FeatureRow(
                            icon = Icons.Filled.Lock,
                            contentDescription = stringResource(R.string.cd_end_to_end_encryption),
                            title = stringResource(R.string.about_e2e_title),
                            body = stringResource(R.string.about_e2e_desc)
                        )
                    }

                    item(key = "footer") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .padding(bottom = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "balls",
                                fontSize = 11.sp,
                                fontFamily = AppMonospaceFont,
                                color = colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .height(64.dp)
                        .background(colorScheme.background.copy(alpha = topBarAlpha))
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.close_plain),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = colorScheme.onBackground
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureRow(
    icon: ImageVector,
    contentDescription: String,
    title: String,
    body: String
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .padding(vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontFamily = AppMonospaceFont,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = AppMonospaceFont,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
            )
        }
    }
}

/**
 * Password prompt dialog for password-protected channels.
 */
@Composable
fun PasswordPromptDialog(
    show: Boolean,
    channelName: String?,
    passwordInput: String,
    onPasswordChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (show && channelName != null) {
        val colorScheme = MaterialTheme.colorScheme

        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = stringResource(R.string.pwd_prompt_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = AppMonospaceFont,
                    color = colorScheme.onSurface
                )
            },
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.pwd_prompt_message, channelName),
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = AppMonospaceFont,
                        color = colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = onPasswordChange,
                        label = { Text(stringResource(R.string.pwd_label), style = MaterialTheme.typography.bodyMedium, fontFamily = AppMonospaceFont) },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = AppMonospaceFont),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorScheme.primary,
                            unfocusedBorderColor = colorScheme.outline
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text(
                        text = stringResource(R.string.join),
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = AppMonospaceFont,
                        color = colorScheme.primary
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = stringResource(R.string.cancel),
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = AppMonospaceFont,
                        color = colorScheme.onSurface
                    )
                }
            },
            containerColor = colorScheme.surface,
            tonalElevation = 8.dp
        )
    }
}
