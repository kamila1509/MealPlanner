package com.kam666.mealplanner.domain.usecase.mercadona

import com.kam666.mealplanner.domain.model.MercadonaProduct
import com.kam666.mealplanner.domain.model.PriceMatchResult
import com.kam666.mealplanner.domain.model.ShoppingListItem
import com.kam666.mealplanner.domain.repository.MercadonaRepository
import javax.inject.Inject
import kotlin.math.ceil

class MatchMercadonaProductsUseCase @Inject constructor(
    private val mercadonaRepository: MercadonaRepository
) {
    suspend operator fun invoke(items: List<ShoppingListItem>): List<PriceMatchResult> =
        items.map { item -> matchItem(item) }

    private suspend fun matchItem(item: ShoppingListItem): PriceMatchResult {
        val conversion = UnitConversionHelper.convert(item.totalQuantity, item.ingredient.unit)
            ?: return PriceMatchResult.NoMatch(item)

        val candidates = mercadonaRepository.searchProducts(item.ingredient.name, hitsPerPage = 8)
        if (candidates.isEmpty()) return PriceMatchResult.NoMatch(item)

        // Score each candidate:
        //   primary   → packs needed (fewer = better)
        //   secondary → waste in units (less = better)
        //   tertiary  → name distance from ingredient (0 = exact, 1 = many extra words)
        val required = conversion.valueInBulkUnit
        val bulkUnit = conversion.bulkUnit

        // Discard candidates with zero name relevance (e.g. "espinas" for "espinacas")
        val relevant = candidates.filter { dto ->
            nameRelevanceScore(item.ingredient.name, dto.displayName) > 0.0
        }
        val pool = relevant.ifEmpty { candidates } // fallback to all if nothing passes

        val bestDto = pool.minByOrNull { dto ->
            val size = dto.priceInstructions.unitSize.takeIf { it > 0.0 } ?: 1.0
            val compatible = unitsCompatible(bulkUnit, dto.priceInstructions.sizeFormat)
            val packs = if (compatible) ceil(required / size) else ceil(required)
            val waste = if (compatible) packs * size - required else packs * 0.5
            val nameDist = 1.0 - nameRelevanceScore(item.ingredient.name, dto.displayName)
            val unitPenalty = if (compatible) 0.0 else 10_000_000.0
            unitPenalty + packs * 1_000_000.0 + waste * 1_000.0 + nameDist * 100.0
        } ?: return PriceMatchResult.NoMatch(item)

        // Final guard: if the best match still shares no words with the ingredient, skip it
        if (nameRelevanceScore(item.ingredient.name, bestDto.displayName) == 0.0) {
            return PriceMatchResult.NoMatch(item)
        }

        val unitPrice = bestDto.priceInstructions.unitPrice.toDoubleOrNull()
            ?: return PriceMatchResult.NoMatch(item)
        val unitSize = bestDto.priceInstructions.unitSize.takeIf { it > 0.0 } ?: 1.0
        val compatible = unitsCompatible(bulkUnit, bestDto.priceInstructions.sizeFormat)

        // When units are incompatible (e.g. "ud" ingredient vs "kg" product), don't divide
        // different dimensions. Fall back to ceil(required) packs (1 pack per unit needed).
        val packagesNeeded = if (compatible) {
            UnitConversionHelper.packagesNeeded(required, unitSize)
        } else {
            ceil(required).toInt().coerceAtLeast(1)
        }
        val estimatedCost = packagesNeeded * unitPrice

        val alternatives = candidates.filterNot { it.id == bestDto.id }.take(4).map { dto ->
            MercadonaProduct(
                id = dto.id,
                name = dto.displayName,
                thumbnailUrl = dto.thumbnail,
                packaging = dto.packaging.orEmpty(),
                unitPrice = dto.priceInstructions.unitPrice.toDoubleOrNull() ?: 0.0,
                bulkPrice = dto.priceInstructions.bulkPrice?.toDoubleOrNull(),
                unitSize = dto.priceInstructions.unitSize,
                sizeFormat = dto.priceInstructions.sizeFormat
            )
        }

        return PriceMatchResult.Matched(
            shoppingItem = item,
            product = MercadonaProduct(
                id = bestDto.id,
                name = bestDto.displayName,
                thumbnailUrl = bestDto.thumbnail,
                packaging = bestDto.packaging.orEmpty(),
                unitPrice = unitPrice,
                bulkPrice = bestDto.priceInstructions.bulkPrice?.toDoubleOrNull(),
                unitSize = unitSize,
                sizeFormat = bestDto.priceInstructions.sizeFormat.lowercase()
            ),
            packagesNeeded = packagesNeeded,
            estimatedCost = estimatedCost,
            alternatives = alternatives,
            requiredInBulkUnit = conversion.valueInBulkUnit,
            bulkUnit = conversion.bulkUnit
        )
    }

    /**
     * Returns a score in [0, 1] representing how closely the product name matches
     * the ingredient name. Score = ingredient words found in product / total product
     * content words (after stripping numbers, units, size codes, stop words).
     *
     * "huevos XL 24 ud" → content={"huevos"} → score=1.0 (exact match)
     * "huevos camperos 6 ud" → content={"huevos","camperos"} → score=0.5
     * "huevos cocidos 6 ud" → content={"huevos","cocidos"} → score=0.5
     *
     * Products with fewer extra words score higher, helping choose simple/raw
     * products over prepared variants when packs and waste are equal.
     */
    // "ud" ↔ "ud", "uds" — "kg" ↔ "kg" — "l" ↔ "l"
    // Prevents dividing "2 ud" by "0.3 kg" which gives nonsensical pack counts
    private fun unitsCompatible(bulkUnit: String, sizeFormat: String): Boolean {
        fun normalize(s: String) = s.lowercase().trimEnd('s') // "uds"→"ud", "kgs"→"kg"
        val a = normalize(bulkUnit)
        val b = normalize(sizeFormat)
        if (a == b) return true
        // Weight group: g and kg are compatible (both mass)
        val weight = setOf("g", "kg")
        // Volume group: ml and l are compatible
        val volume = setOf("ml", "l")
        return (a in weight && b in weight) || (a in volume && b in volume)
    }

    private fun nameRelevanceScore(ingredientName: String, productName: String): Double {
        val stopWords = setOf(
            "ud", "uds", "pack", "bandeja", "de", "con", "y", "e",
            "del", "la", "el", "los", "las", "xl", "xxl", "xs", "xm", "sin"
        )
        val numberPattern = Regex("""^\d+([.,]\d+)?$""")

        // Strip trailing 's' to unify Spanish singular/plural: espinacas→espinaca, frescas→fresca
        fun stem(w: String) = if (w.length > 3 && w.endsWith('s')) w.dropLast(1) else w

        fun contentWords(name: String): Set<String> = name
            .lowercase()
            .split(Regex("""[\s,.()\-/+]+"""))
            .filter { w ->
                w.length >= 2 &&
                !numberPattern.matches(w) &&
                w !in stopWords
            }
            .map { stem(it) }
            .toSet()

        val ingWords = contentWords(ingredientName)
        val prodWords = contentWords(productName)

        if (ingWords.isEmpty() || prodWords.isEmpty()) return 0.5

        val matched = ingWords.intersect(prodWords).size
        return matched.toDouble() / prodWords.size.toDouble()
    }
}
