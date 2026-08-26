package com.minimize.uniswap.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * CompositionLocal providing an animated slide-down dismissal trigger for any child inside AppBottomSheet.
 */
val LocalBottomSheetDismiss = staticCompositionLocalOf<() -> Unit> { {} }

/**
 * Reusable Bottom Sheet supporting both fixed height bounds and wrap-content layouts.
 * Provides unified smooth slide-down exit animations via LocalBottomSheetDismiss.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    heightFraction: Float? = 0.65f,
    sheetState: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    ),
    shape: Shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    showCloseIcon: Boolean = true,
    dragHandle: @Composable (() -> Unit)? = { BottomSheetDefaults.DragHandle() },
    content: @Composable ColumnScope.() -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    val dismissWithAnimation: () -> Unit = {
        coroutineScope.launch {
            sheetState.hide()
        }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                onDismissRequest()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = shape,
        containerColor = containerColor,
        contentColor = contentColor,
        dragHandle = null,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        modifier = modifier
    ) {
        CompositionLocalProvider(LocalBottomSheetDismiss provides dismissWithAnimation) {
            Box(
                modifier = if (heightFraction != null) {
                    Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(heightFraction)
                } else {
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                }
            ) {
                Column(
                    modifier = if (heightFraction != null) {
                        Modifier
                            .fillMaxSize()
                            .navigationBarsPadding()
                    } else {
                        Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .navigationBarsPadding()
                    }
                ) {
                    // Header Bar with Drag Handle & Theme-Aware Close Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    ) {
                        if (dragHandle != null) {
                            Box(
                                modifier = Modifier.align(Alignment.Center)
                            ) {
                                dragHandle()
                            }
                        }

                        if (showCloseIcon) {
                            IconButton(
                                onClick = dismissWithAnimation,
                                modifier = Modifier.align(Alignment.CenterEnd)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Sheet",
                                    tint = contentColor.copy(alpha = 0.7f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Slot Content
                    content()
                }
            }
        }
    }
}