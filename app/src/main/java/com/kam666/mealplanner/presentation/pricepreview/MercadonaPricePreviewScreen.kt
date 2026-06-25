package com.kam666.mealplanner.presentation.pricepreview

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.kam666.mealplanner.R
import com.kam666.mealplanner.domain.model.MercadonaProduct
import com.kam666.mealplanner.domain.model.PriceMatchResult
import com.kam666.mealplanner.presentation.theme.AppMuted
import com.kam666.mealplanner.presentation.theme.CoralPrimary
import com.kam666.mealplanner.presentation.theme.CoralSecondary
import kotlin.math.ceil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MercadonaPricePreviewScreen(
    onBack: () -> Unit,
    viewModel: MercadonaPricePreviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val moreProductsState by viewModel.moreProductsState.collectAsStateWithLifecycle()
    var swapTarget by remember { mutableStateOf<PriceMatchResult.Matched?>(null) }

    swapTarget?.let { target ->
        ProductSwapSheet(
            result = target,
            moreProductsState = moreProductsState,
            onSelect = { newProduct ->
                viewModel.swapProduct(target, newProduct)
                viewModel.clearMoreProducts()
                swapTarget = null
            },
            onLoadMore = { viewModel.loadMoreProducts(target) },
            onDismiss = {
                viewModel.clearMoreProducts()
                swapTarget = null
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.screen_price_preview_title),
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is MercadonaPreviewUiState.Loading -> LoadingContent(Modifier.padding(padding))
            is MercadonaPreviewUiState.Error   -> ErrorContent(state.message, viewModel::load, Modifier.padding(padding))
            is MercadonaPreviewUiState.Success -> SuccessContent(
                state = state,
                onChangeProduct = { swapTarget = it },
                onAdjustPackages = { matched, delta -> viewModel.adjustPackages(matched, delta) },
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductSwapSheet(
    result: PriceMatchResult.Matched,
    moreProductsState: MoreProductsState,
    onSelect: (MercadonaProduct) -> Unit,
    onLoadMore: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                "Elige otro producto",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            val allProducts = when (moreProductsState) {
                is MoreProductsState.Loaded -> result.alternatives + moreProductsState.products
                else -> result.alternatives
            }

            if (allProducts.isEmpty() && moreProductsState !is MoreProductsState.Loading) {
                Text(
                    "No hay alternativas disponibles",
                    fontSize = 13.sp,
                    color = AppMuted,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            } else {
                allProducts.forEach { product ->
                    val packs = ceil(result.requiredInBulkUnit / product.unitSize.coerceAtLeast(0.001)).toInt().coerceAtLeast(1)
                    val cost = packs * product.unitPrice
                    AlternativeProductRow(
                        product = product,
                        packagesNeeded = packs,
                        estimatedCost = cost,
                        onClick = { onSelect(product) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                }
            }

            when (moreProductsState) {
                is MoreProductsState.Idle, is MoreProductsState.Error -> {
                    TextButton(
                        onClick = onLoadMore,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            if (moreProductsState is MoreProductsState.Error) "Reintentar búsqueda" else "Ver más productos",
                            color = CoralPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                is MoreProductsState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = CoralPrimary, modifier = Modifier.size(24.dp))
                    }
                }
                is MoreProductsState.Loaded -> Unit
            }
        }
    }
}

@Composable
private fun AlternativeProductRow(
    product: MercadonaProduct,
    packagesNeeded: Int,
    estimatedCost: Double,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AsyncImage(
            model = product.thumbnailUrl,
            contentDescription = product.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.background)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                product.name,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (product.packaging.isNotBlank()) {
                Text(product.packaging, fontSize = 11.sp, color = AppMuted)
            }
            Text(
                "${packagesNeeded}x ${formatPrice(product.unitPrice)}/ud",
                fontSize = 11.sp,
                color = AppMuted
            )
        }
        Text(
            "~${formatPrice(estimatedCost)}",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 15.sp,
            color = CoralPrimary
        )
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            CircularProgressIndicator(color = CoralPrimary)
            Text(
                stringResource(R.string.price_preview_loading),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Text("\u26A0\uFE0F", fontSize = 48.sp)
            Text(
                stringResource(R.string.price_preview_error),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 17.sp,
                textAlign = TextAlign.Center
            )
            Text(
                message,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onRetry,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
            ) {
                Text(stringResource(R.string.price_preview_retry), fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun SuccessContent(
    state: MercadonaPreviewUiState.Success,
    onChangeProduct: (PriceMatchResult.Matched) -> Unit,
    onAdjustPackages: (PriceMatchResult.Matched, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            SummaryCard(state, Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        }
        if (state.hasApproximations) {
            item {
                ApproxWarning(Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
            }
        }
        items(state.results, key = { result ->
            when (result) {
                is PriceMatchResult.Matched -> "matched_${result.shoppingItem.ingredient.id}"
                is PriceMatchResult.NoMatch -> "nomatch_${result.shoppingItem.ingredient.id}"
            }
        }) { result ->
            PriceMatchRow(
                result = result,
                onChangeProduct = onChangeProduct,
                onAdjustPackages = onAdjustPackages,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }
        item {
            TotalCostCard(state.totalEstimatedCost, Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
        }
    }
}

@Composable
private fun SummaryCard(state: MercadonaPreviewUiState.Success, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(Icons.Filled.ShoppingCart, contentDescription = null, tint = CoralPrimary, modifier = Modifier.size(22.dp))
        Text(
            stringResource(R.string.price_preview_summary, state.matchedCount, state.noMatchCount),
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ApproxWarning(modifier: Modifier = Modifier) {
    Text(
        "\u26A0\uFE0F ${stringResource(R.string.price_preview_approx_warning)}",
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSecondaryContainer
    )
}

@Composable
private fun PriceMatchRow(
    result: PriceMatchResult,
    onChangeProduct: (PriceMatchResult.Matched) -> Unit,
    onAdjustPackages: (PriceMatchResult.Matched, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    when (result) {
        is PriceMatchResult.Matched -> MatchedRow(result, onChangeProduct, onAdjustPackages, modifier)
        is PriceMatchResult.NoMatch -> NoMatchRow(result, modifier)
    }
}

@Composable
private fun MatchedRow(
    result: PriceMatchResult.Matched,
    onChangeProduct: (PriceMatchResult.Matched) -> Unit,
    onAdjustPackages: (PriceMatchResult.Matched, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AsyncImage(
                model = result.product.thumbnailUrl,
                contentDescription = result.product.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.background)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    result.product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (result.product.packaging.isNotBlank()) {
                    Text(
                        result.product.packaging,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    formatPrice(result.product.unitPrice),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = CoralPrimary
                )
                Text(
                    "~${formatPrice(result.estimatedCost)}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Stepper row for adjusting pack quantity
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = { onAdjustPackages(result, -1) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Filled.Remove,
                        contentDescription = "Reducir",
                        tint = CoralPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    "${result.packagesNeeded} ud",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                IconButton(
                    onClick = { onAdjustPackages(result, +1) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "Aumentar",
                        tint = CoralPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            TextButton(
                onClick = { onChangeProduct(result) },
                contentPadding = PaddingValues(horizontal = 0.dp, vertical = 2.dp)
            ) {
                Text(
                    "Cambiar producto",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = CoralPrimary
                )
            }
        }
    }
}

@Composable
private fun NoMatchRow(result: PriceMatchResult.NoMatch, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Text("\uD83D\uDD0D", fontSize = 24.sp)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                result.shoppingItem.ingredient.name,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                stringResource(R.string.price_preview_no_match),
                fontSize = 12.sp,
                color = AppMuted
            )
        }
        Text("\u2014", color = AppMuted, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun TotalCostCard(total: Double, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(brush = Brush.horizontalGradient(listOf(CoralPrimary, CoralSecondary)))
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.price_preview_total),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                "~${formatPrice(total)}",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 26.sp
            )
        }
        Text(
            stringResource(R.string.price_preview_disclaimer),
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

private fun formatPrice(price: Double): String = "%.2f\u20AC".format(price)
