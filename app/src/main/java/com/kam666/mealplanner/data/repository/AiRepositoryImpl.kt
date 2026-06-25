package com.kam666.mealplanner.data.repository

import com.kam666.mealplanner.data.remote.ai.AiApiService
import com.kam666.mealplanner.domain.model.RecipeCategory
import com.kam666.mealplanner.domain.model.RecipeSuggestion
import com.kam666.mealplanner.domain.model.SuggestedIngredient
import com.kam666.mealplanner.domain.repository.AiRepository
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject

private val json = Json { ignoreUnknownKeys = true }

private const val SYSTEM_PROMPT = """Eres un asistente culinario experto.
Responde ÚNICAMENTE con un array JSON válido (sin markdown, sin texto extra) con exactamente 3 recetas.
Cada receta debe tener esta estructura exacta:
{
  "title": "Nombre de la receta",
  "description": "Descripción breve",
  "category": "ALMUERZO",
  "estimatedTimeMinutes": 30,
  "preparationSteps": ["Paso 1", "Paso 2"],
  "suggestedIngredients": [
    {"name": "Pollo", "quantity": 200.0, "unit": "G"}
  ]
}
Los valores válidos para category son: DESAYUNO, ALMUERZO, CENA, SNACK, POSTRE.
Los valores válidos para unit son: G, KG, ML, L, UNIDAD, CUCHARADA, CUCHARADITA."""

private const val SYSTEM_PROMPT_EXTRACT = """Eres un asistente culinario experto en leer recetas de fotos, documentos o páginas web.
Lee el contenido que te paso (imagen o texto) y extrae LA receta que encuentres, sin inventar datos que no estén presentes.
Si no encontrás ninguna receta reconocible, responde ÚNICAMENTE la palabra: null
Si encontrás una receta, responde ÚNICAMENTE con un objeto JSON válido (sin markdown, sin texto extra) con esta estructura exacta:
{
  "title": "Nombre de la receta",
  "description": "Descripción breve",
  "category": "ALMUERZO",
  "estimatedTimeMinutes": 30,
  "preparationSteps": ["Paso 1", "Paso 2"],
  "suggestedIngredients": [
    {"name": "Pollo", "quantity": 200.0, "unit": "G"}
  ]
}
Los valores válidos para category son: DESAYUNO, ALMUERZO, CENA, SNACK, POSTRE.
Los valores válidos para unit son: G, KG, ML, L, UNIDAD, CUCHARADA, CUCHARADITA."""

class AiRepositoryImpl @Inject constructor(
    private val aiApiService: AiApiService
) : AiRepository {

    override suspend fun suggestRecipes(
        availableIngredients: List<String>,
        userPreferences: String,
        excludeTitles: List<String>
    ): List<RecipeSuggestion> {
        val ingredientText = if (availableIngredients.isEmpty()) "ingredientes variados"
        else availableIngredients.joinToString(", ")

        val userMessage = buildString {
            append("Ingredientes disponibles: $ingredientText.")
            if (userPreferences.isNotBlank()) append(" Preferencias: $userPreferences.")
            if (excludeTitles.isNotEmpty()) {
                append(" NO sugieras ninguna de estas recetas que ya se mostraron: ${excludeTitles.joinToString(", ")}.")
            }
            append(" Sugiere 3 recetas DIFERENTES.")
        }

        val rawResponse = aiApiService.complete(SYSTEM_PROMPT, userMessage)
        return parseResponse(rawResponse)
    }

    private fun parseResponse(raw: String): List<RecipeSuggestion> {
        val cleaned = raw.trim()
            .removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
        return try {
            val dtos = json.decodeFromString<List<RecipeSuggestionDto>>(cleaned)
            dtos.map { it.toDomain() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun extractRecipeFromImage(imageBase64: String, mimeType: String): RecipeSuggestion? {
        val rawResponse = aiApiService.completeWithImage(SYSTEM_PROMPT_EXTRACT, imageBase64, mimeType)
        return parseSingleResponse(rawResponse)
    }

    override suspend fun extractRecipeFromText(rawText: String): RecipeSuggestion? {
        val rawResponse = aiApiService.complete(SYSTEM_PROMPT_EXTRACT, rawText)
        return parseSingleResponse(rawResponse)
    }

    private fun parseSingleResponse(raw: String): RecipeSuggestion? {
        val cleaned = raw.trim()
            .removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
        if (cleaned.isEmpty() || cleaned.equals("null", ignoreCase = true)) return null
        return try {
            json.decodeFromString<RecipeSuggestionDto>(cleaned).toDomain()
        } catch (e: Exception) {
            null
        }
    }
}

@Serializable
private data class RecipeSuggestionDto(
    val title: String,
    val description: String,
    val category: String,
    val estimatedTimeMinutes: Int? = null,
    val preparationSteps: List<String> = emptyList(),
    val suggestedIngredients: List<SuggestedIngredientDto> = emptyList()
) {
    fun toDomain() = RecipeSuggestion(
        title = title,
        description = description,
        category = RecipeCategory.entries.firstOrNull { it.name == category } ?: RecipeCategory.ALMUERZO,
        estimatedTimeMinutes = estimatedTimeMinutes,
        preparationSteps = preparationSteps,
        suggestedIngredients = suggestedIngredients.map { it.toDomain() }
    )
}

@Serializable
private data class SuggestedIngredientDto(
    val name: String,
    val quantity: Double,
    val unit: String
) {
    fun toDomain() = SuggestedIngredient(name = name, quantity = quantity, unit = unit)
}
