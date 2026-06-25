package com.kam666.mealplanner.presentation.recipe

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kam666.mealplanner.data.local.excel.ExcelImportData
import com.kam666.mealplanner.data.local.excel.ExcelParser
import com.kam666.mealplanner.domain.model.Recipe
import com.kam666.mealplanner.domain.model.RecipeCategory
import com.kam666.mealplanner.domain.usecase.recipe.GetAllRecipesUseCase
import com.kam666.mealplanner.domain.usecase.recipe.ImportRecipesFromExcelUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed class RecipeListUiState {
    object Loading : RecipeListUiState()
    data class Success(val recipes: List<Recipe>, val mealCount: Int = 0) : RecipeListUiState()
    data class Error(val message: String? = null) : RecipeListUiState()
}

sealed class ImportUiState {
    object Idle : ImportUiState()
    object Parsing : ImportUiState()
    data class Preview(val recipeCount: Int, val ingredientCount: Int) : ImportUiState()
    object Importing : ImportUiState()
    data class Success(val recipesAdded: Int) : ImportUiState()
    data class Error(val message: String) : ImportUiState()
}

@HiltViewModel
class RecipeListViewModel @Inject constructor(
    getAllRecipes: GetAllRecipesUseCase,
    private val importRecipesUseCase: ImportRecipesFromExcelUseCase,
    private val excelParser: ExcelParser,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val categoryFilter = MutableStateFlow<RecipeCategory?>(null)

    val uiState: StateFlow<RecipeListUiState> = getAllRecipes()
        .map<List<Recipe>, RecipeListUiState> { RecipeListUiState.Success(it) }
        .catch { emit(RecipeListUiState.Error(it.message)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = RecipeListUiState.Loading
        )

    val filteredRecipes: StateFlow<List<Recipe>> = combine(uiState, categoryFilter) { state, filter ->
        val recipes = (state as? RecipeListUiState.Success)?.recipes.orEmpty()
        if (filter == null) recipes else recipes.filter { it.category == filter }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val selectedCategory: StateFlow<RecipeCategory?> =
        categoryFilter.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _importState = MutableStateFlow<ImportUiState>(ImportUiState.Idle)
    val importState: StateFlow<ImportUiState> = _importState.asStateFlow()

    private var pendingImportData: ExcelImportData? = null

    fun setCategoryFilter(category: RecipeCategory?) {
        categoryFilter.value = category
    }

    fun onExcelFileSelected(uri: Uri) {
        viewModelScope.launch {
            _importState.value = ImportUiState.Parsing
            Log.d("ImportExcel", "Inicio de importación, uri=$uri")
            try {
                val data = withContext(Dispatchers.IO) {
                    val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                        ?: error("No se pudo abrir el archivo")
                    Log.d("ImportExcel", "Archivo leído: ${bytes.size} bytes")
                    val result = excelParser.parse(bytes.inputStream())
                    Log.d("ImportExcel", "Parsed: ${result.recipes.size} recetas, ${result.ingredients.size} ingredientes")
                    result
                }
                if (data.recipes.isEmpty()) {
                    _importState.value = ImportUiState.Error(
                        "No se encontraron recetas.\nVerifica que el archivo tenga una hoja llamada \"Recetas\"."
                    )
                } else {
                    pendingImportData = data
                    _importState.value = ImportUiState.Preview(data.recipes.size, data.ingredients.size)
                }
            } catch (e: Exception) {
                Log.e("ImportExcel", "Error al importar", e)
                _importState.value = ImportUiState.Error(
                    "${e.javaClass.simpleName}: ${e.message ?: "Error desconocido"}"
                )
            }
        }
    }

    fun onConfirmImport() {
        val data = pendingImportData ?: return
        viewModelScope.launch {
            _importState.value = ImportUiState.Importing
            try {
                val result = withContext(Dispatchers.IO) { importRecipesUseCase(data) }
                pendingImportData = null
                _importState.value = ImportUiState.Success(result.recipesAdded)
            } catch (e: Exception) {
                _importState.value = ImportUiState.Error(e.message ?: "Error al importar")
            }
        }
    }

    fun onDismissImport() {
        pendingImportData = null
        _importState.value = ImportUiState.Idle
    }
}
