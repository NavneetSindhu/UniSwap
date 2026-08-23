package com.minimize.uniswap.ui.screens.list

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.minimize.uniswap.R
import com.minimize.uniswap.data.model.ItemCategory
import com.minimize.uniswap.ui.components.AppBottomSheet
import com.minimize.uniswap.ui.components.nudge.EmailVerificationFlow
import com.minimize.uniswap.ui.components.nudge.VerificationNudgeDialog
import com.minimize.uniswap.ui.theme.*
import java.util.Locale

/**
 * Sell / List Product Screen matching exact Figma CSS tokens.
 * Features 164x164 image carousel with dot indicators, cancel button, full-screen preview dialog,
 * 15dp rounded input cards, and 62dp sticky "List your product" button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListProductScreen(
    onPostSuccess: () -> Unit,
    onBackClick: () -> Unit = {},
    viewModel: ListViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val title by viewModel.title.collectAsStateWithLifecycle()
    val price by viewModel.price.collectAsStateWithLifecycle()
    val description by viewModel.description.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedImages by viewModel.selectedImages.collectAsStateWithLifecycle()
    val isPosting by viewModel.isPosting.collectAsStateWithLifecycle()

    var showCategorySheet by remember { mutableStateOf(false) }
    var categorySearchQuery by remember { mutableStateOf("") }
    var previewImageUri by remember { mutableStateOf<Uri?>(null) }
    var showPhotoSourceDialog by remember { mutableStateOf(false) }

    val themeColors = UniSwapTheme.colors

    val context = androidx.compose.ui.platform.LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris -> viewModel.onImagesSelected(uris) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val uri = saveBitmapToCache(context, bitmap)
            if (uri != null) {
                viewModel.onImagesSelected(listOf(uri))
            }
        }
    }

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

    // Photo Source Bottom Sheet (Camera vs Gallery)
    if (showPhotoSourceDialog) {
        AppBottomSheet(
            onDismissRequest = { showPhotoSourceDialog = false },
            heightFraction = 0.36f,
            containerColor = themeColors.cardSurface,
            contentColor = themeColors.textPrimary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.photo_source_dialog_title),
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = themeColors.textPrimary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Option 1: Camera
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(themeColors.btnBackBg)
                        .clickable {
                            showPhotoSourceDialog = false
                            cameraLauncher.launch(null)
                        }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PhotoCamera,
                        contentDescription = stringResource(R.string.photo_source_camera),
                        tint = themeColors.textPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = stringResource(R.string.photo_source_camera),
                        fontFamily = MatterFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = themeColors.textPrimary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Option 2: Gallery
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(themeColors.btnBackBg)
                        .clickable {
                            showPhotoSourceDialog = false
                            galleryLauncher.launch("image/*")
                        }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PhotoLibrary,
                        contentDescription = stringResource(R.string.photo_source_gallery),
                        tint = themeColors.textPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = stringResource(R.string.photo_source_gallery),
                        fontFamily = MatterFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = themeColors.textPrimary
                    )
                }
            }
        }
    }

    // Full Screen Image Preview Modal
    if (previewImageUri != null) {
        Dialog(
            onDismissRequest = { previewImageUri = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.92f))
            ) {
                AsyncImage(
                    model = previewImageUri,
                    contentDescription = "Image Preview",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentScale = ContentScale.Fit
                )

                // Close Button
                Box(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(24.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(themeColors.btnBackBg)
                        .clickable { previewImageUri = null }
                        .align(Alignment.TopEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = themeColors.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            // Header: Centered "List your items..." (Matter Medium 14sp) + Custom Back Arrow
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_back),
                        contentDescription = stringResource(R.string.action_back),
                        tint = themeColors.textPrimary,
                        modifier = Modifier
                            .width(18.dp)
                            .height(10.dp)
                    )
                }

                Text(
                    text = stringResource(R.string.list_your_items_title),
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    letterSpacing = (-0.28).sp,
                    color = SearchPlaceholderColor,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.size(36.dp))
            }
        },
        bottomBar = {
            // Sticky Bottom Button: Rectangle 7 (height 62dp, radius 50, #F3F3F3)
            Surface(
                color = UniSwapTheme.colors.background,
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal))
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Button(
                    onClick = { viewModel.onPostAttempt(onPostSuccess) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(62.dp),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NavIndicatorBg,
                        contentColor = PaletteLight.Gray950
                    ),
                    enabled = !isPosting
                ) {
                    if (isPosting) {
                        CircularProgressIndicator(
                            color = PaletteLight.Gray950,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.list_your_product_button),
                            fontFamily = MatterFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            letterSpacing = (-0.32).sp,
                            color = PaletteLight.Gray950
                        )
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
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // 1. Photo Showcase Row (Responsive 1:1 Aspect Ratio Cards with consistent spacing)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Card (1:1 responsive, radius 25)
                UploadedPhotosCard(
                    images = selectedImages,
                    onImageClick = { previewImageUri = it },
                    onRemoveImage = { viewModel.onRemoveImage(it) },
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                )

                // Right Card: "Add Photos" (1:1 responsive, radius 25, #D9D9D9)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(25.dp))
                        .background(BtnChatBg)
                        .clickable { showPhotoSourceDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.add_photos_label),
                            tint = PaletteLight.Gray950,
                            modifier = Modifier.size(36.dp)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = stringResource(R.string.add_photos_label),
                            fontFamily = MatterFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            letterSpacing = (-0.28).sp,
                            color = PaletteLight.Gray950
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 2. Form Input Fields (All in CardDarkSurface #121416, radius 22dp)
            // Field 1: Title
            ListProductCardField(
                label = stringResource(R.string.field_title_label),
                value = title,
                onValueChange = { viewModel.onTitleChange(it) },
                placeholder = stringResource(R.string.field_title_placeholder),
                minHeight = 74.dp
            )

            // Field 2: Category (clickable dropdown)
            ListProductCardField(
                label = stringResource(R.string.field_category_label),
                value = selectedCategory?.name?.replace("_", " ")?.lowercase(Locale.getDefault())
                    ?.replaceFirstChar { it.uppercase() } ?: "",
                onValueChange = { },
                placeholder = stringResource(R.string.field_category_placeholder),
                readOnly = true,
                onClick = {
                    categorySearchQuery = ""
                    showCategorySheet = true
                },
                minHeight = 74.dp
            )

            // Field 3: Price
            ListProductCardField(
                label = stringResource(R.string.field_price_label),
                value = price,
                onValueChange = { viewModel.onPriceChange(it) },
                placeholder = stringResource(R.string.field_price_placeholder),
                minHeight = 74.dp
            )

            // Field 4: Description
            ListProductCardField(
                label = stringResource(R.string.field_description_label),
                value = description,
                onValueChange = { viewModel.onDescriptionChange(it) },
                placeholder = stringResource(R.string.field_description_placeholder),
                minHeight = 150.dp,
                singleLine = false
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Category Picker Bottom Sheet
        if (showCategorySheet) {
            val filteredCategories = remember(categorySearchQuery) {
                ItemCategory.entries.filter {
                    it.name.replace("_", " ").contains(categorySearchQuery.trim(), ignoreCase = true)
                }
            }

            AppBottomSheet(
                onDismissRequest = { showCategorySheet = false },
                heightFraction = 0.65f,
                containerColor = themeColors.cardSurface,
                contentColor = themeColors.textPrimary
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                ) {
                    Text(
                        text = stringResource(R.string.field_category_placeholder),
                        fontFamily = MatterFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = themeColors.textPrimary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = categorySearchQuery,
                        onValueChange = { categorySearchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = stringResource(R.string.search_placeholder),
                                fontFamily = MatterFontFamily,
                                color = themeColors.textSubtle
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = null,
                                tint = themeColors.textSubtle
                            )
                        },
                        trailingIcon = {
                            if (categorySearchQuery.isNotEmpty()) {
                                IconButton(onClick = { categorySearchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = themeColors.textSubtle,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = themeColors.textPrimary,
                            unfocusedBorderColor = themeColors.btnBackBg,
                            focusedContainerColor = themeColors.btnBackBg,
                            unfocusedContainerColor = themeColors.btnBackBg,
                            focusedTextColor = themeColors.textPrimary,
                            unfocusedTextColor = themeColors.textPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val trimmedQuery = categorySearchQuery.trim()
                    val hasExactMatch = filteredCategories.any {
                        it.name.replace("_", " ").equals(trimmedQuery, ignoreCase = true)
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Custom text category addition if not present
                        if (trimmedQuery.isNotEmpty() && !hasExactMatch) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(themeColors.btnBackBg)
                                        .clickable {
                                            viewModel.onCategoryChange(ItemCategory.OTHER)
                                            showCategorySheet = false
                                        }
                                        .padding(vertical = 14.dp, horizontal = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add custom category",
                                        tint = themeColors.textPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Add \"$trimmedQuery\"",
                                        fontFamily = MatterFontFamily,
                                        fontSize = 14.sp,
                                        color = themeColors.textPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

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
                                    fontFamily = MatterFontFamily,
                                    fontSize = 14.sp,
                                    color = if (isSelected) themeColors.textPrimary else themeColors.textSecondary,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = themeColors.textPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            HorizontalDivider(thickness = 0.8.dp, color = PaletteDark.Gray700)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Uploaded Photos 164x164 card with HorizontalPager carousel, dot indicator,
 * full-bleed image display with no white margins, and cancel / cross button to remove individual images.
 */
