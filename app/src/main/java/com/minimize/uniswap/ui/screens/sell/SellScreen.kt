package com.minimize.uniswap.ui.screens.sell

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.CurrencyRupee
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Title
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.minimize.uniswap.data.model.ItemCategory
import com.minimize.uniswap.ui.components.AppBottomSheet
import com.minimize.uniswap.ui.components.nudge.EmailVerificationFlow
import com.minimize.uniswap.ui.components.nudge.VerificationNudgeDialog
import java.util.Locale

private val PrimaryGreen = Color(0xFF146345)
private val SectionTitleGreen = Color(0xFF147A53)
private val ScreenBackground = Color(0xFFFBFBF9)
private val CardBackground = Color.White
private val DarkText = Color(0xFF181C20)
private val SubtitleGray = Color(0xFF707772)
private val BorderStrokeColor = Color(0xFFE4E9E6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellScreen(
    onPostSuccess: () -> Unit,
    onBackClick: () -> Unit = {},
    viewModel: SellViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val title by viewModel.title.collectAsStateWithLifecycle()
    val price by viewModel.price.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedImages by viewModel.selectedImages.collectAsStateWithLifecycle()
    val isPosting by viewModel.isPosting.collectAsStateWithLifecycle()

    // Email Verification Nudge Barrier
    if (uiState.showNudge) {
        VerificationNudgeDialog(
            onDismiss = { viewModel.dismissNudge() },
            onVerifyClick = { viewModel.startVerificationFlow() }
        )
    }

    if (uiState.showVerificationFlow) {
        EmailVerificationFlow(
            email = uiState.userEmail,
            onSendEmail = { viewModel.sendVerificationEmail() },
            onCheckStatus = { viewModel.checkVerificationStatus() },
            isProcessing = uiState.isProcessingVerification,
            isSent = uiState.isVerificationSent,
            isVerified = uiState.isEmailVerified,
            onDismiss = { viewModel.dismissNudge() }
        )
    }

    var showCategorySheet by remember { mutableStateOf(false) }
    var categorySearchQuery by remember { mutableStateOf("") }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris -> viewModel.onImagesSelected(uris) }

    Scaffold(
        containerColor = ScreenBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
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
        },
        bottomBar = {
            Surface(
                color = ScreenBackground,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 12.dp)
                ) {
                    Button(
                        onClick = { viewModel.onPostAttempt(onPostSuccess) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryGreen,
                            disabledContainerColor = PrimaryGreen.copy(alpha = 0.4f)
                        ),
                        enabled = !isPosting && title.isNotBlank()
                    ) {
                        if (isPosting) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                text = "List Item Now",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Header
            Text(
                text = "New Listing",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = DarkText,
                    fontSize = 28.sp
                )
            )
            Text(
                text = "Details & Media",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = SubtitleGray,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.padding(top = 2.dp, bottom = 18.dp)
            )

            // Multi-Image Upload Strip
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(116.dp)
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .size(116.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(CardBackground)
                            .border(1.5.dp, BorderStrokeColor, RoundedCornerShape(20.dp))
                            .clickable { launcher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddAPhoto,
                                contentDescription = "Add Photos",
                                tint = PrimaryGreen,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Add Photo",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = SubtitleGray,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }

                items(selectedImages) { uri ->
                    Box(
                        modifier = Modifier
                            .size(116.dp)
                            .clip(RoundedCornerShape(20.dp))
                    ) {
                        AsyncImage(
                            model = uri,
                            contentDescription = "Selected Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Form Section Card
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
                        .padding(20.dp)
                ) {
                    // Category Picker Card
                    Text(
                        text = "CATEGORY",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SectionTitleGreen,
                            letterSpacing = 0.8.sp,
                            fontSize = 12.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, BorderStrokeColor, RoundedCornerShape(14.dp))
                            .clickable {
                                categorySearchQuery = ""
                                showCategorySheet = true
                            },
                        color = Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Category,
                                contentDescription = null,
                                tint = PrimaryGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = selectedCategory?.name?.replace("_", " ")
                                    ?.lowercase(Locale.getDefault())
                                    ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                                    ?: "Select Category",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = if (selectedCategory != null) DarkText else SubtitleGray,
                                    fontWeight = if (selectedCategory != null) FontWeight.SemiBold else FontWeight.Normal,
                                    fontSize = 15.sp
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = null,
                                tint = SubtitleGray,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    HorizontalDivider(
                        thickness = 1.dp,
                        color = Color(0xFFF1F3F2),
                        modifier = Modifier.padding(vertical = 18.dp)
                    )

                    // Title Input
                    Text(
                        text = "TITLE",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SectionTitleGreen,
                            letterSpacing = 0.8.sp,
                            fontSize = 12.sp
                        )
                    )
                    OutlinedTextField(
                        value = title,
                        onValueChange = { viewModel.onTitleChange(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Title,
                                contentDescription = null,
                                tint = PrimaryGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        placeholder = { Text("e.g. TI-84 Graphing Calculator", color = SubtitleGray) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = BorderStrokeColor,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Price Input
                    Text(
                        text = "PRICE",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SectionTitleGreen,
                            letterSpacing = 0.8.sp,
                            fontSize = 12.sp
                        )
                    )
                    OutlinedTextField(
                        value = price,
                        onValueChange = { input ->
                            if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                                viewModel.onPriceChange(input)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.CurrencyRupee,
                                contentDescription = null,
                                tint = PrimaryGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        prefix = {
                            Text(
                                text = "₹ ",
                                fontWeight = FontWeight.Bold,
                                color = DarkText
                            )
                        },
                        placeholder = { Text("0 for free giveaway", color = SubtitleGray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = BorderStrokeColor,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Searchable Category Sheet using AppBottomSheet
        if (showCategorySheet) {
            val filteredCategories = remember(categorySearchQuery) {
                ItemCategory.entries.filter {
                    it.name.replace("_", " ").contains(categorySearchQuery.trim(), ignoreCase = true)
                }
            }

            AppBottomSheet(
                onDismissRequest = { showCategorySheet = false },
                containerColor = CardBackground
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                ) {
                    Text(
                        text = "Select Category",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = DarkText
                        ),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Search field
                    OutlinedTextField(
                        value = categorySearchQuery,
                        onValueChange = { categorySearchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search category...", color = SubtitleGray) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = null,
                                tint = SubtitleGray
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = BorderStrokeColor,
                            focusedContainerColor = ScreenBackground,
                            unfocusedContainerColor = ScreenBackground
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredCategories) { category ->
                            val isSelected = selectedCategory == category
                            val formattedName = category.name
                                .replace("_", " ")
                                .lowercase(Locale.getDefault())
                                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.onCategoryChange(category)
                                        showCategorySheet = false
                                    }
                                    .padding(vertical = 14.dp, horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = formattedName,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = if (isSelected) PrimaryGreen else DarkText,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = PrimaryGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            HorizontalDivider(thickness = 0.8.dp, color = Color(0xFFF1F3F2))
                        }
                    }
                }
            }
        }
    }
}