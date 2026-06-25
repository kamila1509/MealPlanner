package com.kam666.mealplanner.presentation.ingredient

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kam666.mealplanner.R
import com.kam666.mealplanner.domain.model.Ingredient
import com.kam666.mealplanner.domain.model.IngredientUnit
import com.kam666.mealplanner.presentation.common.localizedName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientListScreen(
    onBack: () -> Unit,
    viewModel: IngredientListViewModel = hiltViewModel()
) {
    val ingredients by viewModel.ingredients.collectAsStateWithLifecycle()
    val inUseWarning by viewModel.inUseWarning.collectAsStateWithLifecycle()
    var editingIngredient by remember { mutableStateOf<Ingredient?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    inUseWarning?.let { ingredient ->
        AlertDialog(
            onDismissRequest = viewModel::clearInUseWarning,
            title = { Text("No se puede eliminar") },
            text = { Text("\"${ingredient.name}\" se está usando en una o más recetas. Primero elimínalo de esas recetas para poder borrarlo de la lista.") },
            confirmButton = {
                TextButton(onClick = viewModel::clearInUseWarning) { Text("Entendido") }
            }
        )
    }

    if (showAddDialog || editingIngredient != null) {
        IngredientEditDialog(
            ingredient = editingIngredient,
            onConfirm = { ingredient ->
                viewModel.save(ingredient)
                showAddDialog = false
                editingIngredient = null
            },
            onDismiss = {
                showAddDialog = false
                editingIngredient = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_ingredients)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.cd_add_ingredient))
            }
        }
    ) { padding ->
        if (ingredients.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.empty_ingredients), style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
                items(ingredients, key = { it.id }) { ingredient ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            if (value == SwipeToDismissBoxValue.EndToStart) {
                                viewModel.tryDelete(ingredient)
                                true
                            } else false
                        }
                    )
                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {
                            Box(
                                Modifier.fillMaxSize().padding(end = 16.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Text(stringResource(R.string.swipe_delete), color = MaterialTheme.colorScheme.error)
                            }
                        }
                    ) {
                        ListItem(
                            headlineContent = { Text(ingredient.name) },
                            supportingContent = { Text(ingredient.unit.localizedName()) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .noRippleClickable { editingIngredient = ingredient }
                        )
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}

private fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier =
    this.then(Modifier.clickable(onClick = onClick))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientEditDialog(
    ingredient: Ingredient?,
    onConfirm: (Ingredient) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember(ingredient) { mutableStateOf(ingredient?.name ?: "") }
    var selectedUnit by remember(ingredient) { mutableStateOf(ingredient?.unit ?: IngredientUnit.UNIDAD) }
    var unitExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (ingredient == null) stringResource(R.string.title_new_ingredient)
                else stringResource(R.string.title_edit_ingredient)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.label_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenuBox(expanded = unitExpanded, onExpandedChange = { unitExpanded = it }) {
                    OutlinedTextField(
                        value = selectedUnit.localizedName(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.label_unit)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = unitExpanded, onDismissRequest = { unitExpanded = false }) {
                        IngredientUnit.entries.forEach { unit ->
                            DropdownMenuItem(
                                text = { Text(unit.localizedName()) },
                                onClick = { selectedUnit = unit; unitExpanded = false }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        Ingredient(
                            id = ingredient?.id ?: 0L,
                            name = name.trim(),
                            unit = selectedUnit
                        )
                    )
                },
                enabled = name.isNotBlank()
            ) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        }
    )
}
