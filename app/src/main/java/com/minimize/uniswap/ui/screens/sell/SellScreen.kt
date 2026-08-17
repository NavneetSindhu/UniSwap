package com.minimize.uniswap.ui.screens.sell

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.minimize.uniswap.data.model.ItemCategory
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SellScreen(
    onPostSuccess: () -> Unit,
    viewModel: SellViewModel = viewModel()
) {
    val title by viewModel.title.collectAsState()
    val price by viewModel.price.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedImages by viewModel.selectedImages.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val isPosting by viewModel.isPosting.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris -> viewModel.onImagesSelected(uris) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Button(
                onClick = { viewModel.postItem(onPostSuccess) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .height(56.dp)
                    .navigationBarsPadding(),
                shape = RoundedCornerShape(16.dp),
                enabled = !isPosting && title.isNotEmpty()
            ) {
                if (isPosting) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text("List Item Now", fontWeight = FontWeight.Bold)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // HEADER AREA
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("New Listing", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                    Text("Details & Media", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                }

                // COMPACT AI BUTTON
                IconButton(
                    onClick = { viewModel.performAIScan() },
                    enabled = selectedImages.isNotEmpty() && !isScanning,
                    modifier = Modifier
                        .background(
                            if (selectedImages.isNotEmpty()) Color(0xFFE9C46A).copy(alpha = 0.2f)
                            else Color.Gray.copy(alpha = 0.1f),
                            CircleShape
                        )
                ) {
                    if (isScanning) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    else Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = "AI Scan",
                        tint = if (selectedImages.isNotEmpty()) Color(0xFFD4A017) else Color.Gray
                    )
                }
            }

            // MULTI-IMAGE UPLOAD SECTION (Horizontal)
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.height(120.dp)
            ) {
                // Add Button
                item {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                            .clickable { launcher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }

                // Selected Images
                items(selectedImages) { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(20.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Column(modifier = Modifier.padding(24.dp)) {
                // IMPROVED CATEGORY GRID
                Text("CATEGORY", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
                FlowRow(
                    modifier = Modifier.padding(top = 12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    maxItemsInEachRow = 3
                ) {
                    ItemCategory.entries.forEach { category ->
                        val isSelected = selectedCategory == category
                        Surface(
                            modifier = Modifier.clickable { viewModel.onCategoryChange(category) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            border = if (!isSelected) BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f)) else null
                        ) {
                            Text(
                                text = category.name.lowercase().capitalize(),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // COMPACT FIELDS
                Text("TITLE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
                TextField(
                    value = title,
                    onValueChange = { viewModel.onTitleChange(it) },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    placeholder = { Text("What are you selling?") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text("PRICE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
                TextField(
                    value = price,
                    onValueChange = { viewModel.onPriceChange(it) },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    prefix = { Text("$", fontWeight = FontWeight.Bold) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )
            }
        }
    }
}
