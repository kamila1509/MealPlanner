package com.kam666.mealplanner.presentation.aisuggestions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kam666.mealplanner.domain.model.RecipeSuggestion
import com.kam666.mealplanner.presentation.theme.AppBackground
import com.kam666.mealplanner.presentation.theme.AppMuted
import com.kam666.mealplanner.presentation.theme.AppText
import com.kam666.mealplanner.presentation.theme.CoralPrimary
import com.kam666.mealplanner.presentation.theme.PrimaryContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiSuggestionsScreen(
    viewModel: AiSuggestionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isLoadingMore by viewModel.isLoadingMore.collectAsStateWithLifecycle()
    val savedMessage by viewModel.savedMessage.collectAsStateWithLifecycle()
    var preferences by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(savedMessage) {
        val msg = savedMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(
            message = msg,
            duration = SnackbarDuration.Short
        )
        viewModel.clearSavedMessage()
    }

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Sugerencias IA",
                        fontWeight = FontWeight.Bold,
                        color = AppText
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBackground)
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = CoralPrimary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = preferences,
                onValueChange = { preferences = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("¿Qué te apetece? (opcional)") },
                placeholder = { Text("Ej: algo ligero con pollo, sin gluten...") },
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CoralPrimary,
                    unfocusedBorderColor = AppMuted.copy(alpha = 0.4f)
                )
            )

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = { viewModel.suggest(preferences) },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is AiSuggestionsUiState.Loading,
                colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Sugerir recetas con IA")
            }

            Spacer(Modifier.height(16.dp))

            when (val state = uiState) {
                is AiSuggestionsUiState.Idle -> {
                    IdleHint()
                }
                is AiSuggestionsUiState.Loading -> {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = CoralPrimary)
                            Spacer(Modifier.height(8.dp))
                            Text("Generando sugerencias...", color = AppMuted)
                        }
                    }
                }
                is AiSuggestionsUiState.Error -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            state.message,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                is AiSuggestionsUiState.Success -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(state.suggestions) { suggestion ->
                            SuggestionCard(
                                suggestion = suggestion,
                                onSave = { viewModel.saveRecipe(suggestion) }
                            )
                        }
                        item {
                            Button(
                                onClick = { viewModel.loadMore() },
                                enabled = !isLoadingMore,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (isLoadingMore) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = androidx.compose.ui.graphics.Color.White
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("Cargando más...")
                                } else {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Mostrar más sugerencias")
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IdleHint() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = CoralPrimary.copy(alpha = 0.4f),
                modifier = Modifier.size(56.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Usa tus ingredientes disponibles\npara que la IA sugiera recetas",
                color = AppMuted,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun SuggestionCard(
    suggestion: RecipeSuggestion,
    onSave: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        suggestion.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppText
                    )
                    suggestion.estimatedTimeMinutes?.let { mins ->
                        Text(
                            "$mins min · ${suggestion.category.name.lowercase().replaceFirstChar { it.uppercase() }}",
                            style = MaterialTheme.typography.labelSmall,
                            color = AppMuted
                        )
                    }
                }
                IconButton(onClick = onSave) {
                    Icon(
                        Icons.Default.BookmarkAdd,
                        contentDescription = "Guardar receta",
                        tint = CoralPrimary
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                suggestion.description,
                style = MaterialTheme.typography.bodySmall,
                color = AppText.copy(alpha = 0.8f)
            )

            if (suggestion.suggestedIngredients.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Ingredientes principales:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AppMuted
                )
                suggestion.suggestedIngredients.take(4).forEach { ing ->
                    Text(
                        "• ${ing.name} — ${ing.quantity} ${ing.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppText.copy(alpha = 0.7f)
                    )
                }
                if (suggestion.suggestedIngredients.size > 4) {
                    Text(
                        "+ ${suggestion.suggestedIngredients.size - 4} más",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppMuted
                    )
                }
            }
        }
    }
}
