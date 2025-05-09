package com.pete.ecommerceapp.ui.feature.product_details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pete.domain.di.model.request.AddCartRequestModel
import com.pete.domain.di.network.ResultWrapper
import com.pete.domain.di.usecase.AddToCartUseCase
import com.pete.ecommerceapp.ShopperSession
import com.pete.ecommerceapp.model.UiProductModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductDetailsViewModel(
    private val addToCartUseCase: AddToCartUseCase
): ViewModel() {
    private val _state = MutableStateFlow<ProductDetailsEvent>(ProductDetailsEvent.Nothing)
    val state = _state.asStateFlow()
    val userDomainModel = ShopperSession.getUser()
    
    private val _quantity = MutableStateFlow(1)
    val quantity = _quantity.asStateFlow()

    fun increaseQuantity() {
        if (_quantity.value < 10) { // Maximum quantity limit
            _quantity.value++
        }
    }

    fun decreaseQuantity() {
        if (_quantity.value > 1) { // Minimum quantity limit
            _quantity.value--
        }
    }

    fun addProductToCart(product: UiProductModel) {
        viewModelScope.launch {
            _state.value = ProductDetailsEvent.Loading
            val result = addToCartUseCase.execute(
                AddCartRequestModel(
                    product.id,
                    product.title,
                    product.price,
                    _quantity.value,
                    userDomainModel!!.id!!
                ),
                userDomainModel.id!!.toLong()
            )
            when (result) {
                is ResultWrapper.Success -> {
                    _state.value = ProductDetailsEvent.Success("Product added to cart")
                }

                is ResultWrapper.Failure -> {
                    _state.value = ProductDetailsEvent.Error("Failed to add product to cart")
                }
            }
        }
    }

    sealed class ProductDetailsEvent {
        data object Loading : ProductDetailsEvent()
        data object Nothing : ProductDetailsEvent()
        data class Success(val message: String) : ProductDetailsEvent()
        data class Error(val message: String) : ProductDetailsEvent()
    }
}