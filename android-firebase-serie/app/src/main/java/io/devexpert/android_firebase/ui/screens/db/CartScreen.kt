package io.devexpert.android_firebase.ui.screens.db

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.devexpert.android_firebase.utils.CartManager
import io.devexpert.android_firebase.model.Product

@Composable
fun CartScreen() {
    val cartItems = CartManager.items
    val total = cartItems.sumOf { it.price ?: 0.0 }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Carrito") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (cartItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Tu carrito está vacío")
                }
                return@Column
            }

            // 2) Lista de items
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cartItems, key = { it.id!! }) { product ->
                    CartItem(product)
                }
            }

            Divider()

            // 3) Mostrar total
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total:",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "\$${"%.2f".format(total)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // 4) Botón de Pagar (inactivo)
            Button(
                onClick = { /* TODO: implementar pago */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                enabled = false  // deshabilitado por ahora
            ) {
                Text("Pagar")
            }
        }
    }
}

@Composable
fun CartItem(product: Product) {
    val name  = product.name.orEmpty()
    val price = product.price ?: 0.0

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Precio: \$${"%.2f".format(price)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(onClick = { CartManager.removeFromCart(product) }) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar")
            }
        }
    }
}
