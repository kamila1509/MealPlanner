package com.kam666.mealplanner.presentation.weeklyplan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kam666.mealplanner.R
import com.kam666.mealplanner.domain.model.MealType
import com.kam666.mealplanner.domain.model.Recipe
import com.kam666.mealplanner.presentation.common.AppLanguage
import com.kam666.mealplanner.presentation.common.RecipeVisuals
import com.kam666.mealplanner.presentation.common.localizedName
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealSlotDialog(
    date: LocalDate,
    mealType: MealType,
    existing: MealSlotUiModel?,
    recipes: List<Recipe>,
    language: AppLanguage,
    onConfirm: (Recipe, Int) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedRecipe by remember { mutableStateOf(existing?.recipe) }
    var peopleCount by remember { mutableIntStateOf(existing?.peopleCount ?: 2) }
    var searchQuery by remember { mutableStateOf("") }

    val filtered = recipes.filter {
        searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true)
    }

    val locale = Locale.forLanguageTag(language.tag)
    val dayFmt = DateTimeFormatter.ofPattern("EEEE d MMM", locale)

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    stringResource(R.string.choose_recipe),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                Text(
                    "${mealType.localizedName()} · ${date.format(dayFmt).replaceFirstChar { it.uppercase(locale) }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text(stringResource(R.string.label_search_recipe)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
                Spacer(Modifier.height(4.dp))
            }

            LazyColumn(
                modifier = Modifier.heightIn(max = 320.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
            ) {
                items(filtered, key = { it.id }) { recipe ->
                    RecipePickerRow(
                        recipe = recipe,
                        selected = selectedRecipe?.id == recipe.id,
                        ingredientCountLabel = stringResource(R.string.ingredient_count_short, recipe.ingredients.size),
                        onClick = { selectedRecipe = recipe }
                    )
                }
                if (filtered.isEmpty()) {
                    item {
                        Text(
                            stringResource(R.string.no_recipes),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 12.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Spacer(Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(stringResource(R.string.label_people), style = MaterialTheme.typography.bodyMedium)
                    IconButton(onClick = { if (peopleCount > 1) peopleCount-- }) {
                        Icon(Icons.Filled.Remove, contentDescription = stringResource(R.string.cd_reduce))
                    }
                    Text("$peopleCount", style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = { peopleCount++ }) {
                        Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.cd_increase))
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (existing != null) {
                        OutlinedButton(
                            onClick = onDelete,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) { Text(stringResource(R.string.action_remove)) }
                    }
                    Button(
                        onClick = { selectedRecipe?.let { onConfirm(it, peopleCount) } },
                        enabled = selectedRecipe != null,
                        modifier = Modifier.weight(1f)
                    ) { Text(stringResource(R.string.action_confirm)) }
                }
            }
        }
    }
}

@Composable
private fun RecipePickerRow(
    recipe: Recipe,
    selected: Boolean,
    ingredientCountLabel: String,
    onClick: () -> Unit
) {
    val categoryLabel = "${RecipeVisuals.categoryEmoji(recipe.category)} ${recipe.category.localizedName()}"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(RecipeVisuals.tint(recipe)),
            contentAlignment = Alignment.Center
        ) {
            Text(RecipeVisuals.emoji(recipe), fontSize = 26.sp)
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                recipe.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "$categoryLabel · $ingredientCountLabel",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Icon(
            imageVector = if (selected) Icons.Filled.Check else Icons.Filled.Add,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
    }
}
