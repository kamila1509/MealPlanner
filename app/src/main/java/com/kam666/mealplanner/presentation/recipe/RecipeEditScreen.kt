package com.kam666.mealplanner.presentation.recipe

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kam666.mealplanner.R
import com.kam666.mealplanner.domain.model.Ingredient
import com.kam666.mealplanner.domain.model.IngredientUnit
import com.kam666.mealplanner.domain.model.RecipeCategory
import com.kam666.mealplanner.presentation.common.RecipeVisuals
import com.kam666.mealplanner.presentation.common.SupermarketCategories
import com.kam666.mealplanner.presentation.common.localizedName
import com.kam666.mealplanner.presentation.theme.AppMuted
import com.kam666.mealplanner.presentation.theme.CoralPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeEditScreen(
    onBack: () -> Unit,
    viewModel: RecipeEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.savedEvent) {
        if (uiState.savedEvent) onBack()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            RecipeEditTopBar(
                title = if (viewModel.isNewRecipe) {
                    stringResource(R.string.title_new_recipe)
                } else {
                    stringResource(R.string.title_edit_recipe)
                },
                onClose = onBack,
                onSave = viewModel::save,
                saveEnabled = !uiState.isSaving && !uiState.savedEvent && uiState.name.isNotBlank()
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CoralPrimary)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                EmojiPickerRow(
                    emojis = RecipeVisuals.pickerEmojis,
                    selected = uiState.selectedEmoji,
                    onSelect = viewModel::onEmojiChange
                )
            }

            item {
                FormLabel(stringResource(R.string.label_recipe_name))
                FormTextField(
                    value = uiState.name,
                    onValueChange = viewModel::onNameChange,
                    placeholder = stringResource(R.string.placeholder_recipe_name)
                )
            }

            item {
                FormLabel(stringResource(R.string.label_category))
                CategoryChipRow(
                    selected = uiState.category,
                    onSelect = viewModel::onCategoryChange
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        FormLabel(stringResource(R.string.label_servings))
                        ServingsStepper(
                            servings = uiState.servings,
                            onChange = viewModel::onServingsChange
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        FormLabel(stringResource(R.string.label_prep_time))
                        FormTextField(
                            value = uiState.preparationTimeMinutes?.toString() ?: "",
                            onValueChange = { viewModel.onPrepTimeChange(it.toIntOrNull()) },
                            placeholder = "30",
                            keyboardType = KeyboardType.Number
                        )
                    }
                }
            }

            item {
                FormLabel(stringResource(R.string.label_ingredients))
            }

            items(uiState.ingredients, key = { it.ingredient.id }) { draft ->
                IngredientEditCard(
                    draft = draft,
                    suggestions = uiState.ingredientSuggestions[draft.ingredient.id] ?: emptyList(),
                    exactMatch = uiState.exactMatchWarnings[draft.ingredient.id],
                    onNameChange = { name ->
                        viewModel.onIngredientNameChanged(draft.ingredient.id, name)
                    },
                    onSuggestionSelected = { existing ->
                        viewModel.onSuggestionSelected(draft.ingredient.id, existing)
                    },
                    onQuantityChange = { qty ->
                        viewModel.onUpdateIngredient(draft.ingredient.id) { d ->
                            d.copy(quantity = qty)
                        }
                    },
                    onUnitChange = { unit ->
                        viewModel.onUpdateIngredient(draft.ingredient.id) { d ->
                            d.copy(ingredient = d.ingredient.copy(unit = unit))
                        }
                    },
                    onCategoryChange = { categoryKey ->
                        viewModel.onUpdateIngredient(draft.ingredient.id) { d ->
                            d.copy(ingredient = d.ingredient.copy(supermarketCategory = categoryKey))
                        }
                    },
                    onRemove = { viewModel.onRemoveIngredient(draft.ingredient.id) }
                )
            }

            item {
                DashedAddButton(
                    label = stringResource(R.string.action_add_ingredient),
                    onClick = viewModel::onAddEmptyIngredient
                )
            }

            item {
                FormLabel(stringResource(R.string.label_preparation))
                FormTextField(
                    value = uiState.preparationText,
                    onValueChange = viewModel::onPreparationTextChange,
                    placeholder = stringResource(R.string.placeholder_preparation),
                    singleLine = false,
                    minLines = 5
                )
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun RecipeEditTopBar(
    title: String,
    onClose: () -> Unit,
    onSave: () -> Unit,
    saveEnabled: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onClose) {
            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.action_cancel))
        }
        Text(
            title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Button(
            onClick = onSave,
            enabled = saveEnabled,
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CoralPrimary,
                disabledContainerColor = CoralPrimary.copy(alpha = 0.4f)
            ),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                stringResource(R.string.action_save),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun EmojiPickerRow(
    emojis: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(emojis) { emoji ->
            val isSelected = emoji == selected
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        width = if (isSelected) 2.5.dp else 0.dp,
                        color = if (isSelected) CoralPrimary else Color.Transparent,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { onSelect(emoji) },
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 28.sp)
            }
        }
    }
}

