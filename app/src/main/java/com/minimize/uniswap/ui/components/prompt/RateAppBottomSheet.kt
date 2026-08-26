package com.minimize.uniswap.ui.components.prompt

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimize.uniswap.R
import com.minimize.uniswap.ui.components.AppBottomSheet
import com.minimize.uniswap.ui.components.LocalBottomSheetDismiss
import com.minimize.uniswap.ui.theme.MatterFontFamily
import com.minimize.uniswap.ui.theme.UniSwapTheme

/**
 * Modern In-App Rating & Review Bottom Sheet.
 * Features 5-star interactive rating, feedback topic tags, optional notes, and smooth dismissal.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RateAppBottomSheet(
    onDismissRequest: () -> Unit,
    onSubmitFeedback: (rating: Int, tags: List<String>, feedback: String) -> Unit = { _, _, _ -> },
    onRateOnPlayStore: () -> Unit = {}
) {
    val themeColors = UniSwapTheme.colors
    var selectedRating by remember { mutableIntStateOf(5) }
    val selectedTags = remember { mutableStateListOf<String>() }
    var feedbackNotes by remember { mutableStateOf("") }

    val feedbackTags = listOf(
        stringResource(R.string.review_tag_fast_trades),
        stringResource(R.string.review_tag_safe_pickups),
        stringResource(R.string.review_tag_great_ui),
        stringResource(R.string.review_tag_student_community),
        stringResource(R.string.review_tag_bug_report)
    )

    AppBottomSheet(
        onDismissRequest = onDismissRequest,
        heightFraction = 0.85f,
        containerColor = themeColors.cardSurface,
        contentColor = themeColors.textPrimary
    ) {
        val dismissSheet = LocalBottomSheetDismiss.current

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Star Icon Badge
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(themeColors.campusAmber.copy(alpha = 0.15f))
                    .border(1.dp, themeColors.campusAmber.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = themeColors.campusAmber,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = stringResource(R.string.review_sheet_title),
                fontFamily = MatterFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = themeColors.textPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.review_sheet_subtitle),
                fontFamily = MatterFontFamily,
                fontSize = 13.sp,
                color = themeColors.textSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 5-Star Interactive Rating Bar
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (star in 1..5) {
                    val isSelected = star <= selectedRating
                    val scale by animateFloatAsState(
                        targetValue = if (star == selectedRating) 1.25f else 1.0f,
                        animationSpec = spring(dampingRatio = 0.6f),
                        label = "star_scale_$star"
                    )

                    IconButton(
                        onClick = { selectedRating = star },
                        modifier = Modifier
                            .size(44.dp)
                            .scale(scale)
                    ) {
                        Icon(
                            imageVector = if (isSelected) Icons.Default.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Star $star",
                            tint = if (isSelected) themeColors.campusAmber else themeColors.divider,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Feedback Tag Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(feedbackTags) { tag ->
                    val isTagSelected = selectedTags.contains(tag)
                    val bg by animateColorAsState(
                        if (isTagSelected) themeColors.textPrimary else themeColors.cardBackground,
                        animationSpec = tween(200),
                        label = "tag_bg_$tag"
                    )
                    val textColor by animateColorAsState(
                        if (isTagSelected) themeColors.background else themeColors.textSecondary,
                        animationSpec = tween(200),
                        label = "tag_text_$tag"
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(bg)
                            .border(
                                1.dp,
                                if (isTagSelected) Color.Transparent else themeColors.divider,
                                RoundedCornerShape(50.dp)
                            )
                            .clickable {
                                if (isTagSelected) selectedTags.remove(tag) else selectedTags.add(tag)
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = tag,
                            fontFamily = MatterFontFamily,
                            fontSize = 12.sp,
                            fontWeight = if (isTagSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = textColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Optional Feedback Notes
            OutlinedTextField(
                value = feedbackNotes,
                onValueChange = { feedbackNotes = it },
                placeholder = {
                    Text(
                        text = stringResource(R.string.review_feedback_placeholder),
                        fontFamily = MatterFontFamily,
                        fontSize = 13.sp,
                        color = themeColors.textSubtle
                    )
                },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = themeColors.wasteMetricGreen,
                    unfocusedBorderColor = themeColors.divider,
                    focusedContainerColor = themeColors.cardBackground,
                    unfocusedContainerColor = themeColors.cardBackground
                ),
                maxLines = 3,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Submit / Rate Button
            Button(
                onClick = {
                    if (selectedRating >= 4) {
                        onRateOnPlayStore()
                    }
                    onSubmitFeedback(selectedRating, selectedTags.toList(), feedbackNotes)
                    dismissSheet()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = themeColors.textPrimary,
                    contentColor = themeColors.background
                )
            ) {
                Text(
                    text = if (selectedRating >= 4) {
                        stringResource(R.string.review_playstore_btn)
                    } else {
                        stringResource(R.string.review_submit_btn)
                    },
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            TextButton(
                onClick = { dismissSheet() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.review_later_btn),
                    fontFamily = MatterFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = themeColors.textSecondary
                )
            }
        }
    }
}
