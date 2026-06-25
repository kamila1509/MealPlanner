package com.kam666.mealplanner.presentation.importrecipe

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PhotoCamera
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
fun ImportRecipeScreen(
    onBack: () -> Unit,
    viewModel: ImportRecipeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val savedMessage by viewModel.savedMessage.collectAsStateWithLifecycle()
    var urlText by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let(viewModel::onImageSelected) }

    val documentPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let(viewModel::onDocumentSelected) }

    LaunchedEffect(savedMessage) {
        val msg = savedMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg, duration = SnackbarDuration.Short)
        viewModel.clearSavedMessage()
    }

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            TopAppBar(
                title = { Text("Importar receta", fontWeight = FontWeight.Bold, color = AppText) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBackground)
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(snackbarData = data, containerColor = CoralPrimary, contentColor = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Importa una receta desde una foto, un documento o un link",
                color = AppMuted,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(16.dp))

            val loading = uiState is ImportRecipeUiState.Loading

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ImportActionButton(
                    icon = Icons.Default.PhotoCamera,
                    label = "Foto",
                    enabled = !loading,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                )
                ImportActionButton(
                    icon = Icons.Default.Description,
                    label = "Documento",
                    enabled = !loading,
                    modifier = Modifier.weight(1f),
                    onClick = { documentPicker.launch("*/*") }
                )
            }

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = urlText,
                onValueChange = { urlText = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Link de la receta") },
                placeholder = { Text("https://...") },
                enabled = !loading,
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CoralPrimary,
                    unfocusedBorderColor = AppMuted.copy(alpha = 0.4f)
                )
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { viewModel.onUrlSubmitted(urlText.trim()) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !loading && urlText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Link, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Importar desde link")
            }

            Spacer(Modifier.height(20.dp))

            when (val state = uiState) {
                is ImportRecipeUiState.Idle -> {}
                is ImportRecipeUiState.Loading -> {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = CoralPrimary)
                            Spacer(Modifier.height(8.dp))
                            Text("Leyendo la receta...", color = AppMuted)
                        }
                    }
                }
                is ImportRecipeUiState.NotFound -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                "No pudimos detectar una receta en esto que enviaste.",
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(Modifier.height(8.dp))
                            TextButton(onClick = viewModel::reset) { Text("Reintentar") }
                        }
                    }
                }
                is ImportRecipeUiState.Error -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(state.message, color = MaterialTheme.colorScheme.onErrorContainer)
                            Spacer(Modifier.height(8.dp))
                            TextButton(onClick = viewModel::reset) { Text("Reintentar") }
                        }
                    }
                }
                is ImportRecipeUiState.Preview -> {
                    PreviewCard(
                        suggestion = state.suggestion,
                        onSave = { viewModel.saveSuggestion(state.suggestion) },
                        onDiscard = viewModel::reset
                    )
                }
            }
        }
    }
}

@Composable
private fun ImportActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(icon, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(label, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PreviewCard(
    suggestion: RecipeSuggestion,
    onSave: () -> Unit,
    onDiscard: () -> Unit
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
                    Icon(Icons.Default.BookmarkAdd, contentDescription = "Guardar receta", tint = CoralPrimary)
                }
            }

            if (suggestion.description.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(suggestion.description, style = MaterialTheme.typography.bodySmall, color = AppText.copy(alpha = 0.8f))
            }

            if (suggestion.suggestedIngredients.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Ingredientes:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AppMuted
                )
                suggestion.suggestedIngredients.forEach { ing ->
                    Text(
                        "• ${ing.name} — ${ing.quantity} ${ing.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppText.copy(alpha = 0.7f)
                    )
                }
            }

            if (suggestion.preparationSteps.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Pasos:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AppMuted
                )
                suggestion.preparationSteps.forEachIndexed { i, step ->
                    Text(
                        "${i + 1}. $step",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppText.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onDiscard, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                    Text("Descartar")
                }
                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Guardar receta")
                }
            }
        }
    }
}
