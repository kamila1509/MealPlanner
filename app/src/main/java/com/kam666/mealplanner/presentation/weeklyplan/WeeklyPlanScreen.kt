package com.kam666.mealplanner.presentation.weeklyplan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kam666.mealplanner.R
import com.kam666.mealplanner.domain.model.MealType
import com.kam666.mealplanner.presentation.common.AppLanguage
import com.kam666.mealplanner.presentation.common.RecipeVisuals
import com.kam666.mealplanner.presentation.common.ScreenHeader
import com.kam666.mealplanner.presentation.common.ShoppingBanner
import com.kam666.mealplanner.presentation.common.localizedName
import com.kam666.mealplanner.presentation.theme.CoralPrimaryDark
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun WeeklyPlanScreen(
    language: AppLanguage,
    onGoShopping: () -> Unit,
    viewModel: WeeklyPlanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val allRecipes by viewModel.allRecipes.collectAsStateWithLifecycle()
    val mealCount by viewModel.mealCount.collectAsStateWithLifecycle()
    val pendingRecipeId by viewModel.pendingRecipeId.collectAsStateWithLifecycle()

    var slotDialog by remember { mutableStateOf<Pair<java.time.LocalDate, MealType>?>(null) }
    var existingSlot by remember { mutableStateOf<MealSlotUiModel?>(null) }

    val pendingRecipe = pendingRecipeId?.let { id -> allRecipes.find { it.id == id } }

    slotDialog?.let { (date, mealType) ->
        MealSlotDialog(
            date = date,
            mealType = mealType,
            existing = existingSlot,
            recipes = allRecipes,
            language = language,
            onConfirm = { recipe, peopleCount ->
                viewModel.setEntry(date, mealType, recipe, peopleCount)
                slotDialog = null
            },
            onDelete = {
                viewModel.deleteEntry(date, mealType)
                slotDialog = null
            },
            onDismiss = { slotDialog = null }
        )
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            item {
                ScreenHeader(
                    brand = stringResource(R.string.this_week),
                    title = stringResource(R.string.title_weekly_plan)
                )
                WeekHeader(
                    label = viewModel.weekLabel(),
                    onPrev = viewModel::prevWeek,
                    onNext = viewModel::nextWeek,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                OutlinedButton(
                    onClick = viewModel::randomFillWeek,
                    enabled = allRecipes.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(13.dp)
                ) {
                    Text(stringResource(R.string.action_random_fill), fontWeight = FontWeight.Bold)
                }
            }
            item {
                ShoppingBanner(
                    title = stringResource(R.string.meals_planned, mealCount),
                    subtitle = stringResource(R.string.tap_for_list),
                    onClick = onGoShopping,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            if (pendingRecipe != null) {
                item {
                    PendingBanner(
                        emoji = RecipeVisuals.emoji(pendingRecipe),
                        onCancel = viewModel::cancelPending,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
            items(uiState.days) { day ->
                DayCard(
                    day = day,
                    language = language,
                    onSlotClick = { mealType, slot ->
                        if (pendingRecipe != null && slot == null) {
                            viewModel.setEntry(day.date, mealType, pendingRecipe, pendingRecipe.servings)
                        } else {
                            existingSlot = slot
                            slotDialog = Pair(day.date, mealType)
                        }
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun PendingBanner(emoji: String, onCancel: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(emoji, fontSize = 22.sp)
        Column(modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.adding_meal), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = CoralPrimaryDark)
            Text(stringResource(R.string.tap_slot), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        TextButton(onClick = onCancel) {
            Text(stringResource(R.string.action_cancel), fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun WeekHeader(label: String, onPrev: () -> Unit, onNext: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrev) {
            Text("‹", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
        Text(label, style = MaterialTheme.typography.titleMedium)
        IconButton(onClick = onNext) {
            Text("›", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DayCard(
    day: DayPlanUiModel,
    language: AppLanguage,
    onSlotClick: (MealType, MealSlotUiModel?) -> Unit,
    modifier: Modifier = Modifier
) {
    val locale = Locale.forLanguageTag(language.tag)
    val dayFmt = DateTimeFormatter.ofPattern("EEEE", locale)
    val shortFmt = DateTimeFormatter.ofPattern("d MMM", locale)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 15.dp, vertical = 14.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(day.date.format(dayFmt).replaceFirstChar { it.uppercase(locale) }, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            Text(day.date.format(shortFmt), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(10.dp))
        MealRow(MealType.ALMUERZO, day.almuerzo, onSlotClick)
        Spacer(Modifier.height(9.dp))
        MealRow(MealType.CENA, day.cena, onSlotClick)
    }
}

@Composable
private fun MealRow(mealType: MealType, slot: MealSlotUiModel?, onSlotClick: (MealType, MealSlotUiModel?) -> Unit) {
    val typeEmoji = if (mealType == MealType.ALMUERZO) "🍽️" else "🌙"
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            "$typeEmoji ${mealType.localizedName()}",
            modifier = Modifier.width(74.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (slot != null) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(13.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.4.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(13.dp))
                    .clickable { onSlotClick(mealType, slot) }
                    .padding(horizontal = 9.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(RecipeVisuals.emoji(slot.recipe), fontSize = 20.sp)
                Text(
                    slot.recipe.name,
                    modifier = Modifier.weight(1f),
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text("👤${slot.peopleCount}", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
            }
        } else {
            OutlinedButton(
                onClick = { onSlotClick(mealType, null) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(13.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.6.dp)
            ) {
                Text("+ ${stringResource(R.string.action_add)}", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
            }
        }
    }
}