@Composable
private fun UploadedPhotosCard(
    images: List<Uri>,
    onImageClick: (Uri) -> Unit,
    onRemoveImage: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(25.dp))
            .background(CardDarkSurface)
    ) {
        if (images.isEmpty()) {
            // Default placeholder image
            AsyncImage(
                model = "https://images.unsplash.com/photo-1542291026-7eec264c27ff?q=80&w=600",
                contentDescription = "Default Product",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            val pagerState = rememberPagerState(pageCount = { images.size })

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val uri = images[page]

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onImageClick(uri) }
                ) {
                    AsyncImage(
                        model = uri,
                        contentDescription = "Uploaded Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Cancel / Remove Cross Icon
                    Box(
                        modifier = Modifier
                            .padding(10.dp)
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.65f))
                            .clickable { onRemoveImage(page) }
                            .align(Alignment.TopEnd),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove photo",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Dot Indicator for multiple images
            if (images.size > 1) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(images.size) { index ->
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (index == pagerState.currentPage) Color.White else Color.White.copy(alpha = 0.4f))
                        )
                    }
                }
            }
        }
    }
}

/**
 * Saves a captured Camera Bitmap to application cache and returns its local Uri.
 */
private fun saveBitmapToCache(context: android.content.Context, bitmap: android.graphics.Bitmap): Uri? {
    return try {
        val file = java.io.File(context.cacheDir, "camera_upload_${System.currentTimeMillis()}.jpg")
        java.io.FileOutputStream(file).use { out ->
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
        }
        Uri.fromFile(file)
    } catch (e: Exception) {
        null
    }
}

