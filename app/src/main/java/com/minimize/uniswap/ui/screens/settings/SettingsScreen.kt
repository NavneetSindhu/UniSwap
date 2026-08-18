package com.minimize.uniswap.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val PrimaryGreen = Color(0xFF146345)
private val SectionTitleGreen = Color(0xFF147A53)
private val DarkText = Color(0xFF181C20)
private val SubtitleGray = Color(0xFF707772)
private val ArrowGray = Color(0xFFB0B8B2)
private val DividerColor = Color(0xFFF1F3F2)
private val CardBackground = Color.White
private val ScreenBackground = Color(0xFFFBFBF9)
private val LogoutRed = Color(0xFFB3261E)
private val LogoutButtonBackground = Color(0xFFEDEFEF)

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onChangeCampusClick: () -> Unit = {},
    onBadgesClick: () -> Unit = {},
    onCampusRulesClick: () -> Unit = {},
    onVerifiedStatusClick: () -> Unit = {},
    onHelpCenterClick: () -> Unit = {},
    onTermsClick: () -> Unit = {},
    onPrivacyPolicyClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    var pushNotificationsEnabled by remember { mutableStateOf(true) }
    var emailDigestsEnabled by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = ScreenBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            SettingsTopBar(onBackClick = onBackClick)
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Screen Title
            item {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = DarkText,
                        fontSize = 28.sp
                    ),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            // 1. Account Section
            item {
                SettingsSectionCard(title = "ACCOUNT") {
                    SettingsNavigationItem(
                        icon = Icons.Outlined.Person,
                        title = "Edit Profile",
                        onClick = onEditProfileClick
                    )
                    SettingsDivider()
                    SettingsNavigationItem(
                        icon = Icons.Outlined.Place,
                        title = "Change Campus",
                        subtitle = "North Campus",
                        onClick = onChangeCampusClick
                    )
                    SettingsDivider()
                    SettingsNavigationItem(
                        icon = Icons.Outlined.MilitaryTech,
                        title = "Sustainability Badges",
                        onClick = onBadgesClick
                    )
                }
            }

            // 2. Notifications Section
            item {
                SettingsSectionCard(title = "NOTIFICATIONS") {
                    SettingsSwitchItem(
                        icon = Icons.Outlined.Notifications,
                        title = "Push Notifications",
                        checked = pushNotificationsEnabled,
                        onCheckedChange = { pushNotificationsEnabled = it }
                    )
                    SettingsDivider()
                    SettingsSwitchItem(
                        icon = Icons.Outlined.Mail,
                        title = "Email Digests",
                        checked = emailDigestsEnabled,
                        onCheckedChange = { emailDigestsEnabled = it }
                    )
                }
            }

            // 3. Community & Safety Section
            item {
                SettingsSectionCard(title = "COMMUNITY & SAFETY") {
                    SettingsNavigationItem(
                        icon = Icons.Outlined.Gavel,
                        title = "Campus Rules",
                        onClick = onCampusRulesClick
                    )
                    SettingsDivider()
                    SettingsNavigationItem(
                        icon = Icons.Outlined.Shield,
                        title = "Verified Student Status",
                        trailingBadge = {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = "Verified",
                                tint = PrimaryGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        onClick = onVerifiedStatusClick
                    )
                    SettingsDivider()
                    SettingsNavigationItem(
                        icon = Icons.AutoMirrored.Outlined.HelpOutline,
                        title = "Help Center",
                        onClick = onHelpCenterClick
                    )
                }
            }

            // 4. About Section
            item {
                SettingsSectionCard(title = "ABOUT") {
                    SettingsNavigationItem(
                        icon = Icons.AutoMirrored.Outlined.Assignment,
                        title = "Terms of Service",
                        onClick = onTermsClick
                    )
                    SettingsDivider()
                    SettingsNavigationItem(
                        icon = Icons.Outlined.Security,
                        title = "Privacy Policy",
                        onClick = onPrivacyPolicyClick
                    )
                }
            }

            // 5. Log Out Button
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(onClick = onLogoutClick),
                        color = LogoutButtonBackground,
                        shape = CircleShape
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 32.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Logout,
                                contentDescription = null,
                                tint = LogoutRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Log Out",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = LogoutRed,
                                    fontSize = 15.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsTopBar(onBackClick: () -> Unit) {
    Surface(
        color = ScreenBackground,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = PrimaryGreen,
                    modifier = Modifier.size(24.dp)
                )
            }

            Text(
                text = "Shared Gear",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = PrimaryGreen,
                    fontSize = 22.sp
                ),
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(24.dp), spotColor = Color.Black.copy(alpha = 0.04f)),
        shape = RoundedCornerShape(24.dp),
        color = CardBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = SectionTitleGreen,
                    letterSpacing = 0.8.sp,
                    fontSize = 12.sp
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

@Composable
private fun SettingsNavigationItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    trailingBadge: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PrimaryGreen,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    color = DarkText,
                    fontSize = 15.sp
                )
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = SubtitleGray,
                        fontSize = 13.sp
                    )
                )
            }
        }
        if (trailingBadge != null) {
            trailingBadge()
        } else {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = ArrowGray,
                modifier = Modifier.size(13.dp)
            )
        }
    }
}

@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PrimaryGreen,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium,
                color = DarkText,
                fontSize = 15.sp
            ),
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = PrimaryGreen,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFD6DCD8),
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        thickness = 1.dp,
        color = DividerColor,
        modifier = Modifier.padding(start = 38.dp)
    )
}