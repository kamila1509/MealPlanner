package com.kam666.mealplanner.presentation.pricepreview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kam666.mealplanner.domain.model.MercadonaProduct
import com.kam666.mealplanner.domain.model.PriceMatchResult
import com.kam666.mealplanner.domain.model.ShoppingListItem
import com.kam666.mealplanner.domain.repository.MercadonaRepository
import com.kam666.mealplanner.domain.usecase.mercadona.GetMercadonaPricePreviewUseCase
import com.kam666.mealplanner.domain.usecase.mercadona.UnitConversionHelper
import com.kam666.mealplanner.presentation.common.WeekSelectionHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class MoreProductsState {
    object Idle : MoreProductsState()
    object Loading : MoreProductsState()
    data class Loaded(val products: List<MercadonaProduct>) : MoreProductsState()
    data class Error(val message: String) : MoreProductsState()
}

sealed class MercadonaPreviewUiState {
    object Loading : MercadonaPreviewUiState()
    data class Error(val message: String) : MercadonaPreviewUiState()
    data class Success(
        val results: List<PriceMatchResult>,
        val totalEstimatedCost: Double,
        val matchedCount: Int,
        val noMatchCount: Int,
        val hasApproximations: Boolean
    ) : MercadonaPreviewUiState()
}

@HiltViewModel
class MercadonaPricePreviewViewModel @Inject constructor(
    private val weekSelection: WeekSelectionHolder,
    private val getMercadonaPricePreview: GetMercadonaPricePreviewUseCase,
    private val mercadonaRepository: MercadonaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MercadonaPreviewUiState>(MercadonaPreviewUiState.Loading)
    val uiState: StateFlow<MercadonaPreviewUiState> = _uiState.asStateFlow()

    private val _moreProductsState = MutableStateFlow<MoreProductsState>(MoreProductsState.Idle)
    val moreProductsState: StateFlow<MoreProductsState> = _moreProductsState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = MercadonaPreviewUiState.Loading
            runCatching {
                getMercadonaPricePreview(weekSelection.weekStart.value)
            }.fold(
                onSuccess = { results ->
                    _uiState.value = buildSuccess(results)
                },
                onFailure = { e ->
                    _uiState.value = MercadonaPreviewUiState.Error(e.message ?: "Error desconocido")
                }
            )
        }
    }

    fun adjustPackages(matched: PriceMatchResult.Matched, delta: Int) {
        val current = _uiState.value as? MercadonaPreviewUiState.Success ?: return
        val updated = current.results.map { result ->
            if (result is PriceMatchResult.Matched &&
                result.shoppingItem.ingredient.id == matched.shoppingItem.ingredient.id
            ) {
                val newPacks = (result.packagesNeeded + delta).coerceAtLeast(1)
                result.copy(
                    packagesNeeded = newPacks,
                    estimatedCost = newPacks * result.product.unitPrice
                )
            } else result
        }
        _uiState.value = buildSuccess(updated)
    }

    fun loadMoreProducts(matched: PriceMatchResult.Matched) {
        _moreProductsState.value = MoreProductsState.Loading
        viewModelScope.launch {
            runCatching {
                mercadonaRepository.searchProducts(matched.shoppingItem.ingredient.name, hitsPerPage = 20)
            }.fold(
                onSuccess = { dtos ->
                    val excluded = (listOf(matched.product) + matched.alternatives).map { it.id }.toSet()
                    val products = dtos
                        .filter { it.id !in excluded }
                        .map { dto ->
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
                    _moreProductsState.value = MoreProductsState.Loaded(products)
                },
                onFailure = { e ->
                    _moreProductsState.value = MoreProductsState.Error(e.message ?: "Error")
                }
            )
        }
    }

    fun clearMoreProducts() {
        _moreProductsState.value = MoreProductsState.Idle
    }

    fun swapProduct(matchedResult: PriceMatchResult.Matched, newProduct: MercadonaProduct) {
        val current = _uiState.value as? MercadonaPreviewUiState.Success ?: return
        val updated = current.results.map { result ->
            if (result is PriceMatchResult.Matched &&
                result.shoppingItem.ingredient.id == matchedResult.shoppingItem.ingredient.id
            ) {
                val packages = UnitConversionHelper.packagesNeeded(result.requiredInBulkUnit, newProduct.unitSize)
                val newAlternatives = (listOf(result.product) + result.alternatives.filterNot { it.id == newProduct.id }).take(4)
                result.copy(
                    product = newProduct,
                    packagesNeeded = packages,
                    estimatedCost = packages * newProduct.unitPrice,
                    alternatives = newAlternatives
                )
            } else result
        }
        _uiState.value = buildSuccess(updated)
    }

    private fun buildSuccess(results: List<PriceMatchResult>): MercadonaPreviewUiState.Success {
        val matched = results.filterIsInstance<PriceMatchResult.Matched>()
        return MercadonaPreviewUiState.Success(
            results = results,
            totalEstimatedCost = matched.sumOf { it.estimatedCost },
            matchedCount = matched.size,
            noMatchCount = results.size - matched.size,
            hasApproximations = matched.any { m ->
                m.shoppingItem.ingredient.unit.let { u ->
                    u == com.kam666.mealplanner.domain.model.IngredientUnit.CUCHARADA ||
                    u == com.kam666.mealplanner.domain.model.IngredientUnit.CUCHARADITA
                }
            }
        )
    }
}