/**
 * List Product Rounded Card Field matching exact Figma spec:
 * Fill: CardDarkSurface #121416, radius 22dp.
 * Label: Matter SemiBold 13sp.
 * Placeholder / Text: Matter Regular 13sp.
 */
@Composable
private fun ListProductCardField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    minHeight: Dp = 74.dp,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    readOnly: Boolean = false,
    onClick: () -> Unit = {}
) {
    val themeColors = UniSwapTheme.colors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .clip(RoundedCornerShape(22.dp))
            .background(themeColors.cardSurface)
            .clickable(enabled = readOnly, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = if (singleLine) Arrangement.Center else Arrangement.Top
    ) {
        Text(
            text = label,
            fontFamily = MatterFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            letterSpacing = (-0.26).sp,
            color = themeColors.textPrimary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (!singleLine) Modifier.weight(1f) else Modifier),
            contentAlignment = if (singleLine) Alignment.CenterStart else Alignment.TopStart
        ) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    letterSpacing = (-0.26).sp,
                    color = themeColors.textSubtle
                )
            }

            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                readOnly = readOnly,
                textStyle = TextStyle(
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.Normal,
                    color = themeColors.textPrimary,
                    fontSize = 13.sp,
                    letterSpacing = (-0.26).sp
                ),
                singleLine = singleLine,
                cursorBrush = SolidColor(themeColors.textPrimary)
            )
        }
    }
}
