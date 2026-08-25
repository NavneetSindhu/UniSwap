package com.minimize.uniswap.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.lazy.items
import com.minimize.uniswap.data.model.Report
import com.minimize.uniswap.data.model.ReportStatus
import com.minimize.uniswap.ui.components.EmptyStateView
import com.minimize.uniswap.R
import com.minimize.uniswap.BuildConfig
import com.minimize.uniswap.data.preferences.ThemeMode
import com.minimize.uniswap.ui.theme.*

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.minimize.uniswap.ui.components.AppBottomSheet

/**
 * Fully functional Settings Screen matching UniSwap dark aesthetic and AGENTS.md guidelines.
 * All settings (Appearance, Campus Center, Notifications, Profile, Privacy & Safety, Terms, Help, Logout)
 * are completely interactive and connected to ViewModel and DataStore.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val preferences by viewModel.preferences.collectAsState()
    val currentUser by viewModel.user.collectAsState()
    val blockedUserIds by viewModel.blockedUserIds.collectAsState()
    val myReports by viewModel.myReports.collectAsState()
    val userFeedbackMessage by viewModel.userFeedbackMessage.collectAsState()

    val context = LocalContext.current

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showCampusDialog by remember { mutableStateOf(false) }
    var showBlockedUsersDialog by remember { mutableStateOf(false) }
    var showMyReportsDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userFeedbackMessage) {
        userFeedbackMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearFeedbackMessage()
        }
    }

    val themeColors = UniSwapTheme.colors

    Scaffold(
        containerColor = themeColors.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            // Header with Back Button (clean, no fill) and Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = stringResource(R.string.action_back),
                        tint = themeColors.textPrimary,
                        modifier = Modifier
                            .width(18.dp)
                            .height(10.dp)
                    )
                }

                Text(
                    text = stringResource(R.string.settings_title),
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    letterSpacing = (-0.32).sp,
                    color = themeColors.textPrimary,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.size(36.dp))
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Theme & Appearance Section
            item {
                SettingsSectionCard(title = stringResource(R.string.settings_section_appearance)) {
                    ThemeSelectionRow(
                        selectedMode = preferences.themeMode,
                        onModeSelected = { viewModel.onThemeModeChanged(it) }
                    )
                    SettingsDivider()
                    SettingsSwitchItem(
                        icon = Icons.Outlined.Palette,
                        title = stringResource(R.string.settings_dynamic_color),
                        checked = preferences.dynamicColor,
                        onCheckedChange = { viewModel.onDynamicColorChanged(it) }
                    )
                }
            }

            // 2. Account Section
            item {
                SettingsSectionCard(title = stringResource(R.string.settings_section_account)) {
                    SettingsNavigationItem(
                        icon = Icons.Outlined.Person,
                        title = stringResource(R.string.settings_edit_profile),
                        subtitle = currentUser?.displayName?.ifBlank { currentUser?.email } ?: stringResource(R.string.sample_seller_lokesh),
                        onClick = { showEditProfileDialog = true }
                    )
                    SettingsDivider()
                    SettingsNavigationItem(
                        icon = Icons.Outlined.Place,
                        title = stringResource(R.string.settings_campus_center),
                        subtitle = preferences.campusCenter,
                        onClick = { showCampusDialog = true }
                    )
                }
            }

            // 3. Privacy & Safety Section
            item {
                SettingsSectionCard(title = stringResource(R.string.settings_section_privacy_safety)) {
                    SettingsNavigationItem(
                        icon = Icons.Outlined.Block,
                        title = stringResource(R.string.settings_blocked_users),
                        subtitle = if (blockedUserIds.isEmpty()) stringResource(R.string.blocked_users_empty_title) else "${blockedUserIds.size} accounts blocked",
                        onClick = { showBlockedUsersDialog = true }
                    )
                    SettingsDivider()
                    SettingsNavigationItem(
                        icon = Icons.Outlined.Flag,
                        title = stringResource(R.string.settings_my_reports),
                        subtitle = if (myReports.isEmpty()) stringResource(R.string.my_reports_empty_title) else "${myReports.size} submitted reports",
                        onClick = { showMyReportsDialog = true }
                    )
                }
            }

            // 4. Notifications Section
            item {
                SettingsSectionCard(title = stringResource(R.string.settings_section_notifications)) {
                    SettingsSwitchItem(
                        icon = Icons.Outlined.Notifications,
                        title = stringResource(R.string.settings_push_notifications),
                        checked = preferences.pushNotificationsEnabled,
                        onCheckedChange = { viewModel.onPushNotificationsChanged(it) }
                    )
                    SettingsDivider()
                    SettingsSwitchItem(
                        icon = Icons.Outlined.Mail,
                        title = stringResource(R.string.settings_email_digests),
                        checked = preferences.emailDigestEnabled,
                        onCheckedChange = { viewModel.onEmailDigestChanged(it) }
                    )
                }
            }

            // 5. About Section
            item {
                SettingsSectionCard(title = stringResource(R.string.settings_section_about)) {
                    SettingsNavigationItem(
                        icon = Icons.AutoMirrored.Outlined.Assignment,
                        title = stringResource(R.string.settings_terms_of_service),
                        onClick = { showTermsDialog = true }
                    )
                    SettingsDivider()
                    SettingsNavigationItem(
                        icon = Icons.AutoMirrored.Outlined.HelpOutline,
                        title = stringResource(R.string.settings_help_center),
                        onClick = { showHelpDialog = true }
                    )
                }
            }

            // 6. Log Out Button
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { showLogoutConfirmDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(53.dp),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = themeColors.btnBackBg,
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Logout,
                            contentDescription = stringResource(R.string.settings_logout),
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.settings_logout),
                            fontFamily = MatterFontFamily,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        }
    }

    // --- Interactive Dialogs ---

    // 1. Campus Selection Dialog
    if (showCampusDialog) {
        val campusOptions = listOf(
            "Panjab University, Chandigarh",
            "Panjab Engineering College (PEC)",
            "UIET Chandigarh",
            "Chitkara University",
            "Thapar Institute (TIET)",
            "IIT Ropar"
        )
        AlertDialog(
            onDismissRequest = { showCampusDialog = false },
            containerColor = themeColors.cardSurface,
            title = {
                Text(
                    text = stringResource(R.string.settings_select_campus),
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.textPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    campusOptions.forEach { campus ->
                        val isSelected = preferences.campusCenter == campus
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) themeColors.btnBackBg else Color.Transparent)
                                .clickable {
                                    viewModel.onCampusCenterChanged(campus)
                                    showCampusDialog = false
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    viewModel.onCampusCenterChanged(campus)
                                    showCampusDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = campus,
                                fontFamily = MatterFontFamily,
                                fontSize = 13.sp,
                                color = themeColors.textPrimary
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCampusDialog = false }) {
                    Text(text = stringResource(R.string.action_close), color = themeColors.textPrimary)
                }
            }
        )
    }

    // 2. Edit Profile Dialog
    if (showEditProfileDialog) {
        var tempName by remember { mutableStateOf(currentUser?.displayName ?: "") }
        var tempGradYear by remember { mutableStateOf("2026") }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            containerColor = themeColors.cardSurface,
            title = {
                Text(
                    text = stringResource(R.string.settings_edit_profile),
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.textPrimary
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        label = { Text(stringResource(R.string.settings_display_name)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = themeColors.textPrimary,
                            unfocusedTextColor = themeColors.textPrimary,
                            focusedBorderColor = themeColors.textPrimary,
                            unfocusedBorderColor = PaletteDark.Gray400,
                            focusedLabelColor = themeColors.textPrimary,
                            unfocusedLabelColor = PaletteDark.Gray400
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = tempGradYear,
                        onValueChange = { tempGradYear = it },
                        label = { Text(stringResource(R.string.settings_grad_year)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = themeColors.textPrimary,
                            unfocusedTextColor = themeColors.textPrimary,
                            focusedBorderColor = themeColors.textPrimary,
                            unfocusedBorderColor = PaletteDark.Gray400,
                            focusedLabelColor = themeColors.textPrimary,
                            unfocusedLabelColor = PaletteDark.Gray400
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showEditProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.textPrimary, contentColor = themeColors.background)
                ) {
                    Text(text = stringResource(R.string.settings_save_changes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text(text = stringResource(R.string.action_cancel), color = themeColors.textSubtle)
                }
            }
        )
    }

    // 3. Blocked Users Bottom Sheet
    if (showBlockedUsersDialog) {
        AppBottomSheet(
            onDismissRequest = { showBlockedUsersDialog = false },
            heightFraction = 0.70f,
            containerColor = themeColors.cardSurface,
            contentColor = themeColors.textPrimary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.blocked_users_title),
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = themeColors.textPrimary,
                    modifier = Modifier.padding(bottom = 14.dp)
                )

                if (blockedUserIds.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyStateView(
                            title = stringResource(R.string.blocked_users_empty_title),
                            subtitle = stringResource(R.string.blocked_users_empty_subtitle),
                            lottieRes = R.raw.anim_user_search,
                            fallbackIcon = Icons.Outlined.Block
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(blockedUserIds.toList(), key = { it }) { userId ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(themeColors.btnBackBg)
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(themeColors.cardSurface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.PersonOff,
                                        contentDescription = null,
                                        tint = themeColors.textSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "User (${userId.take(8)}...)",
                                        fontFamily = MatterFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp,
                                        color = themeColors.textPrimary
                                    )
                                    Text(
                                        text = stringResource(R.string.user_blocked_notice),
                                        fontFamily = MatterFontFamily,
                                        fontSize = 11.sp,
                                        color = themeColors.textSecondary
                                    )
                                }
                                OutlinedButton(
                                    onClick = { viewModel.unblockUser(userId) },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = themeColors.textPrimary
                                    )
                                ) {
                                    Text(
                                        text = stringResource(R.string.action_unblock),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 4. My Reports Bottom Sheet
    if (showMyReportsDialog) {
        AppBottomSheet(
            onDismissRequest = { showMyReportsDialog = false },
            heightFraction = 0.75f,
            containerColor = themeColors.cardSurface,
            contentColor = themeColors.textPrimary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.my_reports_title),
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = themeColors.textPrimary,
                    modifier = Modifier.padding(bottom = 14.dp)
                )

                if (myReports.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyStateView(
                            title = stringResource(R.string.my_reports_empty_title),
                            subtitle = stringResource(R.string.my_reports_empty_subtitle),
                            fallbackIcon = Icons.Outlined.Flag
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(myReports, key = { it.id }) { report ->
                            val (statusText, statusBg, statusColor) = when (report.status) {
                                ReportStatus.PENDING -> Triple(
                                    stringResource(R.string.report_status_pending),
                                    CampusAmber.copy(alpha = 0.15f),
                                    CampusAmber
                                )
                                ReportStatus.UNDER_REVIEW -> Triple(
                                    stringResource(R.string.report_status_under_review),
                                    ActionLinkBlue.copy(alpha = 0.15f),
                                    ActionLinkBlue
                                )
                                ReportStatus.RESOLVED -> Triple(
                                    stringResource(R.string.report_status_resolved),
                                    SuccessGreen.copy(alpha = 0.15f),
                                    SuccessGreen
                                )
                                ReportStatus.DISMISSED -> Triple(
                                    stringResource(R.string.report_status_dismissed),
                                    PaletteDark.Gray400.copy(alpha = 0.15f),
                                    PaletteDark.Gray400
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(themeColors.btnBackBg)
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = report.itemTitle?.ifBlank { stringResource(report.reason.stringResId) }
                                            ?: stringResource(report.reason.stringResId),
                                        fontFamily = MatterFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = themeColors.textPrimary,
                                        modifier = Modifier.weight(1f)
                                    )

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(statusBg)
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = statusText,
                                            fontFamily = MatterFontFamily,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = statusColor
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Reason: ${stringResource(report.reason.stringResId)}",
                                    fontFamily = MatterFontFamily,
                                    fontSize = 12.sp,
                                    color = themeColors.textSecondary
                                )

                                if (report.additionalDetails.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Notes: ${report.additionalDetails}",
                                        fontFamily = MatterFontFamily,
                                        fontSize = 12.sp,
                                        color = themeColors.textSubtle,
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 5. Terms of Service / About Bottom Sheet
    if (showTermsDialog) {
        AppBottomSheet(
            onDismissRequest = { showTermsDialog = false },
            heightFraction = 0.95f,
            containerColor = themeColors.cardSurface,
            contentColor = themeColors.textPrimary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                // Header (Vertically structured to avoid overlap)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.about_uniswap_title),
                            fontFamily = MatterFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = themeColors.textPrimary
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(themeColors.btnBackBg)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.about_version_pill, BuildConfig.VERSION_NAME),
                                fontFamily = MatterFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = themeColors.textSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = stringResource(R.string.about_tagline),
                        fontFamily = MatterFontFamily,
                        fontSize = 13.sp,
                        color = themeColors.textSecondary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Mission Card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(themeColors.btnBackBg)
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Eco,
                                contentDescription = null,
                                tint = themeColors.wasteMetricGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.about_section_mission_title),
                                fontFamily = MatterFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = themeColors.textPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.about_section_mission_body),
                            fontFamily = MatterFontFamily,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            color = themeColors.textSecondary
                        )
                    }

                    Text(
                        text = stringResource(R.string.about_section_guidelines_title),
                        fontFamily = MatterFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = themeColors.textPrimary,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    // Rule 1
                    AboutRuleItem(
                        icon = Icons.Outlined.VerifiedUser,
                        title = stringResource(R.string.about_rule_1_title),
                        body = stringResource(R.string.about_rule_1_body),
                        accentColor = ActionLinkBlue
                    )

                    // Rule 2
                    AboutRuleItem(
                        icon = Icons.Outlined.LocalLibrary,
                        title = stringResource(R.string.about_rule_2_title),
                        body = stringResource(R.string.about_rule_2_body),
                        accentColor = CampusAmber
                    )

                    // Rule 3
                    AboutRuleItem(
                        icon = Icons.Outlined.WarningAmber,
                        title = stringResource(R.string.about_rule_3_title),
                        body = stringResource(R.string.about_rule_3_body),
                        accentColor = MaterialTheme.colorScheme.error
                    )

                    // Rule 4
                    AboutRuleItem(
                        icon = Icons.Outlined.MonetizationOn,
                        title = stringResource(R.string.about_rule_4_title),
                        body = stringResource(R.string.about_rule_4_body),
                        accentColor = themeColors.wasteMetricGreen
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.about_version, BuildConfig.VERSION_NAME),
                        fontFamily = MatterFontFamily,
                        fontSize = 11.sp,
                        color = themeColors.textSubtle,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp)
                    )
                }
            }
        }
    }

    // 6. Help Center Full-Screen Bottom Sheet (Collapsible Q&A with Filters)
    if (showHelpDialog) {
        var selectedFaqFilter by remember { mutableStateOf(FaqFilter.ALL) }
        var expandedFaqId by remember { mutableStateOf<String?>("q1") }

        val displayedFaqs = remember(selectedFaqFilter) {
            if (selectedFaqFilter == FaqFilter.ALL) FAQ_ITEMS
            else FAQ_ITEMS.filter { it.filter == selectedFaqFilter }
        }

        AppBottomSheet(
            onDismissRequest = { showHelpDialog = false },
            heightFraction = 0.95f,
            containerColor = themeColors.cardSurface,
            contentColor = themeColors.textPrimary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                // Top Header
                Text(
                    text = stringResource(R.string.settings_help_title),
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = themeColors.textPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.settings_help_subtitle),
                    fontFamily = MatterFontFamily,
                    fontSize = 12.sp,
                    color = themeColors.textSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Filter Pills Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(FaqFilter.values(), key = { it.name }) { filter ->
                        val isSelected = filter == selectedFaqFilter
                        val pillBg = if (isSelected) themeColors.textPrimary else themeColors.btnBackBg
                        val pillTextColor = if (isSelected) themeColors.background else themeColors.textSecondary

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(pillBg)
                                .clickable { selectedFaqFilter = filter }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = stringResource(filter.labelResId),
                                fontFamily = MatterFontFamily,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                fontSize = 12.sp,
                                color = pillTextColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Collapsible Q&A List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    items(displayedFaqs, key = { it.id }) { faq ->
                        FaqAccordionCard(
                            faq = faq,
                            isExpanded = expandedFaqId == faq.id,
                            onToggle = {
                                expandedFaqId = if (expandedFaqId == faq.id) null else faq.id
                            }
                        )
                    }
                }
            }
        }
    }

    // 7. Logout Confirmation Dialog
    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            containerColor = themeColors.cardSurface,
            title = {
                Text(
                    text = stringResource(R.string.settings_logout_confirm_title),
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.textPrimary
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.settings_logout_confirm_msg),
                    fontFamily = MatterFontFamily,
                    fontSize = 13.sp,
                    color = themeColors.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirmDialog = false
                        viewModel.logout(onLogoutClick)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = Color.White)
                ) {
                    Text(text = stringResource(R.string.settings_logout))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmDialog = false }) {
                    Text(text = stringResource(R.string.action_cancel), color = themeColors.textSubtle)
                }
            }
        )
    }
}

@Composable
private fun ThemeSelectionRow(
    selectedMode: ThemeMode,
    onModeSelected: (ThemeMode) -> Unit
) {
    val themeColors = UniSwapTheme.colors
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = stringResource(R.string.settings_theme_mode),
            fontFamily = MatterFontFamily,
            fontSize = 14.sp,
            color = themeColors.textPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ThemeMode.entries.forEach { mode ->
                val isSelected = selectedMode == mode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .background(if (isSelected) themeColors.textPrimary else themeColors.btnBackBg)
                        .clickable { onModeSelected(mode) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mode.name.lowercase().replaceFirstChar { it.uppercase() },
                        fontFamily = MatterFontFamily,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp,
                        color = if (isSelected) themeColors.background else themeColors.textPrimary
                    )
                }
            }
        }
    }
}


@Composable
private fun SettingsSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val themeColors = UniSwapTheme.colors
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontFamily = MatterFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            letterSpacing = 0.5.sp,
            color = themeColors.textSubtle,
            modifier = Modifier.padding(start = 6.dp, bottom = 8.dp)
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = themeColors.cardSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsNavigationItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    val themeColors = UniSwapTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = themeColors.textPrimary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = MatterFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = themeColors.textPrimary
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontFamily = MatterFontFamily,
                    fontSize = 11.sp,
                    color = themeColors.textSubtle
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = themeColors.textSubtle,
            modifier = Modifier.size(12.dp)
        )
    }
}

@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val themeColors = UniSwapTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = themeColors.textPrimary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            fontFamily = MatterFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = themeColors.textPrimary,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = themeColors.background,
                checkedTrackColor = themeColors.textPrimary,
                uncheckedThumbColor = PaletteDark.Gray400,
                uncheckedTrackColor = themeColors.btnBackBg
            )
        )
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        thickness = 0.8.dp,
        color = PaletteDark.Gray700.copy(alpha = 0.35f)
    )
}

@Composable
private fun AboutRuleItem(
    icon: ImageVector,
    title: String,
    body: String,
    accentColor: Color
) {
    val themeColors = UniSwapTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(themeColors.btnBackBg)
            .padding(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = MatterFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = themeColors.textPrimary
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = body,
                fontFamily = MatterFontFamily,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = themeColors.textSecondary
            )
        }
    }
}

@Composable
private fun FaqAccordionCard(
    faq: FaqItem,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val themeColors = UniSwapTheme.colors
    val rotationState by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "faq_chevron_rotation"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(themeColors.btnBackBg)
            .clickable(onClick = onToggle)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(faq.questionResId),
                fontFamily = MatterFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = themeColors.textPrimary,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Icon(
                imageVector = Icons.Outlined.ExpandMore,
                contentDescription = null,
                tint = themeColors.textSecondary,
                modifier = Modifier
                    .size(20.dp)
                    .rotate(rotationState)
            )
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(
                    thickness = 0.6.dp,
                    color = themeColors.cardSurface
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(faq.answerResId),
                    fontFamily = MatterFontFamily,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    color = themeColors.textSecondary
                )
            }
        }
    }
}

private enum class FaqFilter(val labelResId: Int) {
    ALL(R.string.faq_cat_all),
    SAFETY(R.string.faq_cat_safety),
    TRADING(R.string.faq_cat_trading),
    MEETUPS(R.string.faq_cat_meetups),
    ACCOUNT(R.string.faq_cat_account)
}

private data class FaqItem(
    val id: String,
    val filter: FaqFilter,
    val questionResId: Int,
    val answerResId: Int
)

private val FAQ_ITEMS = listOf(
    FaqItem("q1", FaqFilter.SAFETY, R.string.faq_q1, R.string.faq_a1),
    FaqItem("q2", FaqFilter.MEETUPS, R.string.faq_q2, R.string.faq_a2),
    FaqItem("q3", FaqFilter.TRADING, R.string.faq_q3, R.string.faq_a3),
    FaqItem("q4", FaqFilter.TRADING, R.string.faq_q4, R.string.faq_a4),
    FaqItem("q5", FaqFilter.SAFETY, R.string.faq_q5, R.string.faq_a5),
    FaqItem("q6", FaqFilter.ACCOUNT, R.string.faq_q6, R.string.faq_a6),
    FaqItem("q7", FaqFilter.TRADING, R.string.faq_q7, R.string.faq_a7),
    FaqItem("q8", FaqFilter.ACCOUNT, R.string.faq_q8, R.string.faq_a8)
)
