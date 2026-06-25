package com.kam666.mealplanner.presentation.shoppinglist

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kam666.mealplanner.R
import com.kam666.mealplanner.domain.model.ShoppingListItem
import com.kam666.mealplanner.presentation.common.*
import com.kam666.mealplanner.presentation.theme.CoralPrimary
import com.kam666.mealplanner.presentation.theme.CoralSecondary
import com.kam666.mealplanner.presentation.weeklyplan.WeekHeader

@Composable
fun ShoppingListScreen(
    onGoPlan: () -> Unit,
    onViewPrices: () -> Unit = {},
    viewModel: ShoppingListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val checkedItems by viewModel.checkedItems.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val progress = viewModel.progressPercent(checkedItems)
    val shareLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            item {
                ScreenHeader(
                    brand = stringResource(R.string.auto_gen),
                    title = stringResource(R.string.title_shopping_list)
                )
                WeekHeader(
                    label = viewModel.weekLabel(),
                    onPrev = viewModel::prevWeek,
                    onNext = viewModel::nextWeek,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }

            if (uiState.mealCount == 0) {
                item {
                    EmptyShoppingState(onGoPlan = onGoPlan, modifier = Modifier.padding(40.dp))
                }
            } else {
                item {
                    ProgressCard(
                        progress = progress,
                        checked = checkedItems.size,
                        total = uiState.itemCount,
                        itemCount = uiState.itemCount,
                        recipeCount = uiState.recipeCount,
                        mealCount = uiState.mealCount,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                items(uiState.groups, key = { it.key }) { group ->
                    ShoppingGroupSection(
                        group = group,
                        checkedItems = checkedItems,
                        onToggle = viewModel::toggleItem,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        OutlinedButton(
                            onClick = {
                                val title = context.getString(R.string.title_shopping_list)
                                val text = buildShareText(uiState, checkedItems, title, context)
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, text)
                                }
                                shareLauncher.launch(Intent.createChooser(sendIntent, null))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp)
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.8.dp)
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = null, tint = CoralPrimary)
                            Spacer(Modifier.width(9.dp))
                            Text(stringResource(R.string.share_list), fontWeight = FontWeight.ExtraBold, color = CoralPrimary)
                        }
                        OutlinedButton(
                            onClick = onViewPrices,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp, bottom = 20.dp)
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.8.dp)
                        ) {
                            Text("🏪", fontSize = 18.sp)
                            Spacer(Modifier.width(9.dp))
                            Text(stringResource(R.string.price_preview_button), fontWeight = FontWeight.ExtraBold, color = CoralPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyShoppingState(onGoPlan: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text("🛒", fontSize = 42.sp)
        }
        Text(stringResource(R.string.empty_shopping_title), fontWeight = FontWeight.ExtraBold, fontSize = 19.sp)
        Text(
            stringResource(R.string.empty_shopping_body),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 21.sp
        )
        Button(onClick = onGoPlan, shape = RoundedCornerShape(14.dp)) {
            Text(stringResource(R.string.go_plan), fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun ProgressCard(
    progress: Int,
    checked: Int,
    total: Int,
    itemCount: Int,
    recipeCount: Int,
    mealCount: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.shopping_progress, checked, total), fontWeight = FontWeight.ExtraBold)
            Text("$progress%", fontWeight = FontWeight.ExtraBold, color = CoralPrimary)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
                .height(9.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(MaterialTheme.colorScheme.background)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress / 100f)
                    .background(brush = androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(CoralPrimary, CoralSecondary)))
            )
        }
        Row(Modifier.fillMaxWidth().padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatBox("$itemCount", stringResource(R.string.stat_items))
            StatBox("$recipeCount", stringResource(R.string.stat_recipes))
            StatBox("$mealCount", stringResource(R.string.stat_meals))
        }
    }
}

@Composable
private fun RowScope.StatBox(value: String, label: String) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(13.dp))
            .background(MaterialTheme.colorScheme.background)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ShoppingGroupSection(
    group: ShoppingGroup,
    checkedItems: Set<Long>,
    onToggle: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(group.emoji, fontSize = 18.sp)
            Text(stringResource(group.labelRes).uppercase(), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
            Text("· ${group.items.size}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 14.dp, vertical = 4.dp)
        ) {
            group.items.forEachIndexed { index, item ->
                ShoppingItemRow(
                    item = item,
                    checked = item.ingredient.id in checkedItems,
                    onToggle = { onToggle(item.ingredient.id) }
                )
                if (index < group.items.lastIndex) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                }
            }
        }
    }
}

@Composable
private fun ShoppingItemRow(item: ShoppingListItem, checked: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(8.dp))
                .then(
                    if (checked) Modifier.background(CoralPrimary)
                    else Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                ),
            contentAlignment = Alignment.Center
        ) {
            if (checked) Text("✓", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
        Text(
            item.ingredient.name,
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None,
            color = if (checked) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f) else MaterialTheme.colorScheme.onSurface
        )
        Text(
            "${formatQuantity(item.totalQuantity)} ${item.ingredient.unit.localizedName()}",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 14.sp,
            color = if (checked) CoralPrimary.copy(alpha = 0.4f) else CoralPrimary
        )
    }
}

private fun formatQuantity(qty: Double): String =
    if (qty == qty.toLong().toDouble()) qty.toLong().toString() else "%.2f".format(qty)

private fun buildShareText(
    uiState: ShoppingListUiState,
    checked: Set<Long>,
    title: String,
    context: android.content.Context
): String {
    val sb = StringBuilder("🛒 $title\n\n")
    uiState.groups.forEach { group ->
        sb.append("${group.emoji} ${context.getString(group.labelRes)}\n")
        group.items.forEach { item ->
            val mark = if (item.ingredient.id in checked) "✓ " else "- "
            sb.append("$mark${item.ingredient.name} ${formatQuantity(item.totalQuantity)} ${context.getString(item.ingredient.unit.labelRes())}\n")
        }
        sb.append("\n")
    }
    return sb.toString()
}
