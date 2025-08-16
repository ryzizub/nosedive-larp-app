package me.vavra.dive.common.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CenteredLoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LoadingIndicator(
            modifier = Modifier.align(Alignment.Center), polygons = listOf(
                MaterialShapes.Diamond,
                MaterialShapes.Burst,
                MaterialShapes.Pentagon
            )
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun InlinedLoadingIndicator() {
    LoadingIndicator(
        polygons = listOf(
            MaterialShapes.Diamond,
            MaterialShapes.Burst,
            MaterialShapes.Pentagon
        )
    )
}