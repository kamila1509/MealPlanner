package com.kam666.mealplanner.presentation.recipe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kam666.mealplanner.R
import com.kam666.mealplanner.domain.model.Recipe
import com.kam666.mealplanner.presentation.common.CircleIconButton
import com.kam666.mealplanner.presentation.common.RecipeVisuals
import com.kam666.mealplanner.presentation.common.SectionLabel
import com.kam666.mealplanner.presentation.common.SupermarketCategories
import com.kam666.mealplanner.presentation.common.localizedName
import com.kam666.mealplanner.presentation.theme.CoralPrimaryDark

@Composable
fun RecipeDetailScreen(
    onEdit: (Long) -> Unit,
    onBack: () -> Unit,
    onAddToPlan: () -> Unit,
    viewModel: RecipeDetailViewModel = hiltViewModel()
) {
    val recipe by viewModel.recipe.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var servings by remember(recipe?.id) { mutableIntStateOf(recipe?.servings ?: 2) }
    var hadRecipe by remember { mutableStateOf(false) }

    LaunchedEffect(recipe?.id) {
        if (recipe != null) hadRecipe = true
    }

    LaunchedEffect(recipe?.servings) {
        recipe?.servings?.let { servings = it }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_recipe_title)) },
            text = { Text(stringResource(R.string.delete_recipe_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.delete()
                    onBack()
                }) { Text(stringResource(R.string.action_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    val r = recipe
    if (r == null) {
        if (hadRecipe) {
            LaunchedEffect(Unit) { onBack() }
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        return
    }

    val scale = servings.toDouble() / r.servings.coerceAtLeast(1)

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                        .background(RecipeVisuals.tint(r)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(RecipeVisuals.emoji(r), fontSize = 104.sp)
                    CircleIconButton(
                        onClick = onBack,
                        contentDescription = stringResource(R.string.cd_back),
                        modifier = Modifier.align(Alignment.TopStart).padding(14.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                    Row(modifier = Modifier.align(Alignment.TopEnd).padding(14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CircleIconButton(onClick = { onEdit(r.id) }, contentDescription = stringResource(R.string.cd_edit)) {
                            Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(20.dp))
                        }
                        CircleIconButton(onClick = { showDeleteDialog = true }, contentDescription = stringResource(R.string.cd_delete)) {
                            Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
            item {
                Column(
                    modifier = Modifier
                        .offset(y = (-26).dp)
                        .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    RecipeDetailContent(recipe = r, servings = servings, scale = scale, onServingsChange = { servings = it })
                    Spacer(Modifier.height(100.dp))
                }
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 22.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.Transparent,
            shadowElevation = 0.dp
        ) {
            Button(
                onClick = { viewModel.addToPlan(onAddToPlan) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Filled.CalendarMonth, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.add_to_plan), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun RecipeDetailContent(
    recipe: Recipe,
    servings: Int,
    scale: Double,
    onServingsChange: (Int) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(999.dp)
    ) {
        Text(
            "${RecipeVisuals.categoryEmoji(recipe.category)} ${recipe.category.localizedName()}",
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelMedium,
            color = CoralPrimaryDark
        )
    }
    Text(
        recipe.name,
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
    )
    recipe.preparationTimeMinutes?.let {
        Text(
            "⏱ ${stringResource(R.string.prep_time_chip, it)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 18.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                stringResource(R.string.label_servings).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                stringResource(R.string.for_people, servings),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            OutlinedIconButton(
                onClick = { onServingsChange((servings - 1).coerceAtLeast(1)) },
                modifier = Modifier.size(38.dp),
                shape = CircleShape
            ) {
                Icon(Icons.Filled.Remove, contentDescription = stringResource(R.string.cd_reduce))
            }
            Text("$servings", style = MaterialTheme.typography.titleLarge)
            FilledIconButton(
                onClick = { onServingsChange((servings + 1).coerceAtMost(20)) },
                modifier = Modifier.size(38.dp),
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.cd_increase), tint = Color.White)
            }
        }
    }

    if (recipe.ingredients.isNotEmpty()) {
        SectionLabel(stringResource(R.string.label_ingredients), modifier = Modifier.padding(top = 24.dp))
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            recipe.ingredients.forEachIndexed { index, ri ->
                val cat = SupermarketCategories.forKey(ri.ingredient.supermarketCategory)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(cat.emoji, fontSize = 20.sp, modifier = Modifier.width(26.dp))
                    Text(ri.ingredient.name, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Text(
                        "${formatQuantity(ri.quantity * scale)} ${ri.ingredient.unit.localizedName()}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = CoralPrimaryDark
                    )
                }
                if (index < recipe.ingredients.lastIndex) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                }
            }
        }
    }

    if (recipe.preparationSteps.isNotEmpty()) {
        SectionLabel(stringResource(R.string.label_preparation), modifier = Modifier.padding(top = 24.dp))
        recipe.preparationSteps.forEachIndexed { index, step ->
            Row(
                modifier = Modifier.padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(13.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text("${index + 1}", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = CoralPrimaryDark)
                }
                Text(step, fontSize = 15.sp, lineHeight = 22.sp, modifier = Modifier.padding(top = 2.dp))
            }
        }
    }
}

private fun formatQuantity(qty: Double): String =
    if (qty == qty.toLong().toDouble()) qty.toLong().toString() else "%.2f".format(qty)
