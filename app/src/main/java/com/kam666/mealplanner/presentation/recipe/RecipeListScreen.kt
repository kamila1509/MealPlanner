package com.kam666.mealplanner.presentation.recipe

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items as lazyItems
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kam666.mealplanner.R
import com.kam666.mealplanner.domain.model.RecipeCategory
import com.kam666.mealplanner.presentation.common.PrimaryFab
import com.kam666.mealplanner.presentation.common.RecipeGridCard
import com.kam666.mealplanner.presentation.common.RecipeVisuals
import com.kam666.mealplanner.presentation.common.ScreenHeader
import com.kam666.mealplanner.presentation.common.localizedName

@Composable
fun RecipeListScreen(
    onRecipeClick: (Long) -> Unit,
    onAddRecipe: () -> Unit,
    onIngredientsClick: () -> Unit,
    onImportRecipe: () -> Unit,
    viewModel: RecipeListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val recipes by viewModel.filteredRecipes.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val importState by viewModel.importState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    val xlsxMime = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.onExcelFileSelected(it) }
    }

    val successMessage = if (importState is ImportUiState.Success) {
        stringResource(R.string.import_success, (importState as ImportUiState.Success).recipesAdded)
    } else null

    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            snackbarHostState.showSnackbar(successMessage)
            viewModel.onDismissImport()
        }
    }

    val subtitle = when (val state = uiState) {
        is RecipeListUiState.Success -> stringResource(R.string.recipes_subtitle, state.recipes.size)
        else -> null
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            PrimaryFab(onClick = onAddRecipe, contentDescription = stringResource(R.string.cd_add_recipe))
        },
        floatingActionButtonPosition = FabPosition.End
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is RecipeListUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is RecipeListUiState.Success -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            ScreenHeader(
                                brand = stringResource(R.string.brand),
                                title = stringResource(R.string.title_recipes),
                                subtitle = subtitle,
                                trailing = {
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        IconButton(onClick = onImportRecipe) {
                                            Icon(
                                                Icons.Filled.AutoAwesome,
                                                contentDescription = stringResource(R.string.cd_import_recipe_ai),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        IconButton(onClick = { filePicker.launch(xlsxMime) }) {
                                            Icon(
                                                Icons.Filled.FileUpload,
                                                contentDescription = stringResource(R.string.cd_import_excel),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        IconButton(onClick = onIngredientsClick) {
                                            Icon(
                                                Icons.Filled.Kitchen,
                                                contentDescription = stringResource(R.string.cd_ingredients),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            )
                            LazyRow(
                                modifier = Modifier.padding(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                item {
                                    CategoryChip(
                                        label = stringResource(R.string.filter_all),
                                        selected = selectedCategory == null,
                                        onClick = { viewModel.setCategoryFilter(null) }
                                    )
                                }
                                lazyItems(RecipeCategory.entries) { category ->
                                    CategoryChip(
                                        label = "${RecipeVisuals.categoryEmoji(category)} ${category.localizedName()}",
                                        selected = selectedCategory == category,
                                        onClick = { viewModel.setCategoryFilter(category) }
                                    )
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                        }
                        if (recipes.isEmpty()) {
                            item {
                                Text(
                                    stringResource(R.string.empty_recipes),
                                    modifier = Modifier.fillMaxWidth().padding(48.dp),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            item {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 200.dp, max = 4000.dp)
                                        .padding(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp),
                                    userScrollEnabled = false
                                ) {
                                    gridItems(recipes, key = { it.id }) { recipe ->
                                        RecipeGridCard(
                                            name = recipe.name,
                                            emoji = RecipeVisuals.emoji(recipe),
                                            tint = RecipeVisuals.tint(recipe),
                                            timeLabel = recipe.preparationTimeMinutes?.let {
                                                stringResource(R.string.prep_time_chip, it)
                                            },
                                            categoryLabel = recipe.category.localizedName(),
                                            categoryEmoji = RecipeVisuals.categoryEmoji(recipe.category),
                                            ingredientCountLabel = stringResource(
                                                R.string.ingredient_count_short,
                                                recipe.ingredients.size
                                            ),
                                            onClick = { onRecipeClick(recipe.id) }
                                        )
                                    }
                                }
                                Spacer(Modifier.height(120.dp))
                            }
                        }
                    }
                }
                is RecipeListUiState.Error -> {
                    Text(
                        state.message ?: stringResource(R.string.error_loading_recipes),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }

    ImportDialog(
        state = importState,
        onConfirm = viewModel::onConfirmImport,
        onDismiss = viewModel::onDismissImport
    )
}

@Composable
private fun ImportDialog(
    state: ImportUiState,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    when (state) {
        is ImportUiState.Parsing -> {
            AlertDialog(
                onDismissRequest = {},
                title = { Text(stringResource(R.string.import_parsing)) },
                text = { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) },
                confirmButton = {}
            )
        }
        is ImportUiState.Preview -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(stringResource(R.string.import_preview_title)) },
                text = {
                    Text(stringResource(R.string.import_preview_body, state.recipeCount, state.ingredientCount))
                },
                confirmButton = {
                    TextButton(onClick = onConfirm) { Text(stringResource(R.string.action_import)) }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
                }
            )
        }
        is ImportUiState.Importing -> {
            AlertDialog(
                onDismissRequest = {},
                title = { Text(stringResource(R.string.import_importing)) },
                text = { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) },
                confirmButton = {}
            )
        }
        is ImportUiState.Error -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(stringResource(R.string.import_error_title)) },
                text = { Text(state.message) },
                confirmButton = {
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
                }
            )
        }
        else -> {}
    }
}

@Composable
private fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = null
    )
}
