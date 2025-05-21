package io.devexpert.android_firebase.utils

import androidx.compose.runtime.mutableStateListOf
import io.devexpert.android_firebase.model.Product

object CartManager {
    private val _items = mutableStateListOf<Product>()
    val items: List<Product> = _items

    fun addToCart(product: Product) {
        _items.add(product)
    }

    fun removeFromCart(product: Product) {
        _items.remove(product)
    }

    fun clearCart() {
        _items.clear()
    }
}
