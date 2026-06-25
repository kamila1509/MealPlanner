package com.kam666.mealplanner.presentation.importrecipe

import android.content.Context
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kam666.mealplanner.domain.model.RecipeSuggestion
import com.kam666.mealplanner.domain.repository.AiRepository
import com.kam666.mealplanner.domain.usecase.ai.SaveSuggestedRecipeUseCase
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import javax.inject.Inject

sealed class ImportRecipeUiState {
    object Idle : ImportRecipeUiState()
    object Loading : ImportRecipeUiState()
    data class Preview(val suggestion: RecipeSuggestion) : ImportRecipeUiState()
    object NotFound : ImportRecipeUiState()
    data class Error(val message: String) : ImportRecipeUiState()
}

@HiltViewModel
class ImportRecipeViewModel @Inject constructor(
    private val aiRepository: AiRepository,
    private val saveSuggestedRecipeUseCase: SaveSuggestedRecipeUseCase,
    private val httpClient: HttpClient,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<ImportRecipeUiState>(ImportRecipeUiState.Idle)
    val uiState: StateFlow<ImportRecipeUiState> = _uiState.asStateFlow()

    private val _savedMessage = MutableStateFlow<String?>(null)
    val savedMessage: StateFlow<String?> = _savedMessage.asStateFlow()

    fun onImageSelected(uri: Uri) {
        runImport {
            val (bytes, mimeType) = withContext(Dispatchers.IO) {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: error("No se pudo leer la imagen")
                val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
                bytes to mimeType
            }
            val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
            aiRepository.extractRecipeFromImage(base64, mimeType)
        }
    }

    fun onDocumentSelected(uri: Uri) {
        runImport {
            val text = withContext(Dispatchers.IO) { extractTextFromDocument(uri) }
            aiRepository.extractRecipeFromText(text)
        }
    }

    fun onUrlSubmitted(url: String) {
        runImport {
            val text = withContext(Dispatchers.IO) {
                val html = httpClient.get(url).bodyAsText()
                Jsoup.parse(html).text()
            }
            aiRepository.extractRecipeFromText(text)
        }
    }

    private fun runImport(extract: suspend () -> RecipeSuggestion?) {
        viewModelScope.launch {
            _uiState.value = ImportRecipeUiState.Loading
            runCatching { extract() }.fold(
                onSuccess = { suggestion ->
                    _uiState.value = if (suggestion != null)
                        ImportRecipeUiState.Preview(suggestion)
                    else
                        ImportRecipeUiState.NotFound
                },
                onFailure = { e ->
                    Log.e("ImportRecipe", "Error al importar receta", e)
                    _uiState.value = ImportRecipeUiState.Error(e.message ?: "Error desconocido")
                }
            )
        }
    }

    private fun extractTextFromDocument(uri: Uri): String {
        val mimeType = context.contentResolver.getType(uri) ?: ""
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: error("No se pudo leer el documento")
        return if (mimeType == "application/pdf") {
            PDFBoxResourceLoader.init(context)
            inputStream.use { stream ->
                PDDocument.load(stream).use { document -> PDFTextStripper().getText(document) }
            }
        } else {
            inputStream.use { it.bufferedReader().readText() }
        }
    }

    fun saveSuggestion(suggestion: RecipeSuggestion) {
        viewModelScope.launch {
            runCatching { saveSuggestedRecipeUseCase(suggestion) }.fold(
                onSuccess = {
                    _savedMessage.value = "\"${suggestion.title}\" guardada en tus recetas"
                    _uiState.value = ImportRecipeUiState.Idle
                },
                onFailure = { _savedMessage.value = "Error al guardar la receta" }
            )
        }
    }

    fun reset() {
        _uiState.value = ImportRecipeUiState.Idle
    }

    fun clearSavedMessage() {
        _savedMessage.value = null
    }
}