@Composable
private fun FormLabel(text: String) {
    Text(
        text,
        modifier = Modifier.padding(bottom = 8.dp),
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
private fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
        },
        modifier = Modifier.fillMaxWidth(),
        singleLine = singleLine,
        minLines = minLines,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            disabledContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
}

@Composable
private fun CategoryChipRow(
    selected: RecipeCategory,
    onSelect: (RecipeCategory) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(RecipeCategory.entries) { category ->
            val isSelected = category == selected
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(category) },
                label = {
                    Text(
                        "${RecipeVisuals.categoryEmoji(category)} ${category.localizedName()}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CoralPrimary,
                    selectedLabelColor = Color.White,
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                border = null,
                shape = RoundedCornerShape(999.dp)
            )
        }
    }
}

@Composable
private fun ServingsStepper(servings: Int, onChange: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StepperButton(onClick = { onChange((servings - 1).coerceAtLeast(1)) }) {
            Icon(Icons.Filled.Remove, contentDescription = stringResource(R.string.cd_reduce), tint = Color.White)
        }
        Text(
            "$servings",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold
        )
        StepperButton(onClick = { onChange((servings + 1).coerceAtMost(20)) }) {
            Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.cd_increase), tint = Color.White)
        }
    }
}

@Composable
private fun StepperButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    FilledIconButton(
        onClick = onClick,
        modifier = Modifier.size(36.dp),
        shape = RoundedCornerShape(12.dp),
        colors = IconButtonDefaults.filledIconButtonColors(containerColor = CoralPrimary)
    ) {
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IngredientEditCard(
    draft: IngredientDraft,
    suggestions: List<Ingredient> = emptyList(),
    exactMatch: Ingredient? = null,
    onNameChange: (String) -> Unit,
    onSuggestionSelected: (Ingredient) -> Unit = {},
    onQuantityChange: (Double) -> Unit,
    onUnitChange: (IngredientUnit) -> Unit,
    onCategoryChange: (String) -> Unit,
    onRemove: () -> Unit
) {
    var unitExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    val supermarket = SupermarketCategories.forKey(draft.ingredient.supermarketCategory)
    var quantityText by remember(draft.ingredient.id) {
        mutableStateOf(formatQuantity(draft.quantity))
    }

    LaunchedEffect(draft.quantity) {
        quantityText = formatQuantity(draft.quantity)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = draft.ingredient.name,
                onValueChange = onNameChange,
                placeholder = { Text(stringResource(R.string.label_ingredient)) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = ingredientFieldColors()
            )
            IconButton(onClick = onRemove, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = stringResource(R.string.cd_remove),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        exactMatch?.let { existing ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(CoralPrimary.copy(alpha = 0.08f))
                    .clickable { onSuggestionSelected(existing) }
                    .padding(horizontal = 10.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Outlined.Info,
                    contentDescription = null,
                    tint = CoralPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    "Ya existe \"${existing.name}\" (${existing.unit.displayName}). Toca para reutilizarlo.",
                    style = MaterialTheme.typography.labelSmall,
                    color = CoralPrimary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (suggestions.isNotEmpty()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "¿Quizás quisiste decir?",
                    style = MaterialTheme.typography.labelSmall,
                    color = AppMuted,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(suggestions) { ingredient ->
                        SuggestionChip(
                            onClick = { onSuggestionSelected(ingredient) },
                            label = {
                                Text(
                                    "${ingredient.name} (${ingredient.unit.displayName})",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        )
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = quantityText,
                onValueChange = { text ->
                    quantityText = text
                    text.toDoubleOrNull()?.let(onQuantityChange)
                },
                modifier = Modifier.weight(0.8f),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = ingredientFieldColors(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            ExposedDropdownMenuBox(
                expanded = unitExpanded,
                onExpandedChange = { unitExpanded = it },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = draft.ingredient.unit.localizedName(),
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ingredientFieldColors(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) }
                )
                ExposedDropdownMenu(expanded = unitExpanded, onDismissRequest = { unitExpanded = false }) {
                    IngredientUnit.entries.forEach { unit ->
                        DropdownMenuItem(
                            text = { Text(unit.localizedName()) },
                            onClick = { onUnitChange(unit); unitExpanded = false }
                        )
                    }
                }
            }


        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = it },
                modifier = Modifier.weight(1.2f)
            ) {
                OutlinedTextField(
                    value = stringResource(supermarket.labelRes),
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ingredientFieldColors(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) }
                )
                ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                    SupermarketCategories.all.forEach { category ->
                        DropdownMenuItem(
                            text = { Text("${category.emoji} ${stringResource(category.labelRes)}") },
                            onClick = { onCategoryChange(category.key); categoryExpanded = false }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ingredientFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.background,
    unfocusedContainerColor = MaterialTheme.colorScheme.background,
    focusedBorderColor = Color.Transparent,
    unfocusedBorderColor = Color.Transparent
)

@Composable
private fun DashedAddButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .drawBehind {
                drawRoundRect(
                    color = CoralPrimary,
                    cornerRadius = CornerRadius(16.dp.toPx()),
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f))
                    )
                )
            }
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = CoralPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
    }
}

private fun formatQuantity(qty: Double): String =
    if (qty == qty.toLong().toDouble()) qty.toLong().toString() else "%.2f".format(qty)
